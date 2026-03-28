package com.mall.tinymall.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.mall.tinymall.entity.document.GoodsKeyword;
import com.mall.tinymall.entity.pojo.Goods;
import com.mall.tinymall.mapper.GoodsMapper;
import com.mall.tinymall.service.KeywordService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 关键词服务实现
 * 使用Jieba分词器提取关键词，存储到MongoDB和Redis
 */
@Service
@Slf4j
public class KeywordServiceImpl implements KeywordService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final JiebaSegmenter segmenter = new JiebaSegmenter();

    /**
     * Redis缓存Key
     */
    private static final String REDIS_KEY_KEYWORDS = "ai:keywords:goods";
    private static final String REDIS_KEY_LAST_UPDATE = "ai:keywords:last_update";

    /**
     * 关键词最小长度（过滤单字）
     */
    private static final int MIN_KEYWORD_LENGTH = 2;

    /**
     * 关键词最大长度
     */
    private static final int MAX_KEYWORD_LENGTH = 10;

    /**
     * 应用启动时初始化关键词库
     */
    @PostConstruct
    public void init() {
        try {
            // 检查是否需要初始化
            String lastUpdate = redisTemplate.opsForValue().get(REDIS_KEY_LAST_UPDATE);
            if (lastUpdate == null) {
                log.info("首次启动，初始化关键词库...");
                initializeKeywords();
            } else {
                log.info("关键词库已存在，上次更新时间: {}", lastUpdate);
                // 加载到内存缓存
                loadKeywordsToRedis();
            }
        } catch (Exception e) {
            log.error("初始化关键词库失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void initializeKeywords() {
        log.info("开始初始化关键词库...");

        try {
            // 查询所有商品
            List<Goods> allGoods = goodsMapper.selectList(new LambdaQueryWrapper<>());
            log.info("共加载 {} 个商品", allGoods.size());

            // 清空MongoDB中的旧关键词
            mongoTemplate.dropCollection(GoodsKeyword.class);
            log.info("已清空旧关键词数据");

            // 提取关键词
            Map<String, Set<Integer>> keywordMap = new HashMap<>();

            for (Goods goods : allGoods) {
                // 从商品名称提取关键词
                extractKeywords(goods.getGoodsName(), keywordMap, goods.getGoodsID());

                // 从商品描述提取关键词
                if (goods.getText() != null && !goods.getText().isEmpty()) {
                    extractKeywords(goods.getText(), keywordMap, goods.getGoodsID());
                }
            }

            log.info("共提取 {} 个关键词", keywordMap.size());

            // 批量保存到MongoDB
            List<GoodsKeyword> keywords = new ArrayList<>();
            for (Map.Entry<String, Set<Integer>> entry : keywordMap.entrySet()) {
                GoodsKeyword keyword = new GoodsKeyword();
                keyword.setKeyword(entry.getKey());
                keyword.setGoodsIds(new ArrayList<>(entry.getValue()));
                keyword.setFrequency(entry.getValue().size());
                keyword.setSource("auto");
                keyword.setCategory(categorizeKeyword(entry.getKey()));
                keywords.add(keyword);
            }

            // 批量插入（分批处理，避免一次插入过多）
            int batchSize = 1000;
            for (int i = 0; i < keywords.size(); i += batchSize) {
                int end = Math.min(i + batchSize, keywords.size());
                List<GoodsKeyword> batch = keywords.subList(i, end);
                mongoTemplate.insertAll(batch);
            }

            log.info("关键词已保存到MongoDB");

            // 加载到Redis缓存
            loadKeywordsToRedis();

            // 更新最后更新时间
            redisTemplate.opsForValue().set(REDIS_KEY_LAST_UPDATE,
                String.valueOf(System.currentTimeMillis()));

            log.info("关键词库初始化完成");

        } catch (Exception e) {
            log.error("初始化关键词库失败: {}", e.getMessage(), e);
            throw new RuntimeException("初始化关键词库失败", e);
        }
    }

    @Override
    public void refreshKeywords() {
        log.info("开始刷新关键词库...");
        initializeKeywords();
    }

    @Override
    public Set<String> getGoodsTypeKeywords() {
        try {
            // 从Redis获取所有关键词
            Set<String> keywords = redisTemplate.opsForSet().members(REDIS_KEY_KEYWORDS);

            if (keywords == null || keywords.isEmpty()) {
                log.warn("Redis中无关键词缓存，从MongoDB加载...");
                loadKeywordsToRedis();
                keywords = redisTemplate.opsForSet().members(REDIS_KEY_KEYWORDS);
            }

            return keywords != null ? keywords : Collections.emptySet();

        } catch (Exception e) {
            log.error("获取关键词失败: {}", e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    /**
     * 使用Jieba分词器提取关键词
     */
    private void extractKeywords(String text, Map<String, Set<Integer>> keywordMap,
                                Integer goodsId) {
        if (text == null || text.isEmpty()) {
            return;
        }

        // 使用Jieba分词（INDEX模式，提取更多词汇）
        List<SegToken> tokens = segmenter.process(text, JiebaSegmenter.SegMode.INDEX);

        for (SegToken token : tokens) {
            String word = token.word.trim();

            // 过滤条件
            if (word.length() < MIN_KEYWORD_LENGTH ||
                word.length() > MAX_KEYWORD_LENGTH ||
                isStopWord(word) ||
                isNumber(word)) {
                continue;
            }

            // 添加到关键词Map
            keywordMap.computeIfAbsent(word, k -> new HashSet<>()).add(goodsId);
        }
    }

    /**
     * 加载关键词到Redis缓存
     */
    private void loadKeywordsToRedis() {
        try {
            // 从MongoDB查询所有关键词
            List<GoodsKeyword> keywords = mongoTemplate.findAll(GoodsKeyword.class);

            // 清空Redis旧数据
            redisTemplate.delete(REDIS_KEY_KEYWORDS);

            // 批量添加到Redis
            if (!keywords.isEmpty()) {
                String[] keywordArray = keywords.stream()
                    .map(GoodsKeyword::getKeyword)
                    .toArray(String[]::new);
                redisTemplate.opsForSet().add(REDIS_KEY_KEYWORDS, keywordArray);
            }

            log.info("已加载 {} 个关键词到Redis缓存", keywords.size());

        } catch (Exception e) {
            log.error("加载关键词到Redis失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 停用词集合（静态常量，避免重复创建）
     */
    private static final Set<String> STOP_WORDS = Set.of(
        "的", "了", "是", "在", "有", "和", "与", "或", "等", "及",
        "这", "那", "个", "些", "种", "类", "型", "号", "款", "版",
        "可", "能", "会", "要", "应", "该", "需", "须", "必",
        "很", "太", "最", "更", "还", "也", "都", "就", "才", "只",
        "不", "没", "无", "非", "未", "已", "曾", "将", "想",
        "我", "你", "他", "她", "它", "们", "自", "己", "此", "彼",
        "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
        "上", "下", "左", "右", "前", "后", "中", "内", "外", "里",
        "大", "小", "多", "少", "高", "低", "长", "短", "宽", "窄"
    );

    /**
     * 判断是否为停用词
     */
    private boolean isStopWord(String word) {
        return STOP_WORDS.contains(word);
    }

    /**
     * 判断是否为纯数字
     */
    private boolean isNumber(String word) {
        return word.matches("\\d+");
    }

    /**
     * 关键词分类
     */
    private String categorizeKeyword(String keyword) {
        // 食品类
        if (keyword.contains("肉") || keyword.contains("鱼") || keyword.contains("虾") ||
            keyword.contains("蛋") || keyword.contains("奶") || keyword.contains("茶") ||
            keyword.contains("酒") || keyword.contains("水") || keyword.contains("果")) {
            return "food";
        }

        // 电子产品类
        if (keyword.contains("手机") || keyword.contains("电脑") || keyword.contains("电") ||
            keyword.contains("机") || keyword.contains("器")) {
            return "electronics";
        }

        // 服装类
        if (keyword.contains("衣") || keyword.contains("裤") || keyword.contains("鞋") ||
            keyword.contains("帽") || keyword.contains("包")) {
            return "clothing";
        }

        // 办公用品类
        if (keyword.contains("笔") || keyword.contains("纸") || keyword.contains("本") ||
            keyword.contains("胶") || keyword.contains("钉") || keyword.contains("夹")) {
            return "office";
        }

        return "other";
    }
}

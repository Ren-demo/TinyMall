package com.mall.tinymall.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.tinymall.entity.pojo.Comment;
import com.mall.tinymall.entity.pojo.Goods;
import com.mall.tinymall.mapper.GoodsMapper;
import com.mall.tinymall.service.AiService;
import com.mall.tinymall.service.KeywordService;
import com.mall.tinymall.util.MongoDbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI智能客服服务实现
 * 使用Function Calling机制让AI真正调用数据库方法获取真实数据
 */
@Service
@Slf4j
public class AiServiceImpl implements AiService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private MongoDbUtils mongoDbUtils;

    @Autowired
    private KeywordService keywordService;

    @Value("${ai.bailian.api-key}")
    private String apiKey;

    @Value("${ai.bailian.base-url}")
    private String baseUrl;

    @Value("${ai.bailian.model}")
    private String model;

    @Value("${ai.bailian.max-tokens}")
    private Integer maxTokens;

    @Value("${ai.bailian.temperature}")
    private Double temperature;

    @Value("${ai.bailian.system-prompt}")
    private String systemPrompt;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 会话存储：sessionId -> 消息列表
     */
    private final Map<String, LinkedList<ChatMessage>> sessionStore = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY_SIZE = 50;
    
    /**
     * 最大工具调用轮次，防止无限循环
     */
    private static final int MAX_TOOL_CALL_ROUNDS = 5;
    
    /**
     * 工具类型枚举
     */
    private enum ToolType {
        SEARCH_GOODS,      // 搜索商品
        ANALYZE_QUALITY,   // 分析质量
        GET_DETAIL,        // 获取详情
        NONE               // 无需工具
    }
    
    /**
     * 意图分析关键词
     */
    private static final Set<String> SEARCH_KEYWORDS = Set.of(
        "找", "搜索", "查找", "买", "购买", "推荐", "有什么", "有没有",
        "想要", "需要", "寻找", "看看", "商品", "东西", "货", "来点", "来些",
        "查看", "看下", "看一下", "查询", "搜", "找下", "找一下"
    );

    private static final Set<String> QUALITY_KEYWORDS = Set.of(
        "质量", "评价", "评论", "怎么样", "好不好", "值得买", "推荐吗",
        "评分", "好评", "差评", "反馈", "口碑"
    );

    private static final Set<String> DETAIL_KEYWORDS = Set.of(
        "详情", "详细信息", "价格多少", "多少钱", "库存", "还有吗",
        "什么价格", "具体信息"
    );

    private static final Set<String> GOODS_ID_PATTERN = Set.of(
        "id", "ID", "编号"
    );

    /**
     * 纯问候语（不需要工具）
     */
    private static final Set<String> GREETING_KEYWORDS = Set.of(
        "你好", "您好", "在吗", "在不在", "嗨", "hi", "hello", "哈喽",
        "早上好", "下午好", "晚上好", "再见", "拜拜", "谢谢", "感谢"
    );

    @Override
    public Flux<String> chatStream(String sessionId, Integer userId, String message) {
        return Flux.create(emitter -> {
            try {
                addMessage(sessionId, "user", message);
                List<Map<String, Object>> messages = buildApiMessages(sessionId);

                log.info("开始调用AI, 消息数量: {}", messages.size());
                
                // 分析用户意图，选择需要的工具
                Set<ToolType> neededTools = analyzeIntent(message);
                log.info("根据意图分析，选择的工具: {}", neededTools);

                String response = callBailianApi(messages, 1, neededTools);
                log.info("AI响应: {}", response);

                emitter.next(response);
                addMessage(sessionId, "assistant", response);
                emitter.complete();

            } catch (Exception e) {
                log.error("AI流式对话失败: {}", e.getMessage(), e);
                emitter.next("抱歉，我暂时无法回答您的问题，请稍后再试。错误信息：" + e.getMessage());
                emitter.complete();
            }
        });
    }

    @Override
    public String chat(String sessionId, Integer userId, String message) {
        try {
            addMessage(sessionId, "user", message);
            List<Map<String, Object>> messages = buildApiMessages(sessionId);

            log.info("开始调用AI, 消息数量: {}", messages.size());
            
            // 分析用户意图，选择需要的工具
            Set<ToolType> neededTools = analyzeIntent(message);
            log.info("根据意图分析，选择的工具: {}", neededTools);

            String response = callBailianApi(messages, 1, neededTools);
            log.info("AI响应: {}", response);

            addMessage(sessionId, "assistant", response);
            return response;

        } catch (Exception e) {
            log.error("AI对话失败: {}", e.getMessage(), e);
            return "抱歉，我暂时无法回答您的问题，请稍后再试。错误信息：" + e.getMessage();
        }
    }

    /**
     * 调用阿里云百炼API（支持Function Calling，动态工具选择）
     * @param messages 消息列表
     * @param round 当前调用轮次
     * @param neededTools 需要的工具类型（仅第一轮有效）
     * @return AI最终回复
     */
    private String callBailianApi(List<Map<String, Object>> messages, int round, Set<ToolType> neededTools) throws Exception {
        if (round > MAX_TOOL_CALL_ROUNDS) {
            log.warn("达到最大工具调用轮次限制: {}", MAX_TOOL_CALL_ROUNDS);
            return "抱歉，处理您的请求时遇到了复杂情况，请尝试简化您的问题。";
        }
        
        String url = baseUrl + "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);
        
        // 动态添加工具定义（仅第一轮根据意图分析添加）
        List<Map<String, Object>> tools = buildToolsDefinition(neededTools);
        if (!tools.isEmpty()) {
            requestBody.put("tools", tools);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        log.debug("请求体: {}", jsonBody);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        log.debug("响应体: {}", response.getBody());

        // 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        
        if (choices == null || choices.isEmpty()) {
            return "抱歉，我无法理解您的请求。";
        }
        
        Map<String, Object> choice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");
        
        // 检查是否有工具调用
        if (message.containsKey("tool_calls") && message.get("tool_calls") != null) {
            List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) message.get("tool_calls");
            log.info("AI请求调用 {} 个工具", toolCalls.size());
            
            // 将assistant消息（包含tool_calls）添加到消息列表
            messages.add(message);
            
            // 处理每个工具调用
            for (Map<String, Object> toolCall : toolCalls) {
                String toolCallId = (String) toolCall.get("id");
                Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
                String functionName = (String) function.get("name");
                String functionArgs = (String) function.get("arguments");
                
                log.info("执行工具: {}({})", functionName, functionArgs);
                
                // 执行工具并获取结果
                String toolResult = executeToolCall(functionName, functionArgs);
                log.info("工具结果: {}", toolResult);
                
                // 添加工具结果消息
                Map<String, Object> toolResultMessage = new HashMap<>();
                toolResultMessage.put("role", "tool");
                toolResultMessage.put("tool_call_id", toolCallId);
                toolResultMessage.put("content", toolResult);
                messages.add(toolResultMessage);
            }
            
            // 递归调用API获取最终回复（后续轮次使用所有工具以支持链式调用）
            return callBailianApi(messages, round + 1, EnumSet.allOf(ToolType.class));
        }
        
        // 没有工具调用，返回最终回复
        return (String) message.get("content");
    }
    
    /**
     * 执行工具调用
     */
    private String executeToolCall(String functionName, String arguments) {
        try {
            Map<String, Object> args = objectMapper.readValue(arguments, Map.class);

            switch (functionName) {
                case "searchGoods":
                    String description = (String) args.get("description");
                    return searchGoods(description);
                case "analyzeGoodsQuality":
                    Integer goodsIdForAnalyze = parseGoodsId(args.get("goodsId"));
                    return analyzeGoodsQuality(goodsIdForAnalyze);
                case "getGoodsDetail":
                    Integer goodsIdForDetail = parseGoodsId(args.get("goodsId"));
                    return getGoodsDetail(goodsIdForDetail);
                default:
                    return "未知工具: " + functionName;
            }
        } catch (Exception e) {
            log.error("执行工具调用失败: {} - {}", functionName, e.getMessage());
            return "工具执行失败: " + e.getMessage();
        }
    }

    /**
     * 解析商品ID
     */
    private Integer parseGoodsId(Object goodsIdObj) {
        if (goodsIdObj == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        if (goodsIdObj instanceof Number) {
            return ((Number) goodsIdObj).intValue();
        } else if (goodsIdObj instanceof String) {
            try {
                return Integer.parseInt((String) goodsIdObj);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("无效的商品ID: " + goodsIdObj);
            }
        } else {
            throw new IllegalArgumentException("商品ID类型错误: " + goodsIdObj.getClass());
        }
    }
    
    /**
     * 分析用户消息意图，判断需要哪些工具
     * @param userMessage 用户消息
     * @return 需要的工具类型集合
     */
    private Set<ToolType> analyzeIntent(String userMessage) {
        Set<ToolType> neededTools = new HashSet<>();
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return neededTools;
        }
        
        String message = userMessage.toLowerCase().trim();

        // 检查是否是纯问候语（不需要工具）
        if (GREETING_KEYWORDS.stream().anyMatch(message::contains)) {
            // 如果消息很短且只包含问候语，不调用工具
            if (message.length() <= 10 && !containsGoodsKeyword(message)) {
                log.info("意图分析结果: 用户消息='{}', 纯问候语，不需要工具", userMessage);
                return neededTools;
            }
        }

        // 检查是否包含商品ID（数字）
        boolean hasGoodsId = message.matches(".*\\d+.*") ||
                GOODS_ID_PATTERN.stream().anyMatch(message::contains);

        // 检查是否包含商品类型关键词（动态从Redis获取）
        boolean hasGoodsType = containsGoodsKeyword(message);

        // 检查搜索意图
        if (SEARCH_KEYWORDS.stream().anyMatch(message::contains)) {
            neededTools.add(ToolType.SEARCH_GOODS);
        }

        // 如果消息包含商品类型关键词，默认为搜索意图
        if (hasGoodsType && neededTools.isEmpty()) {
            neededTools.add(ToolType.SEARCH_GOODS);
        }

        // 检查质量分析意图（通常需要商品ID）
        if (QUALITY_KEYWORDS.stream().anyMatch(message::contains)) {
            if (hasGoodsId) {
                neededTools.add(ToolType.ANALYZE_QUALITY);
            } else {
                // 没有ID时，可能需要先搜索
                neededTools.add(ToolType.SEARCH_GOODS);
            }
        }

        // 检查详情查询意图
        if (DETAIL_KEYWORDS.stream().anyMatch(message::contains)) {
            if (hasGoodsId) {
                neededTools.add(ToolType.GET_DETAIL);
            } else {
                neededTools.add(ToolType.SEARCH_GOODS);
            }
        }

        // 如果消息中直接包含商品ID但没有明确意图，默认提供详情
        if (hasGoodsId && neededTools.isEmpty()) {
            neededTools.add(ToolType.GET_DETAIL);
        }

        log.info("意图分析结果: 用户消息='{}', 需要的工具={}", userMessage, neededTools);
        return neededTools;
    }

    /**
     * 检查消息是否包含商品关键词（动态获取）
     */
    private boolean containsGoodsKeyword(String message) {
        try {
            Set<String> keywords = keywordService.getGoodsTypeKeywords();
            return keywords.stream().anyMatch(message::contains);
        } catch (Exception e) {
            log.error("获取动态关键词失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 根据意图分析结果构建工具定义
     * @param neededTools 需要的工具类型
     * @return 工具定义列表
     */
    private List<Map<String, Object>> buildToolsDefinition(Set<ToolType> neededTools) {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        if (neededTools.contains(ToolType.SEARCH_GOODS)) {
            tools.add(buildSearchGoodsTool());
        }
        
        if (neededTools.contains(ToolType.ANALYZE_QUALITY)) {
            tools.add(buildAnalyzeQualityTool());
        }
        
        if (neededTools.contains(ToolType.GET_DETAIL)) {
            tools.add(buildGetGoodsDetailTool());
        }
        
        return tools;
    }
    
    /**
     * 构建searchGoods工具定义
     */
    private Map<String, Object> buildSearchGoodsTool() {
        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");
        Map<String, Object> func = new HashMap<>();
        func.put("name", "searchGoods");
        func.put("description", "根据用户描述搜索商城中的商品。返回匹配的商品列表，包含商品ID、名称、价格、库存和描述。");
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        Map<String, Object> props = new HashMap<>();
        Map<String, Object> descriptionProp = new HashMap<>();
        descriptionProp.put("type", "string");
        descriptionProp.put("description", "商品描述关键词");
        props.put("description", descriptionProp);
        params.put("properties", props);
        params.put("required", List.of("description"));
        func.put("parameters", params);
        tool.put("function", func);
        return tool;
    }
    
    /**
     * 构建analyzeGoodsQuality工具定义
     */
    private Map<String, Object> buildAnalyzeQualityTool() {
        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");
        Map<String, Object> func = new HashMap<>();
        func.put("name", "analyzeGoodsQuality");
        func.put("description", "分析商品质量，基于用户评价给出购买建议。需要商品ID。");
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        Map<String, Object> props = new HashMap<>();
        Map<String, Object> goodsIdProp = new HashMap<>();
        goodsIdProp.put("type", "integer");
        goodsIdProp.put("description", "商品ID");
        props.put("goodsId", goodsIdProp);
        params.put("properties", props);
        params.put("required", List.of("goodsId"));
        func.put("parameters", params);
        tool.put("function", func);
        return tool;
    }
    
    /**
     * 构建getGoodsDetail工具定义
     */
    private Map<String, Object> buildGetGoodsDetailTool() {
        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");
        Map<String, Object> func = new HashMap<>();
        func.put("name", "getGoodsDetail");
        func.put("description", "获取商品的详细信息，包括名称、价格、库存和描述。需要商品ID。");
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        Map<String, Object> props = new HashMap<>();
        Map<String, Object> goodsIdProp = new HashMap<>();
        goodsIdProp.put("type", "integer");
        goodsIdProp.put("description", "商品ID");
        props.put("goodsId", goodsIdProp);
        params.put("properties", props);
        params.put("required", List.of("goodsId"));
        func.put("parameters", params);
        tool.put("function", func);
        return tool;
    }

    @Override
    public void clearSession(String sessionId) {
        sessionStore.remove(sessionId);
    }

    @Override
    public String searchGoods(String description) {
        log.info("AI调用工具: searchGoods, 参数: {}", description);
        
        List<Goods> allGoods = goodsMapper.selectList(new LambdaQueryWrapper<>());
        List<Goods> results = allGoods.stream()
                .filter(goods -> calculateRelevanceScore(goods, description) > 0)
                .sorted((a, b) -> Double.compare(
                        calculateRelevanceScore(b, description), 
                        calculateRelevanceScore(a, description)))
                .limit(5)
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            return "未找到匹配的商品";
        }

        StringBuilder sb = new StringBuilder("找到以下相关商品：\n");
        for (int i = 0; i < results.size(); i++) {
            Goods g = results.get(i);
            sb.append(i + 1).append(". ")
              .append(g.getGoodsName())
              .append(" - 价格: ").append(g.getPrice()).append("元")
              .append(", 库存: ").append(g.getCount())
              .append(", 描述: ").append(g.getText())
              .append("\n");
        }
        return sb.toString();
    }

    @Override
    public String analyzeGoodsQuality(Integer goodsId) {
        log.info("AI调用工具: analyzeGoodsQuality, 参数: {}", goodsId);
        
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            return "未找到商品ID为" + goodsId + "的商品";
        }

        List<Comment> comments = mongoDbUtils.findCommentsByGoodsId(goodsId);
        StringBuilder sb = new StringBuilder();
        sb.append("商品：").append(goods.getGoodsName()).append("\n");
        sb.append("价格：").append(goods.getPrice()).append("元\n");
        
        if (comments.isEmpty()) {
            sb.append("该商品暂无用户评价。\n建议：参考商品描述判断是否购买。");
        } else {
            double avgStar = comments.stream().mapToInt(Comment::getStar).average().orElse(0.0);
            long positiveCount = comments.stream().filter(c -> c.getStar() >= 4).count();
            double positiveRate = (double) positiveCount / comments.size() * 100;
            
            sb.append("评价总数：").append(comments.size()).append("条\n");
            sb.append("平均评分：").append(String.format("%.1f", avgStar)).append("星/5星\n");
            sb.append("好评率：").append(String.format("%.1f%%", positiveRate)).append("\n");
            
            sb.append("\n代表性评价：\n");
            comments.stream().filter(c -> c.getStar() >= 4).limit(2)
                    .forEach(c -> sb.append("好评：").append(c.getContent()).append("\n"));
            comments.stream().filter(c -> c.getStar() <= 2).limit(1)
                    .forEach(c -> sb.append("差评：").append(c.getContent()).append("\n"));
            
            sb.append("\n购买建议：");
            if (avgStar >= 4.0 && positiveRate >= 80) {
                sb.append("该商品用户评价较好，推荐购买。");
            } else if (avgStar >= 3.5) {
                sb.append("该商品整体评价尚可，可以考虑购买。");
            } else {
                sb.append("该商品评价一般，建议谨慎购买。");
            }
        }
        return sb.toString();
    }

    @Override
    public String getGoodsDetail(Integer goodsId) {
        log.info("AI调用工具: getGoodsDetail, 参数: {}", goodsId);
        
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            return "未找到商品ID为" + goodsId + "的商品";
        }
        return String.format("商品详情：\n名称：%s\n价格：%.2f元\n库存：%d\n描述：%s",
                goods.getGoodsName(), goods.getPrice(), goods.getCount(), goods.getText());
    }

    /**
     * 添加消息到会话历史
     */
    private void addMessage(String sessionId, String role, String content) {
        sessionStore.computeIfAbsent(sessionId, k -> new LinkedList<>());
        LinkedList<ChatMessage> history = sessionStore.get(sessionId);
        history.add(new ChatMessage(role, content));
        
        while (history.size() > MAX_HISTORY_SIZE) {
            Iterator<ChatMessage> iterator = history.iterator();
            while (iterator.hasNext()) {
                ChatMessage msg = iterator.next();
                if (!"system".equals(msg.role())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    /**
     * 构建API请求消息列表
     */
    private List<Map<String, Object>> buildApiMessages(String sessionId) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 添加系统消息
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
        
        // 添加历史消息
        LinkedList<ChatMessage> history = sessionStore.getOrDefault(sessionId, new LinkedList<>());
        int start = Math.max(0, history.size() - 20);
        for (int i = start; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            Map<String, Object> historyMessage = new HashMap<>();
            historyMessage.put("role", msg.role());
            historyMessage.put("content", msg.content());
            messages.add(historyMessage);
        }
        
        return messages;
    }

    /**
     * 计算商品与描述的相关性得分
     * 优化：提高相关性阈值，减少无关商品匹配
     */
    private double calculateRelevanceScore(Goods goods, String description) {
        if (description == null || description.isEmpty()) return 0;

        String lowerDesc = description.toLowerCase();
        String goodsName = goods.getGoodsName() != null ? goods.getGoodsName().toLowerCase() : "";
        String goodsText = goods.getText() != null ? goods.getText().toLowerCase() : "";

        double score = 0;
        boolean hasDirectMatch = false; // 是否有直接匹配

        for (String word : lowerDesc.split("[\\s,，。！？、]+")) {
            if (word.isEmpty()) continue;

            // 商品名称完全包含关键词（高权重）
            if (goodsName.contains(word)) {
                score += 15;
                hasDirectMatch = true;
            }
            // 商品描述包含关键词（中等权重）
            else if (goodsText.contains(word)) {
                score += 5;
            }
        }

        // 如果没有直接匹配，大幅降低得分
        if (!hasDirectMatch) {
            score = score * 0.3;
        }

        // 评论评分加成（降低权重）
        List<Comment> comments = mongoDbUtils.findCommentsByGoodsId(goods.getGoodsID());
        if (!comments.isEmpty()) {
            score += comments.stream().mapToInt(Comment::getStar).average().orElse(0.0) * 0.2;
        }

        return score;
    }

    /**
     * 聊天消息内部类
     */
    private record ChatMessage(String role, String content) {}
}

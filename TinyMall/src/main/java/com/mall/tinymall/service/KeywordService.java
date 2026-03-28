package com.mall.tinymall.service;

import java.util.Set;

/**
 * 关键词服务接口
 * 负责从商品数据中提取关键词并缓存
 */
public interface KeywordService {

    /**
     * 初始化关键词库
     * 从数据库加载所有商品，提取关键词并存储到MongoDB和Redis
     */
    void initializeKeywords();

    /**
     * 刷新关键词库
     * 重新提取所有商品的关键词
     */
    void refreshKeywords();

    /**
     * 获取所有商品类型关键词
     * 用于AI意图识别
     * @return 关键词集合
     */
    Set<String> getGoodsTypeKeywords();
}

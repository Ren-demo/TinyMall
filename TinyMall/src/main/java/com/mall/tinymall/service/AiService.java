package com.mall.tinymall.service;

import reactor.core.publisher.Flux;

/**
 * AI智能客服服务接口
 */
public interface AiService {

    /**
     * 与AI客服进行对话（流式返回）
     * @param sessionId 会话ID（用于会话隔离）
     * @param userId 用户ID
     * @param message 用户消息
     * @return AI回复流
     */
    Flux<String> chatStream(String sessionId, Integer userId, String message);

    /**
     * 与AI客服进行对话（非流式）
     * @param sessionId 会话ID（用于会话隔离）
     * @param userId 用户ID
     * @param message 用户消息
     * @return AI回复
     */
    String chat(String sessionId, Integer userId, String message);

    /**
     * 清除会话历史
     * @param sessionId 会话ID
     */
    void clearSession(String sessionId);

    /**
     * 根据描述搜索商品
     * @param description 商品描述
     * @return 搜索结果
     */
    String searchGoods(String description);

    /**
     * 分析商品质量（基于评论）
     * @param goodsId 商品ID
     * @return 质量分析结果
     */
    String analyzeGoodsQuality(Integer goodsId);

    /**
     * 获取商品详情
     * @param goodsId 商品ID
     * @return 商品详情
     */
    String getGoodsDetail(Integer goodsId);
}

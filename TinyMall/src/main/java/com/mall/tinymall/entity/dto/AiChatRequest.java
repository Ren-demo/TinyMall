package com.mall.tinymall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI客服聊天请求DTO
 */
@Data
@Schema(description = "AI客服聊天请求")
public class AiChatRequest {

    @Schema(description = "会话ID，用于会话隔离，不传则自动生成")
    private String sessionId;

    @Schema(description = "用户ID", required = true)
    private Integer userId;

    @Schema(description = "用户消息", required = true)
    private String message;
}

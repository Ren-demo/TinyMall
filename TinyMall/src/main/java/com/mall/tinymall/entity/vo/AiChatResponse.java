package com.mall.tinymall.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI客服聊天响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI客服聊天响应")
public class AiChatResponse {

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "AI回复内容")
    private String reply;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "错误信息")
    private String errorMessage;
}

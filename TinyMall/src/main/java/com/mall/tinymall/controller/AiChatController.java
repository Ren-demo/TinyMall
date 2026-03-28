package com.mall.tinymall.controller;

import com.mall.tinymall.entity.dto.AiChatRequest;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.vo.AiChatResponse;
import com.mall.tinymall.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * AI智能客服控制器
 */
@RestController
@RequestMapping("/ai")
@Tag(name = "智能客服", description = "AI智能客服相关接口")
@Slf4j
public class AiChatController {

    @Autowired
    private AiService aiService;

    /**
     * 与AI客服进行对话（流式返回）
     */
    @PostMapping(value = "/chat/stream", produces = "text/event-stream;charset=UTF-8")
    @Operation(summary = "AI客服对话（流式）", description = "与AI智能客服进行对话，使用SSE流式返回")
    public Flux<ServerSentEvent<String>> chatStream(@RequestBody AiChatRequest request) {
        String sessionId = request.getSessionId() != null && !request.getSessionId().isEmpty() 
                ? request.getSessionId() 
                : UUID.randomUUID().toString();

        return aiService.chatStream(sessionId, request.getUserId(), request.getMessage())
                .map(content -> ServerSentEvent.<String>builder()
                        .data(new String(content.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))
                        .build())
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder().event("done").data(sessionId).build()
                ))
                .onErrorResume(e -> {
                    log.error("AI流式对话失败: {}", e.getMessage(), e);
                    return Flux.just(
                            ServerSentEvent.<String>builder().event("error").data("AI对话失败: " + e.getMessage()).build()
                    );
                });
    }

    /**
     * 与AI客服进行对话（非流式）
     */
    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "AI客服对话", description = "与AI智能客服进行对话")
    public Result chat(@RequestBody AiChatRequest request) {
        try {
            String sessionId = request.getSessionId() != null && !request.getSessionId().isEmpty() 
                    ? request.getSessionId() 
                    : UUID.randomUUID().toString();

            String reply = aiService.chat(sessionId, request.getUserId(), request.getMessage());

            return Result.success(AiChatResponse.builder()
                    .sessionId(sessionId)
                    .reply(reply)
                    .success(true)
                    .build());
        } catch (Exception e) {
            log.error("AI对话失败: {}", e.getMessage(), e);
            return Result.error("AI对话失败: " + e.getMessage());
        }
    }

    /**
     * 清除会话历史
     */
    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "清除会话历史", description = "清除指定会话的聊天历史")
    public Result clearSession(@Parameter(description = "会话ID") @PathVariable String sessionId) {
        try {
            log.info("清除ai会话：{}", sessionId);
            aiService.clearSession(sessionId);
            return Result.success("会话已清除");
        } catch (Exception e) {
            log.error("清除会话失败: {}", e.getMessage(), e);
            return Result.error("清除会话失败: " + e.getMessage());
        }
    }
}

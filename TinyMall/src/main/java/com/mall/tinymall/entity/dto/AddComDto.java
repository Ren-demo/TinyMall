package com.mall.tinymall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @date 2026/3/18 20:06
 * @description TODO: 添加评论参数传递
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户添加评论参数传递")
public class AddComDto {
    @Schema(description = "订单号")
    private Integer orderId;
    @Schema(description = "所有的图片url（服务器内的地址）")
    private List<String> imgUrl;
    @Schema(description = "评论等级 1-5星 整数")
    private Integer star;
    @Schema(description = "评论内容")
    private String content;
}

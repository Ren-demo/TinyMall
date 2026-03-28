package com.mall.tinymall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @date 2026/3/18 21:32
 * @description TODO: 修改评论信息传递
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "修改评论传递对象")
public class UpdateComDto {
    @Schema(description = "评论id")
    private String id;
    @Schema(description = "评价等级 1-5 整形")
    private Integer star;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "评论照片url")
    private List<String> images;
}

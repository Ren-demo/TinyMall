package com.mall.tinymall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date 2026/1/20 16:47
 * @description TODO: 商品加入购物车信息传递
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "添加商品到购物车的信息")
public class AddCartDto {
    @Schema(description = "商品id")
    private Integer goodsId;
    @Schema(description = "用户id")
    private Integer userId;
    @Schema(description = "商品数量")
    private Integer count;
}

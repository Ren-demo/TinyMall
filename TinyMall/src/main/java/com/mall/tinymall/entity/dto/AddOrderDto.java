package com.mall.tinymall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date 2026/1/21 10:52
 * @description TODO: 用户立即购买提交订单信息传递
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户提交订单传递信息")
public class AddOrderDto {
    @Schema(description = "商品id")
    private Integer goodsId;
    @Schema(description = "购买数量")
    private Integer count;
    @Schema(description = "用户id")
    private Integer userId;
    @Schema(description = "用户地址")
    private String address;
}

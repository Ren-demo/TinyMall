package com.mall.tinymall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date 2026/1/22 8:03
 * @description TODO: 供应商新增商品管理
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "供货商新增商品的信息传递")
public class AddGoodsDto {
    @Schema(description = "商品名字")
    private String goodsName;
    @Schema(description = "供应商id")
    private Integer storeID;
    @Schema(description = "商品描述")
    private String text;
    @Schema(description = "初始添加的数量，默认0")
    private Integer count=0;
    @Schema(description = "商品单价")
    private float price;
}

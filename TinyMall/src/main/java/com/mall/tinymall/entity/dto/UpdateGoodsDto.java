package com.mall.tinymall.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @date 2026/1/22 9:34
 * @description TODO: 供应商修改商品信息传递
 */
@Data
@AllArgsConstructor
public class UpdateGoodsDto {
    private Integer goodsId;
    private String goodsName;
    private String text;
    private float price;
    private Integer count;
    private boolean isAdd;
}

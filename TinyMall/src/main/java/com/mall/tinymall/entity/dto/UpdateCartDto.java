package com.mall.tinymall.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @date 2026/1/20 17:44
 * @description TODO: 修改购物车信息传递
 */
@Data
@AllArgsConstructor
public class UpdateCartDto {
    private Integer shoppingCartId;
    private Integer goodsCount;
    private boolean isDelete;
}

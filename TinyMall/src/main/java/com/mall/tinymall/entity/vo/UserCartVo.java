package com.mall.tinymall.entity.vo;

import com.mall.tinymall.util.DefaultPic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date 2026/1/21 9:18
 * @description TODO: 用户购物车参数返回
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCartVo {
    private Integer shoppingCartId;
    private Integer userId;
    private Integer storeId;
    private Integer goodsId;
    private Integer count;
    private float totalPrice;
    private String goodsName;
    private float price;
    private String goodsImg= DefaultPic.DEFAULT_GOODS_PICTURE.getUrl();
}

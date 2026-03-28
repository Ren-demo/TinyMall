package com.mall.tinymall.entity.vo;

import com.mall.tinymall.util.DefaultPic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date 2026/1/21 13:14
 * @description TODO: 查询商品列表
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ListGoodsVo {
    private Integer goodsID;
    private String goodsName;
    private Integer storeID;
    private String storeName;
    private String text;
    private Integer count;
    private float price;
    private String picture= DefaultPic.DEFAULT_GOODS_PICTURE.getUrl();
}

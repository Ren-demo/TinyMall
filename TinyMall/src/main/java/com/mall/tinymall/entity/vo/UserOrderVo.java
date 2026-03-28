package com.mall.tinymall.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mall.tinymall.entity.pojo.Goods;
import com.mall.tinymall.util.DefaultPic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @date 2026/1/21 9:18
 * @description TODO: 用户订单参数返回
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderVo {
    private Integer orderId;
    private String userName;
    private Integer storeId;
    private String storeName;
    private Integer goodsId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    private Integer State;
    private Integer count;
    private String picture= DefaultPic.DEFAULT_GOODS_PICTURE.getUrl();
    private float price;
    private String address;
}

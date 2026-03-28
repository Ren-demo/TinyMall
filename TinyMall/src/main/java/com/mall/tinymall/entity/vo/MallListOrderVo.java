package com.mall.tinymall.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @date 2026/1/21 15:11
 * @description TODO: 商家查询所有订单，信息返回
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MallListOrderVo {
    private Integer orderId;
    private Integer userId;
    private String userName;
    private Integer storeId;
    private String storeName;
    private Integer goodsId;
    private String goodsName;
    private Integer count;
    private float price;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    private int state;
    private String address;
}

package com.mall.tinymall.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @date 2026/1/22 9:07
 * @description TODO: 供应商查看订单参数返回
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreListOrderVo {
    private Integer orderId;
    private Integer userId;
    private Integer goodsId;
    private String goodsName;
    private String storeName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    private float price;
    private Integer count;
    private Integer state;
    private String address;
}

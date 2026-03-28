package com.mall.tinymall.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("orders")
public class Order {
    @TableId(value = "OrderID", type = IdType.AUTO)
    private Integer orderId;
    @TableField("UserID")
    private Integer userId;
    @TableField("StoreID")
    private Integer storeId;
    @TableField("GoodsID")
    private Integer goodsId;
    @TableField("Count")
    private Integer count;
    @TableField("Price")
    private float price;
    @TableField("Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    @TableField("State")
    private Integer state;
    @TableField("address")
    private String address;
}

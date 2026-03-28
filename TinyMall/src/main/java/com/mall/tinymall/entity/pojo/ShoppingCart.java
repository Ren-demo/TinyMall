package com.mall.tinymall.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("shoppingcart")
public class ShoppingCart {
    @TableId(value="ShoppingCartID", type= IdType.AUTO)
    private Integer shoppingCartId;
    @TableField("UserID")
    private Integer userID;
    @TableField("StoreID")
    private Integer storeID;
    @TableField("GoodsID")
    private Integer goodsID;
    @TableField("Count")
    private Integer count;
    @TableField("TotalPrice")
    private float totalPrice;
}

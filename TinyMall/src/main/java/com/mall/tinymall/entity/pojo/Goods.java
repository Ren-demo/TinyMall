package com.mall.tinymall.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.tinymall.util.DefaultPic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("goods")
public class Goods {
    @TableId(value = "GoodsID", type = IdType.AUTO)
    private Integer goodsID;
    @TableField("GoodsName")
    private String goodsName;
    @TableField("StoreID")
    private Integer storeID;
    @TableField("Text")
    private String text;
    @TableField("Count")
    private Integer count;
    @TableField("Price")
    private float price;
    @TableField("Picture")
    private String picture= DefaultPic.DEFAULT_GOODS_PICTURE.getUrl();
}

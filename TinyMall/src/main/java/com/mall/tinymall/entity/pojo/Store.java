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
@TableName("store")
public class Store {
    @TableId(value="StoreID", type= IdType.AUTO)
    private Integer storeId;
    @TableField("StoreName")
    private String storeName;
    @TableField("StoreEmail")
    private String storeEmail;
//    @TableField("StorePwd")
//    private String storePwd;
    @TableField("Picture")
    private String picture= DefaultPic.DEFAULT_STORE_PICTURE.getUrl();
}

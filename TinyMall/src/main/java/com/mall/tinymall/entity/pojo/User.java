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
@TableName("user")
public class User {
    @TableId(value="UserID", type= IdType.AUTO)
    private Integer userId;
    @TableField("UserName")
    private String userName;
    @TableField("UserPwd")
    private String userPwd;
    @TableField("UserEmail")
    private String userEmail;
    @TableField("UserPicture")
    private String userPicture= DefaultPic.DEFAULT_USER_PROFILE.getUrl();
    @TableField("address")
    private String address;
}

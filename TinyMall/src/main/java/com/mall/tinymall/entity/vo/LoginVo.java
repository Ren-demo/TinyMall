package com.mall.tinymall.entity.vo;

import com.mall.tinymall.util.DefaultPic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date 2026/1/20 8:40
 * @description TODO: 登录/注册返回参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginVo {
    private Integer userId;
    private String userName;
    private String userEmail;
    private String userPicture=DefaultPic.DEFAULT_USER_PROFILE.getUrl();
    private String address;
}

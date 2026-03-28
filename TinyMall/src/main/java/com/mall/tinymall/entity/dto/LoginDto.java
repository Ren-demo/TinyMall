package com.mall.tinymall.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @date 2026/1/20 8:24
 * @description TODO: 登录参数传递
 */
@Data
@AllArgsConstructor
public class LoginDto {
    private String userEmail;
    private String userPwd;
}

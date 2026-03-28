package com.mall.tinymall.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date 2026/1/20 14:59
 * @description TODO: 修改密码参数传递
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSecretDto {
    private String userPwd;
    private String userEmail;
    private String velify;
}

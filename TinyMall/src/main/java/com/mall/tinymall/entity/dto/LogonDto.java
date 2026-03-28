package com.mall.tinymall.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @date 2026/1/20 9:35
 * @description TODO: 注册信息传递
 */
@Data
@AllArgsConstructor
public class LogonDto {
    private String userName;
    private String userPwd;
    private String userEmail;
    private String velify;

    public static boolean isValid(LogonDto logonDto){
        if(logonDto.getUserName()==null || logonDto.getUserName().isBlank())
            return false;
        else if(logonDto.getUserPwd()==null || logonDto.getUserPwd().isBlank())
            return false;
        else if(logonDto.getVelify()==null || logonDto.getVelify().isBlank())
            return false;
        else if(logonDto.getUserEmail()==null || logonDto.getUserEmail().isBlank())
            return false;
        return true;
    }

}

package com.mall.tinymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.tinymall.entity.dto.LoginDto;
import com.mall.tinymall.entity.dto.LogonDto;
import com.mall.tinymall.entity.dto.UpdateSecretDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.pojo.User;
import com.mall.tinymall.entity.vo.LoginVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {
    LoginVo login(LoginDto loginDto, HttpServletResponse response);

    Result sendVerify(String email);

    Result logon(LogonDto logonDto, HttpServletResponse response);

    Result updateSecret(UpdateSecretDto updateSecretDto);

    Result updateUserInfo(User user);

    Result picture(MultipartFile file, Integer userId);
}

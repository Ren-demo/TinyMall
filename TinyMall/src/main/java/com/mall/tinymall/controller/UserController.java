package com.mall.tinymall.controller;

import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.pojo.User;
import com.mall.tinymall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @date 2026/1/20 15:39
 * @description TODO: 用户对个人信息和数据进行操作
 */
@Slf4j
@RestController
@RequestMapping("/tinymall/user")
@Tag(name="用户对个人信息和数据进行操作")
public class UserController {

    @Autowired
    private UserService userService;

    //用户修改个人信息
    @PutMapping("/update")
    @Operation(summary = "用户修改个人信息")
    public Result updateUserInfo(@RequestBody User user){
        return userService.updateUserInfo(user);
    }

    //用户设置/更换个人头像
    @PostMapping("/picture/{userId}")
    @Operation(summary = "用户设置/更换个人头像")
    public Result picture(MultipartFile file, @PathVariable Integer userId){
        return userService.picture(file, userId);
    }

}

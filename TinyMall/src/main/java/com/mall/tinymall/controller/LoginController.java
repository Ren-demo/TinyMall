package com.mall.tinymall.controller;

import com.mall.tinymall.entity.dto.LoginDto;
import com.mall.tinymall.entity.dto.LogonDto;
import com.mall.tinymall.entity.dto.UpdateSecretDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.vo.LoginVo;
import com.mall.tinymall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * @date 2026/1/20 8:19
 * @description TODO: 注册/登录控制层
 */
@Slf4j
@RestController
@RequestMapping("/tinymall/login")
@Tag(name="注册/登录控制层")
public class LoginController {

    @Autowired
    private UserService userService;
    //登录
    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result Login(@RequestBody LoginDto loginDto, HttpServletResponse response){
        LoginVo loginVo = userService.login(loginDto, response);
        if(loginVo == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return Result.error("登录失败");
        }
        return Result.success(loginVo);
    }

    //注册
    @PostMapping("/logon")
    @Operation(summary = "注册")
    public Result Logon(@RequestBody LogonDto logonDto, HttpServletResponse response){
        return userService.logon(logonDto, response);
    }

    //邮箱验证码
    @GetMapping("/logon/verify/{email}")
    @Operation(summary = "发送验证码")
    public Result LogonVerify(@PathVariable String email){
        return userService.sendVerify(email);
    }

    //修改密码
    @PutMapping("/updateSecret")
    @Operation(summary = "忘记密码")
    public Result updateSecret(@RequestBody UpdateSecretDto updateSecretDto){
        return userService.updateSecret(updateSecretDto);
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        // 返回204 No Content状态码，表示请求成功但无内容返回
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

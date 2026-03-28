package com.mall.tinymall.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.tinymall.entity.dto.LoginDto;
import com.mall.tinymall.entity.dto.LogonDto;
import com.mall.tinymall.entity.dto.UpdateSecretDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.pojo.User;
import com.mall.tinymall.entity.vo.LoginVo;
import com.mall.tinymall.mapper.UserMapper;
import com.mall.tinymall.service.UserService;
import com.mall.tinymall.util.DefaultPic;
import com.mall.tinymall.util.JwtUtils;
import com.mall.tinymall.util.MinioUtil;
import com.mall.tinymall.util.MsgPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;


/**
 * @date 2026/1/20 8:32
 * @description TODO: 用户业务层
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserMapper userMapper;


    @Override
    public LoginVo login(LoginDto loginDto, HttpServletResponse response) {
        //TODO 这里要改为根据email查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserEmail, loginDto.getUserEmail());
        User data = getOne(wrapper);

        LoginVo loginVo = new LoginVo();
        if(data.getUserPwd().equals(encrypt(loginDto.getUserPwd()))){
            Map<String, Object> chaims = new HashMap<>();
            chaims.put("userId", data.getUserId());
            chaims.put("userName", data.getUserName());
            chaims.put("userEmail", data.getUserEmail());
            String token = JwtUtils.generateToken(chaims);
            response.setHeader("token", token);
            BeanUtils.copyProperties(data, loginVo);
            return loginVo;
        }
        return null;
    }

    private String encrypt(String pwd){
        return DigestUtils.md5DigestAsHex(pwd.getBytes());
    }

    // 发件人邮箱（从配置文件读取）
    @Value("${spring.mail.username}")
    private String fromEmail;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private MsgPublisher msgPublisher;

    //注册发送验证码
    @Override
    public Result sendVerify(String email) {
        try {
            Random random = new Random();
            // 生成100000~999999之间的随机数
            int code = random.nextInt(900000) + 100000;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【TinyShop】邮箱注册验证码");
            message.setText("尊敬的用户您好：\n\n" +
                    "您正在进行TinyShop账号注册，本次验证码为：" + code + "\n" +
                    "验证码有效期为5分钟，请在规定时间内完成验证。\n\n" +
                    "若非本人操作，请忽略此邮件，感谢您的理解与支持！\n\n" +
                    "TinyShop 运营团队");
            redisTemplate.opsForValue().set(email, String.valueOf(code));
            javaMailSender.send(message);
            msgPublisher.delayDeleteVelify(email);
            return Result.success();
        } catch (MailException e) {
            return Result.error("邮件发送失败");
        }
    }

    //注册
    @Override
    public Result logon(LogonDto logonDto, HttpServletResponse response) {
        log.info("{}", logonDto);
        if (!LogonDto.isValid(logonDto)) return Result.error("有字段为空");
        if (!redisTemplate.hasKey(logonDto.getUserEmail())) return Result.error("邮箱错误");
        if (!Objects.equals(redisTemplate.opsForValue().get(logonDto.getUserEmail()), logonDto.getVelify()))
            return Result.error("验证码错误");
        redisTemplate.delete(logonDto.getUserEmail());
        Integer userId = userMapper.isUserLive(logonDto.getUserEmail());
        if(userId!=null) return Result.error("该邮箱已被注册，无法重复注册");
        User user = new User();
        LoginVo loginVo = new LoginVo();
        BeanUtils.copyProperties(logonDto, user);
        user.setUserPwd(encrypt(logonDto.getUserPwd()));
        user.setUserPicture(DefaultPic.DEFAULT_USER_PROFILE.getUrl());
        user.setAddress("(暂无)");
        log.info("{}", user);
        this.save(user);
        BeanUtils.copyProperties(user, loginVo);
        Map<String, Object> chaims = new HashMap<>();
        chaims.put("userId", user.getUserId());
        chaims.put("userName", user.getUserName());
        chaims.put("userEmail", user.getUserEmail());
        response.setHeader("token", JwtUtils.generateToken(chaims));
        return Result.success(loginVo);
    }

    //忘记密码功能
    @Override
    public Result updateSecret(UpdateSecretDto updateSecretDto) {
        String email = updateSecretDto.getUserEmail();
        if(!redisTemplate.hasKey(email)) return Result.error("邮箱号无效");
        if (!Objects.equals(redisTemplate.opsForValue().get(updateSecretDto.getUserEmail()), updateSecretDto.getVelify()))
            return Result.error("验证码错误");
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(User::getUserPwd, encrypt(updateSecretDto.getUserPwd()));
        wrapper.eq(User::getUserEmail, updateSecretDto.getUserEmail());
        this.update(wrapper);
        return Result.success();
    }

    //用户修改个人信息
    @Override
    public Result updateUserInfo(User user) {
        log.info("{}", user);
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getUserId, user.getUserId());
        wrapper.set(User::getUserPwd, encrypt(user.getUserPwd()));
        wrapper.set(User::getUserEmail, user.getUserEmail());
        wrapper.set(User::getAddress, user.getAddress());
        update(wrapper);
        return Result.success();
    }

    @Autowired
    private MinioUtil minioUtil;

    @Override
    @Transactional
    public Result picture(MultipartFile file, Integer userId) {
        if(!minioUtil.isImage(Objects.requireNonNull(file.getOriginalFilename()))){
            return Result.error("文件格式有误");
        }
        String url = userMapper.userPictureUrl(userId);
        if(!file.isEmpty()) url = minioUtil.uploadFile(file, 1);
        else if(Objects.equals(url, DefaultPic.DEFAULT_USER_PROFILE.getUrl())){
            url = minioUtil.uploadFile(file, 1);
        }else{
            minioUtil.deleteFile(url);
            url = minioUtil.uploadFile(file, 1);
        }
        if(url==null) return Result.error("上传失败");
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.set("UserPicture", url);
        wrapper.eq("UserID", userId);
        userMapper.update(wrapper);
        return Result.success(url);
    }

}

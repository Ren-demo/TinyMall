package com.mall.tinymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.tinymall.entity.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select UserPicture from user where UserID=#{id}")
    String userPictureUrl(Integer id);

    @Select("select UserName from user where UserID=#{userid}")
    String getUserName(Integer userId);

    @Select("select UserEmail from user where UserID=#{userid}")
    String getUserEmail(Integer userId);

    @Select("select UserID from user where UserEmail=#{userEmail}")
    Integer isUserLive(String userEmail);
}

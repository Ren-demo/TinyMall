package com.mall.tinymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.tinymall.entity.pojo.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    @Select("select `UserID` from orders where OrderID=#{orderId}")
    Integer getUserId(Integer orderId);

    @Select("select `Time` from orders where OrderID=#{orderId}")
    Date getOrderTime(Integer orderId);
}

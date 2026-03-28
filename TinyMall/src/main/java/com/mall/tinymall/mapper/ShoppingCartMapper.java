package com.mall.tinymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.tinymall.entity.pojo.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}

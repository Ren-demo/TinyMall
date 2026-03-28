package com.mall.tinymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.tinymall.entity.dto.AddCartDto;
import com.mall.tinymall.entity.dto.UpdateCartDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.pojo.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {
    Result listById(Integer userId);

    Result addCart(AddCartDto dto);

    Result updateCart(UpdateCartDto dto);
}

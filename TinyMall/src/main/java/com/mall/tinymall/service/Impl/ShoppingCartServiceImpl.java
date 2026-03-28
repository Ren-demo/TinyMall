package com.mall.tinymall.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.tinymall.entity.dto.AddCartDto;
import com.mall.tinymall.entity.dto.UpdateCartDto;
import com.mall.tinymall.entity.pojo.Goods;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.pojo.ShoppingCart;
import com.mall.tinymall.entity.vo.UserCartVo;
import com.mall.tinymall.mapper.GoodsMapper;
import com.mall.tinymall.mapper.ShoppingCartMapper;
import com.mall.tinymall.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @date 2026/1/20 16:13
 * @description TODO: 用户购物车管理业务层
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl
        extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
        implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private GoodsMapper goodsMapper;

    //查询购物车
    @Override
    public Result listById(Integer userId) {
        LambdaUpdateWrapper<ShoppingCart> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ShoppingCart::getUserID, userId);
        List<ShoppingCart> shoppingCarts = this.list(wrapper);
        List<UserCartVo> vos = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCarts) {
            UserCartVo vo = new UserCartVo();
            vo.setShoppingCartId(shoppingCart.getShoppingCartId());
            vo.setUserId(userId);
            vo.setStoreId(shoppingCart.getStoreID());
            vo.setGoodsId(shoppingCart.getGoodsID());
            vo.setCount(shoppingCart.getCount());
            vo.setTotalPrice(shoppingCart.getTotalPrice());
            Goods good = goodsMapper.selectById(shoppingCart.getGoodsID());
            vo.setGoodsName(good.getGoodsName());
            vo.setPrice(good.getPrice());
            vo.setGoodsImg(good.getPicture());
            vos.add(vo);
        }

        return Result.success(vos);
    }

    //商品加入购物车
    @Override
    public Result addCart(AddCartDto dto) {
        ShoppingCart shoppingCart = new ShoppingCart();
        Goods good = goodsMapper.selectById(dto.getGoodsId());
        shoppingCart.setUserID(dto.getUserId());
        shoppingCart.setGoodsID(dto.getGoodsId());
        shoppingCart.setStoreID(good.getStoreID());
        shoppingCart.setCount(dto.getCount());
        shoppingCart.setTotalPrice(good.getPrice() * dto.getCount());
        if(this.save(shoppingCart)) return Result.success(shoppingCart.getShoppingCartId());
        return Result.error("加入购物车失败");
    }

    //修改或删除购物车
    @Override
    public Result updateCart(UpdateCartDto dto) {
        if(dto.isDelete() && this.removeById(dto.getShoppingCartId()))
            return Result.success();
        ShoppingCart old = this.getById(dto.getShoppingCartId());
        UpdateWrapper<ShoppingCart> wrapper = new UpdateWrapper<>();
        wrapper.eq("ShoppingCartId", dto.getShoppingCartId());
        wrapper.set("Count", dto.getGoodsCount());
        wrapper.set("TotalPrice", old.getTotalPrice()/old.getCount()*dto.getGoodsCount());
        if(this.update(wrapper)) return Result.success();
        return Result.error("操作失败");
    }

}

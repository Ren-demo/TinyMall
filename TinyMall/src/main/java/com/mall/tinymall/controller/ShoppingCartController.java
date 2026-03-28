package com.mall.tinymall.controller;

import com.mall.tinymall.entity.dto.AddCartDto;
import com.mall.tinymall.entity.dto.AddOrderDto;
import com.mall.tinymall.entity.dto.UpdateCartDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.service.OrderService;
import com.mall.tinymall.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @date 2026/1/20 16:03
 * @description TODO: 用户购物车控制层
 */
@Slf4j
@RestController
@RequestMapping("/tinymall/shoppingCart")
@Tag(name="用户购物车控制层")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private OrderService orderService;

    //用户查询自己的购物车
    @GetMapping("/list/{userId}")
    @Operation(summary = "用户查询自己的购物车")
    public Result list(@PathVariable Integer userId){
        return shoppingCartService.listById(userId);
    }

    //修改或删除购物车
    @PutMapping("/update")
    @Operation(summary = "修改购物车，count表示修改数量，isDelete是否删除该购物车，true表示要删除")
    public Result update(@RequestBody UpdateCartDto dto){
        return shoppingCartService.updateCart(dto);
    }

    //用户从购物车创建订单
    @PostMapping("/addOrderOnCart")
    @Operation(summary = "用户从购物车下单")
    public Result addOrderOnCart(@RequestBody AddOrderDto dto) throws Exception{
        return orderService.addOrder(dto);
    }

    //商品添加购物车
    @PostMapping("/addCart")
    @Operation(summary = "商品添加购物车")
    public Result addCart(@RequestBody AddCartDto dto){
        return shoppingCartService.addCart(dto);
    }

}

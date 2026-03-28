package com.mall.tinymall.controller;

import com.mall.tinymall.entity.dto.AddOrderDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @date 2026/1/20 16:59
 * @description TODO: 用户订单管理
 */
@Slf4j
@RestController
@RequestMapping("/tinymall/order")
@Tag(name = "用户订单管理")
public class UserOrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "用户查询所有订单")
    @GetMapping("/list/{userId}")
    public Result list(@PathVariable("userId") Integer userId){
        return orderService.userListOrder(userId);
    }

    @Operation(summary = "用户立即购买提交订单")
    @PostMapping("/addOrder")
    public Result addOrder(@RequestBody AddOrderDto dto)throws Exception{
        return Result.success(orderService.addOrder(dto));
    }

    @Operation(summary = "用户删除订单")
    @DeleteMapping("/delOrder/{orderId}")
    public Result userDelOrder(@PathVariable Integer orderId){
        return orderService.userDelOrder(orderId);
    }

    @Operation(summary = "用户确认收货")
    @PutMapping("/checkOrder/{orderId}")
    public Result checkOrder(@PathVariable Integer orderId){
        return orderService.checkOrder(orderId);
    }

}

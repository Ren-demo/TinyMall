package com.mall.tinymall.controller;

import com.mall.tinymall.entity.dto.PayDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @date 2026/1/21 16:33
 * @description TODO: 支付管理
 */
@RestController
@Slf4j
@RequestMapping("/tinymall/pay")
@Tag(name = "支付控制")
public class PayController {
    @Autowired
    private OrderService orderService;

    @PutMapping("/pay")
    @Operation(summary = "用户支付订单")
    public Result payOrd(@RequestBody PayDto dto){
        return orderService.payOrd(dto);
    }
}

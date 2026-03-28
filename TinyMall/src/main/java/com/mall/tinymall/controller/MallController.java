package com.mall.tinymall.controller;

import com.mall.tinymall.entity.dto.AddStoreDto;
import com.mall.tinymall.entity.dto.ManageOrderDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.service.OrderService;
import com.mall.tinymall.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @date 2026/1/21 14:54
 * @description TODO: 商店控制层
 */
@RestController
@Slf4j
@RequestMapping("/tinymall/mall")
@Tag(name="商店控制层")
public class MallController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private StoreService storeService;

    @Operation(summary = "商家查询所有订单")
    @GetMapping("/list")
    public Result listAllOrders(){
        return orderService.mallListOrder();
    }

    @Operation(summary = "商店接受或拒绝订单", description = "商店管理订单（接受/拒绝）")
    @PutMapping("/manageOrder")
    public Result manageOrder(@RequestBody ManageOrderDto dto){
        return orderService.mallManageOrder(dto);
    }

    @Operation(summary = "新增供应商")
    @PostMapping("/addStore")
    public Result addStore(@RequestBody AddStoreDto dto, MultipartFile picture){
        return storeService.addStore(dto, picture);
    }

    @Operation(summary = "商店催促供应商发货")
    @PostMapping("/urge/{orderId}/{storeId}")
    public Result urge(@PathVariable Integer orderId, @PathVariable Integer storeId){
        return orderService.urge(orderId, storeId);
    }
}

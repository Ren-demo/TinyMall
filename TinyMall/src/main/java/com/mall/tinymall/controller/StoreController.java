package com.mall.tinymall.controller;

import com.mall.tinymall.entity.dto.AddGoodsDto;
import com.mall.tinymall.entity.dto.UpdateGoodsDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.service.GoodsService;
import com.mall.tinymall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @date 2026/1/22 0:59
 * @description TODO: 供应商行为
 */
@RestController
@Slf4j
@Tag(name = "供应商行为控制")
@RequestMapping("/tinymall/store")
public class StoreController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private GoodsService goodsService;

    @Operation(summary = "新增商品")
    @PostMapping("/add")
    public Result add(@ModelAttribute AddGoodsDto dto, @RequestParam(value = "file", required = false) MultipartFile file){
        return goodsService.addGoods(dto, file);
    }

    @Operation(summary = "供应商查看自己的订单")
    @GetMapping("/listOrders/{storeId}")
    public Result listOrders(@PathVariable Integer storeId){
        return orderService.storeListOrders(storeId);
    }

    @Operation(summary = "供应商查看自己的商品")
    @GetMapping("/listGoods/{storeId}")
    public Result listGoods(@PathVariable Integer storeId){
        return goodsService.storeListGoods(storeId);
    }

    @Operation(summary = "供应商修改商品信息")
    @PutMapping("/updateGoods")
    public Result updateGoods(@RequestBody UpdateGoodsDto dto){
        return goodsService.updateGoods(dto);
    }

    @Operation(summary = "供应商修改商品图片")
    @PostMapping("/updateGoodsPic/{goodsId}")
    public Result updateGoodsPic(@PathVariable Integer goodsId, MultipartFile file){
        return goodsService.updateGoodsPic(goodsId, file);
    }

    @Operation(summary = "供应商发货")
    @PutMapping("/delivery/{orderId}")
    public Result delivery(@PathVariable Integer orderId){
        return orderService.delivery(orderId);
    }

    @Operation(summary = "供应商删除商品")
    @DeleteMapping("/deleteGoods/{goodsId}")
    public Result deleteGoods(@PathVariable Integer goodsId){
        return goodsService.deleteGoods(goodsId);
    }

}

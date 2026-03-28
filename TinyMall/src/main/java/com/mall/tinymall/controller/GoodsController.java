package com.mall.tinymall.controller;

import com.mall.tinymall.entity.dto.ListGoodsDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @date 2026/1/20 15:25
 * @description TODO: 用户商品操作控制层
 */
@RestController
@Slf4j
@RequestMapping("/tinymall/goods")
@Tag(name="用户商品操作控制层")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    //查询所有余量大于0的商品
    @GetMapping("/list")
    @Operation(summary = "查询所有余量大于0的商品")
    public Result listGoods(){
        return goodsService.listGoods();
    }

    //分页查询所有余量大于0的商品
    @GetMapping("/listInpage/{pageNum}/{pageSize}")
    @Operation(summary = "分页查询所有余量大于0的商品")
    public Result listGoodsInPage(@Schema(description = "页码") @PathVariable(required = false) Integer pageNum, @Schema(description = "每页大小")@PathVariable(required = false) Integer pageSize){
        ListGoodsDto listGoodsDto = new ListGoodsDto(pageNum, pageSize);
        return Result.success(goodsService.listGoodsInPage(listGoodsDto));
    }
}

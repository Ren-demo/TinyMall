package com.mall.tinymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.mall.tinymall.entity.dto.AddGoodsDto;
import com.mall.tinymall.entity.dto.ListGoodsDto;
import com.mall.tinymall.entity.dto.UpdateGoodsDto;
import com.mall.tinymall.entity.pojo.Goods;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.vo.ListGoodsVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GoodsService extends IService<Goods> {
    Result listGoods();

    Result addGoods(AddGoodsDto dto, MultipartFile file);

    Result storeListGoods(Integer storeId);

    Result updateGoods(UpdateGoodsDto dto);

    Result updateGoodsPic(Integer goodsId, MultipartFile file);

    PageInfo<ListGoodsVo> listGoodsInPage(ListGoodsDto listGoodsDto);

    /**
     * 删除商品
     * @param goodsId 商品ID
     * @return 删除结果
     */
    Result deleteGoods(Integer goodsId);
}

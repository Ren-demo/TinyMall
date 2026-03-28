package com.mall.tinymall.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.tinymall.entity.dto.AddGoodsDto;
import com.mall.tinymall.entity.dto.ListGoodsDto;
import com.mall.tinymall.entity.dto.UpdateGoodsDto;
import com.mall.tinymall.entity.pojo.Goods;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.pojo.Store;
import com.mall.tinymall.entity.vo.ListGoodsVo;
import com.mall.tinymall.mapper.GoodsMapper;
import com.mall.tinymall.mapper.StoreMapper;
import com.mall.tinymall.service.GoodsService;
import com.mall.tinymall.util.DefaultPic;
import com.mall.tinymall.util.MinioUtil;
import com.mall.tinymall.util.PicType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @date 2026/1/20 15:26
 * @description TODO: 用户商品业务层
 */
@Slf4j
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private StoreMapper storeMapper;

    @Override
    public Result listGoods() {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Goods::getCount, 0);
        List<Goods> Goods = goodsMapper.selectList(wrapper);
        List<ListGoodsVo> vos = new ArrayList<>();
        for (Goods good : Goods) {
            ListGoodsVo vo = new ListGoodsVo();
            BeanUtils.copyProperties(good, vo);
            vo.setStoreName(storeMapper.getStoreName(good.getStoreID()));
            vos.add(vo);
        }
        return Result.success(vos);
    }

    @Override
    public PageInfo<ListGoodsVo> listGoodsInPage(ListGoodsDto listGoodsDto) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Goods::getCount, 0);
        // 执行分页操作
        PageHelper.startPage(listGoodsDto.getPageNum(), listGoodsDto.getPageSize());
        List<Goods> goodsList = goodsMapper.selectList(wrapper);
        // 先创建Goods类型的PageInfo
        PageInfo<Goods> goodsPageInfo = new PageInfo<>(goodsList);
        // 批量查询店铺名
        List<ListGoodsVo> vos = new ArrayList<>();
        if (!goodsList.isEmpty()) {
            // 提取店铺ID
            Set<Integer> storeIds = goodsList.stream()
                    .map(Goods::getStoreID)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            // 批量查询店铺列表
            List<Store> stores = storeMapper.selectList(new LambdaQueryWrapper<Store>().in(Store::getStoreId, storeIds));
            Map<Integer, String> storeNameMap = stores.stream()
                    .collect(Collectors.toMap(Store::getStoreId, Store::getStoreName));
            // 转换为VO
            for (Goods good : goodsList) {
                ListGoodsVo vo = new ListGoodsVo();
                BeanUtils.copyProperties(good, vo);
                vo.setStoreName(storeNameMap.getOrDefault(good.getStoreID(), ""));
                vos.add(vo);
            }
        }

        // 4. 修复泛型问题：先创建空的ListGoodsVo类型PageInfo，再复制元信息+设置VO列表
        PageInfo<ListGoodsVo> resultPageInfo = new PageInfo<>();
        // 复制分页元信息（复用原PageInfo的所有分页字段）
        BeanUtils.copyProperties(goodsPageInfo, resultPageInfo);
        // 设置VO列表
        resultPageInfo.setList(vos);

        return resultPageInfo;
    }

//    @Override
    public PageInfo<ListGoodsVo> listGoodsInPage1(ListGoodsDto listGoodsDto) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Goods::getCount, 0);

        // 1. 执行分页操作（PageHelper 绑定到紧接着的查询）
        PageHelper.startPage(listGoodsDto.getPageNum(), listGoodsDto.getPageSize());
        List<Goods> goodsList = goodsMapper.selectList(wrapper);

        // 2. 先封装 Goods 的 PageInfo（此时包含正确的 total、pages 等元信息）
        PageInfo<Goods> goodsPageInfo = new PageInfo<>(goodsList);

        // 3. 转换 Goods 到 ListGoodsVo
        List<ListGoodsVo> vos = new ArrayList<>();
        for (Goods good : goodsList) {
            ListGoodsVo vo = new ListGoodsVo();
            BeanUtils.copyProperties(good, vo);
            vo.setStoreName(storeMapper.getStoreName(good.getStoreID()));
            vos.add(vo);
        }

        // 4. 创建新的 PageInfo，复用原分页元信息，只替换数据列表
        PageInfo<ListGoodsVo> resultPageInfo = new PageInfo<>(vos);
        // 复制原分页元信息（total、pages、pageNum 等）
        resultPageInfo.setTotal(goodsPageInfo.getTotal());
        resultPageInfo.setPages(goodsPageInfo.getPages());
        resultPageInfo.setPageNum(goodsPageInfo.getPageNum());
        resultPageInfo.setPageSize(goodsPageInfo.getPageSize());
        resultPageInfo.setIsFirstPage(goodsPageInfo.isIsFirstPage());
        resultPageInfo.setIsLastPage(goodsPageInfo.isIsLastPage());
        resultPageInfo.setHasNextPage(goodsPageInfo.isHasNextPage());
        resultPageInfo.setHasPreviousPage(goodsPageInfo.isHasPreviousPage());

        return resultPageInfo;
    }

    @Autowired
    private MinioUtil minioUtil;
    //新增商品
    @Override
    public Result addGoods(AddGoodsDto dto, MultipartFile file) {
        if(!minioUtil.isImage(Objects.requireNonNull(file.getOriginalFilename()))) return Result.error("请输入正确的图片格式");
        String url = minioUtil.uploadFile(file, PicType.GOODS_PICTURE.getCode());
        Goods good = new Goods();
        BeanUtils.copyProperties(dto, good);
        good.setPicture(url);
        goodsMapper.insert(good);
        return Result.success(good);
    }

    @Override
    public Result storeListGoods(Integer storeId) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("StoreID", storeId);
        List<Goods> goods = goodsMapper.selectList(wrapper);
        return Result.success(goods);
    }

    @Override
    public Result updateGoods(UpdateGoodsDto dto) {
        Integer count = goodsMapper.getGoodsCount(dto.getGoodsId());
        if(dto.getCount()!=0){
            if(dto.isAdd()){
                count += dto.getCount();
            } else if(dto.getCount()>count){
                return Result.error("当前商品数量已不足 "+dto.getCount()+"，操作失败");
            }else {
                count -= dto.getCount();
            }
        }
        LambdaUpdateWrapper<Goods> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Goods::getGoodsID, dto.getGoodsId());
        wrapper.set(Goods::getCount, count);
        wrapper.set(Goods::getGoodsName, dto.getGoodsName());
        wrapper.set(Goods::getText, dto.getText());
        wrapper.set(Goods::getPrice, dto.getPrice());
        update(wrapper);
        return Result.success();
    }

    @Override
    public Result updateGoodsPic(Integer goodsId, MultipartFile file) {
        if(!minioUtil.isImage(Objects.requireNonNull(file.getOriginalFilename()))) return Result.error("图片格式错误");
        String url = goodsMapper.getGoodsPicture(goodsId);
        if(!url.equals(DefaultPic.DEFAULT_GOODS_PICTURE.getUrl())) minioUtil.deleteFile(url);
        url = minioUtil.uploadFile(file, PicType.GOODS_PICTURE.getCode());
        goodsMapper.saveGoodsPicture(goodsId, url);
        return Result.success();
    }

    @Override
    @Transactional
    public Result deleteGoods(Integer goodsId) {
        // 检查商品是否存在
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            return Result.error("商品不存在");
        }
        
        // 删除商品
        int result = goodsMapper.deleteById(goodsId);
        if (result > 0) {
            // 删除商品图片（如果不是默认图片）
            String pictureUrl = goods.getPicture();
            if (pictureUrl != null && !pictureUrl.equals(DefaultPic.DEFAULT_GOODS_PICTURE.getUrl())) {
                try {
                    minioUtil.deleteFile(pictureUrl);
                    log.info("商品图片已删除: {}", pictureUrl);
                } catch (Exception e) {
                    log.error("删除商品图片失败: {}", e.getMessage(), e);
                    // 不抛出异常，避免影响主业务流程
                }
            }
            
            return Result.success("商品删除成功");
        } else {
            return Result.error("商品删除失败");
        }
    }



}

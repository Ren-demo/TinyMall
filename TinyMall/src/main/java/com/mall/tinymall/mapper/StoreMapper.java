package com.mall.tinymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.tinymall.entity.pojo.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StoreMapper extends BaseMapper<Store> {
    @Select("select StoreName from store where StoreID=#{storeId}")
    String getStoreName(Integer StoreId);

    @Select("select StoreEmail from store where StoreID=#{storeId}")
    String getStoreEmail(Integer storeId);
}

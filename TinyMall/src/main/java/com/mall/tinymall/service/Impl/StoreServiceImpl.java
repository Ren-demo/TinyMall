package com.mall.tinymall.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.tinymall.entity.dto.AddStoreDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.pojo.Store;
import com.mall.tinymall.mapper.StoreMapper;
import com.mall.tinymall.service.StoreService;
import com.mall.tinymall.util.DefaultPic;
import com.mall.tinymall.util.MinioUtil;
import com.mall.tinymall.util.PicType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @date 2026/1/21 9:33
 * @description TODO: 供货商业务层
 */
@Service
@Slf4j
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {

    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private MinioUtil minioUtil;

    @Override
    public Result addStore(AddStoreDto dto, MultipartFile picture) {
        Store store = new Store();
        String url = "";
        if(picture==null) url = DefaultPic.DEFAULT_STORE_PICTURE.getUrl();
        else
            url = minioUtil.uploadFile(picture, PicType.STORE_PICTURE.getCode());
        store.setPicture(url);
        store.setStoreEmail(dto.getStoreEmail());
        store.setStoreName(dto.getStoreName());
        if(storeMapper.insert(store)==1)
            return Result.success(store);
        return Result.error("新增失败");
    }
}

package com.mall.tinymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.tinymall.entity.dto.AddStoreDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.entity.pojo.Store;
import org.springframework.web.multipart.MultipartFile;

public interface StoreService extends IService<Store> {
    Result addStore(AddStoreDto dto, MultipartFile picture);
}

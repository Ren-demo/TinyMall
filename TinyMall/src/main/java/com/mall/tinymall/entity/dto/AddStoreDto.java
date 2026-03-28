package com.mall.tinymall.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @date 2026/1/22 1:05
 * @description TODO: 新增供应商信息传递
 */
@Data
@AllArgsConstructor
public class AddStoreDto {
    private String storeName;
    private String storeEmail;
}

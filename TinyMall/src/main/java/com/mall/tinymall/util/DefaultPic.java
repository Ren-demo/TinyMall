package com.mall.tinymall.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @date 2026/1/21 12:24
 * @description TODO: 默认图片枚举
 */
@AllArgsConstructor
@Getter
public enum DefaultPic {

    DEFAULT_USER_PROFILE("https://img95.699pic.com/element/40203/4444.png_300.png!/fw/431/clip/0x300a0a0"),
    DEFAULT_GOODS_PICTURE("https://img.shetu66.com/2023/07/06/1688611155457376.png"),
    DEFAULT_STORE_PICTURE("https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg");

    private final String url;


    @Override
    public String toString() {
        return this.url;
    }
}

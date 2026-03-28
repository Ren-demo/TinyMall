package com.mall.tinymall.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @date 2026/1/21 22:57
 * @description TODO: 图片种类
 */
@AllArgsConstructor
@Getter
public enum PicType {
    USER_PICTURE(1),
    STORE_PICTURE(2),
    GOODS_PICTURE(3);

    private final Integer code;
}

package com.mall.tinymall.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @date 2026/1/21 14:02
 * @description TODO: 订单状态描述
 */
@AllArgsConstructor
@Getter
public enum OrderStateENum {
    ORDER_CREATE(0),
    ORDER_PAID(1),
    ORDER_ACCEPT(2),
    ORDER_DELIVERY(3),
    ORDER_REFUSE(-1),
    OREDER_CHECK(4);


    private final int code;
}

package com.mall.tinymall.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @date 2026/1/21 17:22
 * @description TODO: 支付信息传递
 */
@Data
@AllArgsConstructor
public class PayDto {
    private Integer orderId;
    private String paymentToken;
}

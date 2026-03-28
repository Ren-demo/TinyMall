package com.mall.tinymall.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @date 2026/1/22 0:22
 * @description TODO: 商店管理订单信息传递
 */
@Data
@AllArgsConstructor
public class ManageOrderDto {
    private Integer orderId;
    private boolean isRefuse; //true拒绝 false接受
    private String msg; //拒绝信息（可选）
}

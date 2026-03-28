package com.mall.tinymall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.tinymall.entity.dto.AddOrderDto;
import com.mall.tinymall.entity.dto.ManageOrderDto;
import com.mall.tinymall.entity.dto.PayDto;
import com.mall.tinymall.entity.pojo.Order;
import com.mall.tinymall.entity.pojo.Result;

public interface OrderService extends IService<Order> {
    Result userListOrder(Integer userId);

    Result addOrder(AddOrderDto dto) throws Exception;

    Result mallListOrder();

    Result userDelOrder(Integer orderId);

    Result payOrd(PayDto orderId);

    Result mallManageOrder(ManageOrderDto dto);

    Result storeListOrders(Integer storeId);

    Result delivery(Integer orderId);

    Result urge(Integer orderId, Integer storeId);

    Result checkOrder(Integer orderId);
}

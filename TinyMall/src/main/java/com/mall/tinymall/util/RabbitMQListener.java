package com.mall.tinymall.util;

import com.mall.tinymall.entity.pojo.Order;
import com.mall.tinymall.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


/**
 * @date 2025-12-24 9:22
 * @description TODO: 消息消费者
 */
@Component
@Slf4j
public class RabbitMQListener {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "delay.queue_clearVelify", durable = "true"),
            exchange = @Exchange(value = "delay.exchange_DeleteVelify",delayed = "true"),
            key = "clearVelify"
    ))
    public void clearVelify(String msg){
        if(redisTemplate.hasKey(msg)){
            redisTemplate.delete(msg);
        }
    }

    @Autowired
    private OrderMapper orderMapper;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "delay.queue_clearTimeOurOrder", durable = "true"),
            exchange = @Exchange(value = "delay.exchange_DeleteTimeOurOrder",delayed = "true"),
            key = "clearTimeOurOrder"
    ))
    public void delayDeleteTimeOurOrder(String msg){
        if(redisTemplate.hasKey(msg)){
            redisTemplate.delete(msg);
        }
        String orderId = msg.split("_")[1];
        orderMapper.deleteById(Integer.parseInt(orderId));
    }
}
     
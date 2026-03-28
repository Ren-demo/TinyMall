package com.mall.tinymall.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @date 2026/1/20 10:11
 * @description TODO: 消息生产者
 */
@Slf4j
@Component
public class MsgPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void delayDeleteVelify(String message){
        rabbitTemplate.convertAndSend("delay.exchange_DeleteVelify", "delay_DeleteVerify", message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelayLong(300000L);
                return message;
            }
        });
    }

    public void delayDeleteTimeOurOrder(String message){
        rabbitTemplate.convertAndSend("delay.exchange_DeleteTimeOurOrder", "delay_delOrder", message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelayLong(1800000L);
                return message;
            }
        });
    }

}

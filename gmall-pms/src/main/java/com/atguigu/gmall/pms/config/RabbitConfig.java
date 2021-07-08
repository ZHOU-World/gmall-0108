package com.atguigu.gmall.pms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;

/**
 * Date:2021/7/7
 * Author：ZHOU_World
 * Description:RabbitMQ回调
 */
@Slf4j
@Configuration
public class RabbitConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct//保证启动时执行init方法
    public void init(){
        //确认消息是否到达交换机，有没有交换机都会执行此方法
        this.rabbitTemplate.setConfirmCallback((@NonNull CorrelationData correlationData,
                                                boolean ack, @Nullable String cause)->{
            if(!ack){//true:到达交换机
                log.error("消息没有到达交换机,原因：{}",cause);
            }

        });
        //确认消息是否到达队列，只有没有达到队列才会执行此方法
        this.rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText,
                                               String exchange, String routingKey)->{
            log.error("消息没到达队列，交换机：{}，路由键：{}，消息内容：{}",exchange,routingKey,
                    new String(message.getBody()));
        });
    }
}

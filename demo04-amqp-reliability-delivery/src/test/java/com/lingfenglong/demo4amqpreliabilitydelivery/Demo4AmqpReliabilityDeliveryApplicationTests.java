package com.lingfenglong.demo4amqpreliabilitydelivery;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Demo4AmqpReliabilityDeliveryApplicationTests {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void sendMessageSuccess() {
        rabbitTemplate.convertAndSend(
                Demo4AmqpReliabilityDeliveryApplication.EXCHANGE_NAME,
                "userinfo",
                new UserInfo(1, "zhangsan")
        );
    }

    @Test
    void sendMessageFailure_ExchangeFailed() {
        rabbitTemplate.convertAndSend(
                Demo4AmqpReliabilityDeliveryApplication.EXCHANGE_NAME + "xxx",
                "userinfo",
                new UserInfo(1, "zhangsan")
        );
    }

    @Test
    void sendMessageFailure_QueueFailed() {
        rabbitTemplate.convertAndSend(
                Demo4AmqpReliabilityDeliveryApplication.EXCHANGE_NAME,
                "userinfoxxx",
                new UserInfo(1, "zhangsan")
        );
    }

    @Test
    void sendMessageSuccess_OriginQueueFailed_BackupExchange() {
        rabbitTemplate.convertAndSend(
                Demo4AmqpReliabilityDeliveryApplication.EXCHANGE_NAME,
                "userinfoxxx",
                new UserInfo(1, "zhangsan")
        );
    }
}

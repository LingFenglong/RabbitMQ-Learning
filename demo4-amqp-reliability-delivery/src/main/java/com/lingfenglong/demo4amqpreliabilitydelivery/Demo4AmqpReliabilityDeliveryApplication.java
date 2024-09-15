package com.lingfenglong.demo4amqpreliabilitydelivery;

import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@SpringBootApplication
public class Demo4AmqpReliabilityDeliveryApplication {
    public static final String EXCHANGE_NAME = "reliability_delivery_demo_exchange";
    public static final String BACKUP_EXCHANGE_NAME = "reliability_delivery_demo_exchange_backup";
    public static final String QUEUE_NAME = "reliability_delivery_demo_queue";
    public static final String BACKUP_QUEUE_NAME = "reliability_delivery_demo_queue_backup";

    public static void main(String[] args) {
        SpringApplication.run(Demo4AmqpReliabilityDeliveryApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(RabbitTemplate rabbitTemplate) {
        return args -> rabbitTemplate.convertAndSend(
                Demo4AmqpReliabilityDeliveryApplication.EXCHANGE_NAME,
                "userinfoxxx",
                new UserInfo(1, "zhangsan")
        );
    }
}

@Component
class UserInfoListenerBackup {
    private static final Logger log = LoggerFactory.getLogger(UserInfoListenerBackup.class);

    @RabbitListener(
            bindings = @QueueBinding(
                value = @Queue(name = Demo4AmqpReliabilityDeliveryApplication.BACKUP_QUEUE_NAME, durable = "true"),
                exchange = @Exchange(name = Demo4AmqpReliabilityDeliveryApplication.BACKUP_EXCHANGE_NAME, type = "fanout", durable = "true")
    ))
    public void listen(UserInfo userInfo, Message message, Channel channel) {
        log.info("userInfo: {}", userInfo);
        log.info("message: {}", message);
        log.info("channel: {}", channel);
    }
}

@Component
class UserInfoListener {
    private static final Logger log = LoggerFactory.getLogger(UserInfoListener.class);

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = Demo4AmqpReliabilityDeliveryApplication.QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(
                            name = Demo4AmqpReliabilityDeliveryApplication.EXCHANGE_NAME,
                            type = "topic",
                            durable = "true",
                            arguments = {@Argument(name = "alternate-exchange", value = Demo4AmqpReliabilityDeliveryApplication.BACKUP_EXCHANGE_NAME)}
                    ),
                    key = "userinfo.#"
            )
    )
    public void listen(UserInfo userInfo, Message message, Channel channel) {
        log.info("userInfo: {}", userInfo);
        log.info("message: {}", message);
        log.info("channel: {}", channel);
    }
}

@Configuration
class RabbitConfig {
    @Bean
    public SimpleMessageConverter messageConverter() {
        SimpleMessageConverter messageConverter = new SimpleMessageConverter();
        messageConverter.addAllowedListPatterns("*");
        return messageConverter;
    }
}

@Configuration
class RabbitTemplatePostProcessor {
    private static final Logger log = LoggerFactory.getLogger(RabbitTemplatePostProcessor.class);
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitTemplatePostProcessor(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void init() {

        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            log.info("correlationData: {}", correlationData);
            log.info("ack: {}", ack);
            log.info("cause: {}", cause);
        });

        rabbitTemplate.setReturnsCallback(returned -> {
            log.info("returned: {}", returned);
        });

    }
}

record UserInfo(Integer id, String name) implements Serializable {

}


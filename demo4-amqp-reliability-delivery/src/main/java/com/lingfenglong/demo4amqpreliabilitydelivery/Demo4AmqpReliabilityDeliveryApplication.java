package com.lingfenglong.demo4amqpreliabilitydelivery;

import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@SpringBootApplication
public class Demo4AmqpReliabilityDeliveryApplication {
    public static final String EXCHANGE_NAME = "reliability_delivery_demo_exchange";
    public static final String QUEUE_NAME = "reliability_delivery_demo_queue";

    public static void main(String[] args) {
        SpringApplication.run(Demo4AmqpReliabilityDeliveryApplication.class, args);
    }
}

@Component
class UserInfoListener {
    private static final Logger log = LoggerFactory.getLogger(UserInfoListener.class);

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = Demo4AmqpReliabilityDeliveryApplication.QUEUE_NAME),
                    exchange = @Exchange(name = Demo4AmqpReliabilityDeliveryApplication.EXCHANGE_NAME, type = "topic"),
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


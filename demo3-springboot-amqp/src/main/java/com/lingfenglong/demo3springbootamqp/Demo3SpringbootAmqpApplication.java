package com.lingfenglong.demo3springbootamqp;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@SpringBootApplication
public class Demo3SpringbootAmqpApplication {
    public static final String EXCHANGE_NAME = "springboot_demo_exchange";
    public static final String QUEUE_NAME = "springboot_demo_queue";
    public static final String[] KEYS = new String[] { "news.#", "books.#" };;

    public static void main(String[] args) {
        // /*
        //     Attempt to deserialize unauthorized class com.lingfenglong.demo3springbootamqp.UserInfo;
        //     add allowed class name patterns to the message converter or,
        //     if you trust the message originator, set environment variable 'SPRING_AMQP_DESERIALIZATION_TRUST_ALL'
        //     or system property 'spring.amqp.deserialization.trust.all' to true
        //  */
        // System.getProperties()
        //         .put("spring.amqp.deserialization.trust.all", "true");

        SpringApplication.run(Demo3SpringbootAmqpApplication.class, args);
    }

    @Bean
    @Order(0)
    public MessageConverter messageConverter() {
        SimpleMessageConverter simpleMessageConverter = new SimpleMessageConverter();
        simpleMessageConverter.addAllowedListPatterns("*");
        return simpleMessageConverter;
    }

    @Bean
    ApplicationRunner runner(MessagePublisher messagePublisher) {
        return args -> new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                messagePublisher.sendMessage();
            }
        }).start();
    }
}

@Component
@Order(2)
class MessagePublisher {
    private final AmqpTemplate amqpTemplate;

    public MessagePublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendMessage() {
        amqpTemplate.convertAndSend(Demo3SpringbootAmqpApplication.EXCHANGE_NAME, "news.cn", new UserInfo(1, "zhangsan"));
    }
}

@Component
@Order(1)
class MessageListener {
    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = Demo3SpringbootAmqpApplication.QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(name = Demo3SpringbootAmqpApplication.EXCHANGE_NAME, type = "topic"),
                    key = { "news.#", "books.#" }
            )
    )
    public void listen(UserInfo userInfo, Message message, Channel channel) {
        log.info("收到了消息：{}", message);
        log.info("userInfo：{}", userInfo);
        log.info("");
    }
}

record UserInfo(Integer id, String username) implements Serializable {

}

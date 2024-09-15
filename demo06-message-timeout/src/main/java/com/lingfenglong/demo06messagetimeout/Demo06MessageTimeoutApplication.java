package com.lingfenglong.demo06messagetimeout;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.Argument;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Demo06MessageTimeoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(Demo06MessageTimeoutApplication.class, args);
    }

    // queue timeout demo. Message will be deleted when it's timeout.
    // @Bean
    // public ApplicationRunner applicationRunner(RabbitTemplate rabbitTemplate) {
    //     return args -> {
    //         for (int i = 0; i < 100; i++) {
    //             rabbitTemplate.convertAndSend("queue_timeout_demo_queue", i + " Hello World!");
    //         }
    //     };
    // }

    // message timeout demo. Message will be deleted when it's timeout.
    @Bean
    public ApplicationRunner applicationRunner(RabbitTemplate rabbitTemplate) {
        return args -> {
            for (int i = 0; i < 100; i++) {
                rabbitTemplate.convertAndSend(
                        "message_timeout_demo_queue",
                        i + " Hello World!",
                        message -> {
                            MessageProperties properties = message.getMessageProperties();
                            properties.setExpiration("5000");
                            return message;
                        });
            }
        };
    }
}

@Component
class MessageListener_QueueTimeout {
    @RabbitListener(
            queuesToDeclare = @Queue(
                    name = "queue_timeout_demo_queue",
                    arguments = @Argument(name = "x-message-ttl", value ="5000", type = "java.lang.Integer")
            )
    )
    public void listen(String data, Message message, Channel channel) {
        // do nothing
        // just waiting the timeout
        try { Thread.sleep(5000); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }
}

@Component
class MessageListener_MessageTimeout {
    @RabbitListener(
            queuesToDeclare = @Queue(
                    name = "message_timeout_demo_queue",
                    arguments = @Argument(name = "x-message-ttl", value ="5000", type = "java.lang.Integer")
            )
    )
    public void listen(String data, Message message, Channel channel) {
        // do nothing
        // just waiting the timeout
        try { Thread.sleep(5000); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }
}
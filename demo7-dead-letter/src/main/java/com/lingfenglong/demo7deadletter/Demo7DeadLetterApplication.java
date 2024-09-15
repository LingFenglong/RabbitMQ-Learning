package com.lingfenglong.demo7deadletter;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@SpringBootApplication
public class Demo7DeadLetterApplication {
    public static void main(String[] args) {
        SpringApplication.run(Demo7DeadLetterApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(RabbitTemplate rabbitTemplate) {
        return args -> {
            for (int i = 0; i < 20; i++) {
                rabbitTemplate.convertAndSend(
                        "normal_dead_letter_demo_exchange",
                        "normal_dead_letter_demo_queue",
                        i + " Hello World!"
                );
            }
        };
    }
}

// dead letter consumer
@Component
class DeadLetterMessageListener {
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "dead_letter_demo_queue"),
                    exchange = @Exchange(name = "dead_letter_demo_exchange"),
                    key = "dead_letter_demo_queue"
            )
    )
    public void listen(String data, Message message, Channel channel) throws IOException {
        System.out.println("dead letter message listener: " + data);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}

// // nack
// @Component
// class MessageListener_Nack {
//     @RabbitListener(
//             bindings = @QueueBinding(
//                     value = @Queue(
//                             name = "normal_dead_letter_demo_queue",
//                             arguments = {
//                                     @Argument(name = "x-dead-letter-exchange", value = "dead_letter_demo_exchange"),
//                                     @Argument(name = "x-dead-letter-routing-key", value = "dead_letter_demo_queue"),
//                                     @Argument(name = "x-message-ttl", value = "5000", type = "java.lang.Integer"),
//                                     @Argument(name = "x-max-length", value = "10", type = "java.lang.Integer")
//                             }),
//                     exchange = @Exchange(name = "normal_dead_letter_demo_exchange"),
//                     key = "normal_dead_letter_demo_queue"
//             )
//     )
//     public void listen(String data, Message message, Channel channel) throws IOException {
//         System.out.println("nack message: " + data);
//         channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
//     }
// }

// timeout
@Component
class MessageListener_Nack {
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = "normal_dead_letter_demo_queue",
                            arguments = {
                                    @Argument(name = "x-dead-letter-exchange", value = "dead_letter_demo_exchange"),
                                    @Argument(name = "x-dead-letter-routing-key", value = "dead_letter_demo_queue"),
                                    @Argument(name = "x-message-ttl", value = "5000", type = "java.lang.Integer"),
                                    @Argument(name = "x-max-length", value = "10", type = "java.lang.Integer"),

                            }),
                    exchange = @Exchange(name = "normal_dead_letter_demo_exchange"),
                    key = "normal_dead_letter_demo_queue"
            )
    )
    public void listen(String data, Message message, Channel channel) throws IOException {
        try { Thread.sleep(10000); } catch (InterruptedException e) { throw new RuntimeException(e); }
        System.out.println("nack message: " + data);
        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
    }
}
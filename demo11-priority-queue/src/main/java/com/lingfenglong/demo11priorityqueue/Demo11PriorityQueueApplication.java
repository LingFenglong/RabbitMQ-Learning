package com.lingfenglong.demo11priorityqueue;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Demo11PriorityQueueApplication {
    public static void main(String[] args) {
        SpringApplication.run(Demo11PriorityQueueApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(RabbitTemplate rabbitTemplate) {
        return args -> {
            for (int i = 1; i <= 5; i++) {
                // int priority = 5 - i + 1;
                int priority = i;
                rabbitTemplate.convertAndSend(
                        "priority_queue_demo_exchange",
                        "priority_queue_demo_queue",
                        i + " Hello World",
                        message -> {
                            message.getMessageProperties().setPriority(priority);
                            return message;
                        }
                );
            }
        };
    }
}

@Component
class PriorityQueueMessageListener {
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = "priority_queue_demo_queue",
                            arguments = @Argument(name = "x-max-priority", value = "5", type = "java.lang.Integer")
                    ),
                    exchange = @Exchange(name = "priority_queue_demo_exchange"),
                    key = "priority_queue_demo_queue"
            )
    )
    public void listen(String data, Message message, Channel channel) {
        System.out.println(data + " | priority: " + message.getMessageProperties().getPriority());
        try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }
}

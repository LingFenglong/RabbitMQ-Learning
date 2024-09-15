package com.lingfenglong.demo05prefetch;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Demo05PrefetchApplication {
    public static void main(String[] args) {
        SpringApplication.run(Demo05PrefetchApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(RabbitTemplate rabbitTemplate) {
        return args -> {
            for (int i = 0; i < 100; i++) {
                rabbitTemplate.convertAndSend("prefetch_demo_queue", i + "Hello World");
            }
        };
    }
}

@Component
class MessageListener {

    @RabbitListener(
            queuesToDeclare = @Queue(name = "prefetch_demo_queue")
    )
    public void listen(String data, Message message, Channel channel) {
        try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }
        System.out.println(data);
    }
}
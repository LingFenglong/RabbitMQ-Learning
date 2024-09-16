package com.lingfenglong.demo12rabbitmqcluster;

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
public class Demo12RabbitmqClusterApplication {
    public static void main(String[] args) {
        SpringApplication.run(Demo12RabbitmqClusterApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(RabbitTemplate rabbitTemplate) {
        return args -> {
            for (int i = 0; i < 100; i++) {
                rabbitTemplate.convertAndSend(
                        "rabbitmq_cluster_demo_exchange",
                        "rabbitmq_cluster_demo_queue",
                        i + " Hello World"
                );
            }
        };
    }
}

@Component
class RabbitmqClusterMessageConsumer {
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = "rabbitmq_cluster_demo_queue",
                            arguments = {
                                    @Argument(name = "x-queue-type", value = "quorum")
                            }
                    ),
                    exchange = @Exchange(name = "rabbitmq_cluster_demo_exchange"),
                    key = "rabbitmq_cluster_demo_queue"
            )
    )
    public void listen(String data, Message message, Channel channel) throws Exception {
        try { Thread.sleep(100); } catch (InterruptedException e) { throw new RuntimeException(e); }
        System.out.println(data);
    }
}

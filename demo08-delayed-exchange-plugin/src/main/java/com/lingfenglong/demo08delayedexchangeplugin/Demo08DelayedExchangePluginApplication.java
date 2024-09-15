package com.lingfenglong.demo08delayedexchangeplugin;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@SpringBootApplication
public class Demo08DelayedExchangePluginApplication {
    public static void main(String[] args) {
        SpringApplication.run(Demo08DelayedExchangePluginApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(RabbitTemplate rabbitTemplate) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                rabbitTemplate.convertAndSend(
                        "delayed_demo_exchange",
                        "delayed_demo_queue",
                        i + " Hello World! " + LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.LONG).withZone(ZoneId.systemDefault())),
                        message -> {
                            MessageProperties properties = message.getMessageProperties();
                            properties.setHeader("x-delay","10000");
                            return message;
                        });
            }
        };
    }
}

@Component
class DelayedExchangeMessageListener {

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "delayed_demo_queue"),
                    exchange = @Exchange(
                            name = "delayed_demo_exchange",
                            type = "x-delayed-message",
                            arguments = {
                                    @Argument(name = "x-delayed-type", value = "direct")
                            }
                    ),
                    key = { "delayed_demo_queue" }
            )
    )
    public void listen(String data, Message message, Channel channel) throws IOException {
        try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }
        System.out.println(data);
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.LONG).withZone(ZoneId.systemDefault())));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}

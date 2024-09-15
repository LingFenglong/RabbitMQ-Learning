package com.lingfenglong.demo09transaction;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@SpringBootApplication
public class Demo09TransactionApplication {
    public static void main(String[] args) {
        SpringApplication.run(Demo09TransactionApplication.class, args);
    }

    @Bean
    public RabbitTransactionManager rabbitTransactionManager(ConnectionFactory connectionFactory) {
        return new RabbitTransactionManager(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setChannelTransacted(true);
        return rabbitTemplate;
    }

    @Bean
    ApplicationRunner applicationRunner(MessageSender messageSender) {
        return args -> {
            messageSender.send();
        };
    }
}

@Component
class MessageSender {
    public RabbitTemplate rabbitTemplate;

    @Autowired
    public MessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public void send() {
        rabbitTemplate.convertAndSend("transaction_demo_queue", "Transaction start");

        int i = 10 / 0;

        rabbitTemplate.convertAndSend("transaction_demo_queue", "Transaction end");
    }
}

@Component
class MessageListener {
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "transaction_demo_queue"),
                    exchange = @Exchange(name = "transaction_demo_exchange"),
                    key = { "transaction_demo_queue" }
            )
    )
    public void listen(String data, Message message, Channel channel) throws IOException {
        System.out.println(data);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}

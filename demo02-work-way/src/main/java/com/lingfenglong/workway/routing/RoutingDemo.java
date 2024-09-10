package com.lingfenglong.workway.routing;

import com.lingfenglong.workway.base.AbstractChannelCustomer;
import com.lingfenglong.workway.base.AbstractChannelProducer;
import com.lingfenglong.workway.base.ConnectionUtil;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

public class RoutingDemo {
    public static final String QUEUE_NAME1 = "routing_queue1";
    public static final String QUEUE_NAME2 = "routing_queue2";
    public static final String QUEUE_NAME3 = "routing_queue3";
    public static final String EXCHANGE_NAME = "routing_exchange";

    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // core code
        channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
        channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
        channel.queueDeclare(QUEUE_NAME3, false, false, false, null);
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME, "news");
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "books");
        channel.queueBind(QUEUE_NAME3, EXCHANGE_NAME, "fruits");
        // end

        RoutingProducer producer = new RoutingProducer("producer", channel);
        RoutingConsumer newsCustomer = new RoutingConsumer("news customer", channel);
        RoutingConsumer booksCustomer = new RoutingConsumer("books customer", channel);
        RoutingConsumer fruitsCustomer = new RoutingConsumer("fruits customer", channel);

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                producer.send(i);
            }
        }).start();

        newsCustomer.consume(QUEUE_NAME1);
        booksCustomer.consume(QUEUE_NAME2);
        fruitsCustomer.consume(QUEUE_NAME3);
    }
}

class RoutingProducer extends AbstractChannelProducer {
    private static final String[] words = new String[] { "news", "books", "fruits" };

    public RoutingProducer(String name, Channel channel) {
        super(name, channel);
    }

    @Override
    public void send(int i) {
        try {
            String routingKey = words[i % 3];
            channel.basicPublish(RoutingDemo.EXCHANGE_NAME, routingKey, null, (i + " Hello from work producer " + routingKey + ".").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class RoutingConsumer extends AbstractChannelCustomer {

    public RoutingConsumer(String name, Channel channel) {
        super(name, channel);
    }

    @Override
    public void consume(String queueName) {
        try {
            getChannel().basicConsume(queueName, true, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

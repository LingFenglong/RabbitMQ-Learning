package com.lingfenglong.workway.topic;

import com.lingfenglong.workway.base.AbstractChannelCustomer;
import com.lingfenglong.workway.base.AbstractChannelProducer;
import com.lingfenglong.workway.base.ConnectionUtil;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

public class TopicDemo {
    public static final String QUEUE_NAME1 = "topic_queue1";
    public static final String QUEUE_NAME2 = "topic_queue2";
    public static final String QUEUE_NAME3 = "topic_queue3";
    public static final String EXCHANGE_NAME = "topic_exchange";

    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        channel.queueDelete(QUEUE_NAME1);
        channel.queueDelete(QUEUE_NAME2);
        channel.queueDelete(QUEUE_NAME3);
        channel.exchangeDelete(EXCHANGE_NAME);

        // core code
        channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
        channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
        channel.queueDeclare(QUEUE_NAME3, false, false, false, null);
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME, "news.*");
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "news.#");
        channel.queueBind(QUEUE_NAME3, EXCHANGE_NAME, "*.*");
        // end

        TopicProducer producer = new TopicProducer("producer", channel);
        TopicConsumer newsCustomer = new TopicConsumer("news.* customer", channel);
        TopicConsumer booksCustomer = new TopicConsumer("news.# customer", channel);
        TopicConsumer fruitsCustomer = new TopicConsumer("*.* customer", channel);

        new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                producer.send(i);
            }
        }).start();

        newsCustomer.consume(QUEUE_NAME1);
        booksCustomer.consume(QUEUE_NAME2);
        fruitsCustomer.consume(QUEUE_NAME3);
    }
}

class TopicProducer extends AbstractChannelProducer {
    private static final String[] news = new String[] { "news.cn", "news.cn.hb", "news.cn.bj" };

    public TopicProducer(String name, Channel channel) {
        super(name, channel);
    }

    @Override
    public void send(int i) {
        try {
            String routingKey = news[i % 3];
            channel.basicPublish(TopicDemo.EXCHANGE_NAME, routingKey, null, (i + " Hello from work producer " + routingKey + ".").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class TopicConsumer extends AbstractChannelCustomer {

    public TopicConsumer(String name, Channel channel) {
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

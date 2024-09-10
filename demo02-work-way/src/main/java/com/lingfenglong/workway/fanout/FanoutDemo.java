package com.lingfenglong.workway.fanout;

import com.lingfenglong.workway.base.AbstractChannelCustomer;
import com.lingfenglong.workway.base.AbstractChannelProducer;
import com.lingfenglong.workway.base.ConnectionUtil;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

public class FanoutDemo {
    public static final String QUEUE_NAME1 = "fanout_queue1";
    public static final String QUEUE_NAME2 = "fanout_queue2";
    public static final String EXCHANGE_NAME = "fanout_exchange";

    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // core code
        channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
        channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME, "");
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "");
        // end

        FanoutProducer producer = new FanoutProducer("producer", channel);

        FanoutConsumer consumer1 = new FanoutConsumer("consumer1", channel);
        FanoutConsumer consumer2 = new FanoutConsumer("consumer2", channel);

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                producer.send(i);
            }
        }).start();

        consumer1.consume(QUEUE_NAME1);
        consumer2.consume(QUEUE_NAME2);
    }
}

class FanoutProducer extends AbstractChannelProducer {
    public FanoutProducer(String name, Channel channel) {
        super(name, channel);
    }

    @Override
    public void send(int i) {
        try {
            channel.basicPublish(FanoutDemo.EXCHANGE_NAME, "", null, (i + " Hello from work producer " + name + ".").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class FanoutConsumer extends AbstractChannelCustomer {
    public FanoutConsumer(String name, Channel channel) {
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

package com.lingfenglong.workway.work;

import com.lingfenglong.workway.base.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

public class WorkDemo {
    public static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // core code
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // end

        Producer producer = new WorkProducer("producer", channel);

        Consumer consumer1 = new WorkCustomer("consumer1", channel);
        Consumer consumer2 = new WorkCustomer("consumer2", channel);

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                producer.send(i);
            }
        }).start();

        consumer1.consume(QUEUE_NAME);
        consumer2.consume(QUEUE_NAME);
    }
}

class WorkProducer extends AbstractChannelProducer {
    public WorkProducer(String name, Channel channel) {
        super(name, channel);
    }

    @Override
    public void send(int i) {
        try {
            channel.basicPublish("", WorkDemo.QUEUE_NAME, null, (i + " Hello from work producer " + name + ".").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class WorkCustomer extends AbstractChannelCustomer {
    public WorkCustomer(String name, Channel channel) {
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

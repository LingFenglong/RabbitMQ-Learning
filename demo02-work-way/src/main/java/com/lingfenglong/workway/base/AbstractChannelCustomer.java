package com.lingfenglong.workway.base;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public abstract class AbstractChannelCustomer extends DefaultConsumer implements Consumer {

    protected final String name;

    public AbstractChannelCustomer(String name, Channel channel) {
        super(channel);
        this.name = name;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        System.out.println(name + " received: " + new String(body));
    }
}

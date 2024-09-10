package com.lingfenglong.workway.base;

import com.rabbitmq.client.Channel;

public abstract class AbstractChannelProducer implements Producer {
    protected final String name;
    protected final Channel channel;

    public AbstractChannelProducer(String name, Channel channel) {
        this.name = name;
        this.channel = channel;
    }
}

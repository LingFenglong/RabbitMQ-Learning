package com.lingfenglong.demo10lazyqueue;

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
public class Demo10LazyQueueApplication {
	public static void main(String[] args) {
		SpringApplication.run(Demo10LazyQueueApplication.class, args);
	}

	@Bean
	public ApplicationRunner applicationRunner(RabbitTemplate rabbitTemplate) {
	    return args -> {
			rabbitTemplate.convertAndSend(
					"lazy_queue_demo_exchange",
					"lazy_queue_demo_queue",
					"Hello World!"
			);
	    };
	}
}

@Component
class LazyQueueMessageListener {
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(
					name = "lazy_queue_demo_queue",
					// durable = "true",
					// autoDelete = "false",
					arguments = {
							// begin
							@Argument(name = "x-queue-mod", value = "lazy")
							// end
					}
			),
			exchange = @Exchange(name = "lazy_queue_demo_exchange"),
			key = "lazy_queue_demo_queue"
	))
	public void listen(String data, Message message, Channel channel) {
		System.out.println(data);
	}
}


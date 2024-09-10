import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Receive {
    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
//        factory.setUsername("");
//        factory.setPassword();
        factory.setHost("localhost");
//        factory.setVirtualHost();
//        factory.setPort();

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            /*
            Note that we declare the queue here, as well. Because we might start the consumer before the publisher,
            we want to make sure the queue exists before we try to consume messages from it.
             */
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicConsume(QUEUE_NAME, true, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
            }, consumerTag -> {

            });
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        }
    }
}

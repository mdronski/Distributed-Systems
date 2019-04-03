import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Administrator {
    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("ADMINISTRATOR");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // exchange
        String EXCHANGE_NAME = "admin_exchange";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

        // queue
        String QUEUE_NAME = channel.queueDeclare().getQueue();
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
        System.out.println("created admin queue: " + QUEUE_NAME);

        // consumer (message handling)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
            }
        };

        // start listening
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    channel.basicConsume(QUEUE_NAME, true, consumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        System.out.println("Waiting for messages...");
        System.out.println("Now you can send messages to others!");

        while (true) {
            // read msg
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter message: ");
            String message = br.readLine();

            // break condition
            if ("exit".equals(message)) {
                break;
            }

            // publish
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println("Sent: " + message);
        }

    }
}

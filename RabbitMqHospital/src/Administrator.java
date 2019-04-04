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
    private static final String ADMIN_EXCHANGE = "admin_exchange";
//    private static final String QUEUE_NAME = "admin_queue";
    private static String QUEUE_NAME;
    public static final String KEY_LOG = "log";
    public static final String KEY_MESSAGE = "message";

    public static void main(String[] argv) throws Exception {

        System.out.println("ADMINISTRATOR");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(ADMIN_EXCHANGE, BuiltinExchangeType.DIRECT);

        QUEUE_NAME = channel.queueDeclare().getQueue();
        channel.queueBind(QUEUE_NAME, ADMIN_EXCHANGE, KEY_LOG);

        handleMessages(channel);
        sendMessages(channel);
    }

    private static void handleMessages(Channel channel) throws IOException {
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
        thread.start();
    }

    private static void sendMessages(Channel channel) throws IOException {
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
            channel.basicPublish(ADMIN_EXCHANGE, KEY_MESSAGE, null, message.getBytes());
            System.out.println("Sent: " + message);
        }
    }
}

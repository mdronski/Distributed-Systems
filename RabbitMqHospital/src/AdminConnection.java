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
import java.util.concurrent.TimeoutException;

public class AdminConnection {
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private final String EXCHANGE_NAME = "admin_exchange";
    private String QUEUE_NAME;

    public AdminConnection() throws IOException, TimeoutException {
        // connection & channel
        factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();

        // exchange
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

        // queue
        QUEUE_NAME = channel.queueDeclare().getQueue();
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
        startListening();
    }


    private void startListening(){
        // consumer (message handling)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received from admin: " + message);
            }
        };

        // start listening
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    channel.basicConsume(QUEUE_NAME, true, consumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void log(String message){
        try {
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

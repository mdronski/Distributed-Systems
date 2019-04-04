
import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Technician {
    private static final String HIP_QUEUE = "hip_queue";
    private static final String KNEE_QUEUE = "knee_queue";
    private static final String ELBOW_QUEUE = "elbow_queue";
    private static final String TECHNICIANS_EXCHANGE = "technicians_exchange";
    private static final AdminConnection connection = new AdminConnection();


    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("TECHNICIAN");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // exchange
        channel.exchangeDeclare(TECHNICIANS_EXCHANGE, BuiltinExchangeType.DIRECT);

        // queue & bind
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter first body part:");
        String first = reader.readLine().toLowerCase();
        initialiseQueue(channel, first);

        System.out.println("Enter second body part:");
        String second = reader.readLine().toLowerCase();
        initialiseQueue(channel, second);

    }

    public static void initialiseQueue(Channel channel, String key) throws IOException {

        String QUEUE;
        switch (key){
            case "hip":
                QUEUE = HIP_QUEUE;
                break;
            case "knee":
                QUEUE = KNEE_QUEUE;
                break;
            case "elbow":
                QUEUE = ELBOW_QUEUE;
                break;
            default:
                throw new IOException("Wrong body part");
        }

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();
            String message = "";
            try {
                message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Received: " + message);
                connection.log("Technician received " + message);
                message = message + ".done";

            } catch (RuntimeException e) {
                System.out.println(" [.] " + e.toString());
            } finally {
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, message.getBytes("UTF-8"));
                connection.log("Technician send " + message);
                System.out.println("Send: " + message);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        channel.queueDeclare(QUEUE, true, false, false, null);
        channel.queueBind(QUEUE, TECHNICIANS_EXCHANGE, key);


        String finalQUEUE = QUEUE;
        Thread thread = new Thread(() -> {
            try {
                channel.basicConsume(finalQUEUE, true, deliverCallback, consumerTag -> { });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

}

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Doctor {

    private static final String TECHNICIANS_EXCHANGE = "technicians_exchange";
    private static final String DOCTORS_EXCHANGE = "doctors_exchange";
    private static String CALLBACK_QUEUE;
    private static final AdminConnection connection = new AdminConnection();



    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("DOCTOR");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        handleMessages(channel);

        sendMessages(channel);

    }

    public static void handleMessages(Channel channel) throws IOException {
        channel.exchangeDeclare(DOCTORS_EXCHANGE, BuiltinExchangeType.DIRECT);

        // queue & bind
        CALLBACK_QUEUE = channel.queueDeclare().getQueue();
        channel.queueBind(CALLBACK_QUEUE, DOCTORS_EXCHANGE, "doctors");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
                connection.log("Doctor received " + message);
            }
        };

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    channel.basicConsume(CALLBACK_QUEUE, false, consumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

    }

    private static void sendMessages(Channel channel) throws IOException {
        channel.exchangeDeclare(TECHNICIANS_EXCHANGE, BuiltinExchangeType.DIRECT);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .replyTo(CALLBACK_QUEUE)
                .build();

        while (true) {

            System.out.println("Enter patient name: ");
            String patient = reader.readLine().toLowerCase();

            System.out.println("Enter examination type: ");
            String examination  = reader.readLine().toLowerCase();

            // break condition
            if ("exit".equals(patient)) {
                break;
            }

            String message = examination + "." + patient;

            // publish
            channel.basicPublish(TECHNICIANS_EXCHANGE, examination, props, message.getBytes(StandardCharsets.UTF_8));
            connection.log("Doctor send " + message);
            System.out.println("Sent: " + message);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

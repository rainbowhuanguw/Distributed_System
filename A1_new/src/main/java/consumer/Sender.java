package consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import info.LiftRide;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Sends a lift ride log generated in the doPost process to the rabbitmq to be consume later
 * by the Consumer
 */
public class Sender {
  //private static final String HOST_NAME = "172.31.82.248"; // private rabbitMQ address
  private static final String HOST_NAME = "localhost";
  private static final String SKIER_QUEUE_NAME = "skierQueue";
  private static final String RESORT_QUEUE_NAME = "resortQueue";
  private static final String RESORT_TYPE = "resorts";
  private static final String SKIER_TYPE = "skiers";

  private static final String USER_NAME = "rainbow"; // rabbitmq user name
  private static final String PASSWORD = "123456";   // rabbitmq password
  private static final int PORT_NUMBER = 5672;

  private static ConnectionFactory factory = null;
  private static Connection connection = null;

  private static void setup() throws IOException, TimeoutException {
    if (factory == null) {
      // connects to the server
      factory = new ConnectionFactory();
      factory.setHost(HOST_NAME);
      factory.setPort(PORT_NUMBER);
      //factory.setUsername(USER_NAME);
      //factory.setPassword(PASSWORD);
    }

    if (connection == null) {
      connection = factory.newConnection();
    }
  }

  public static void sendAMessage(LiftRide input, String type) throws Exception {
    // construct a connection if first time connect
    setup();

    try (Channel channel = connection.createChannel()) {
      // create a channel after connection is established
      // sends one message once
      if (type.equals(SKIER_TYPE)) {
        channel.queueDeclare(SKIER_QUEUE_NAME, false, false, false, null);

        channel.basicQos(1); // limit the number of unacknowledged messages to 1

        String message = input.toString();
        channel.basicPublish("", SKIER_QUEUE_NAME, null,
            message.getBytes(StandardCharsets.UTF_8));

        System.out.println(" [x] Sent '" + message + "'" + type);
      } else if (type.equals(RESORT_TYPE)) {

        channel.queueDeclare(RESORT_QUEUE_NAME, false, false, false, null);

        channel.basicQos(1); // limit the number of unacknowledged messages to 1

        String message = input.toString();
        channel.basicPublish("", RESORT_QUEUE_NAME, null,
            message.getBytes(StandardCharsets.UTF_8));

        System.out.println(" [x] Sent '" + message + "'" + type);
      }

    }
  }
}
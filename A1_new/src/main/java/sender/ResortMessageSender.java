package sender;

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
public class ResortMessageSender {

  //private static final String HOST_NAME = "172.31.82.248"; // private rabbitMQ address
  private static final String HOST_NAME = "localhost";
  private static final String QUEUE_NAME = "resortQueue";
  private static final String EXCHANGE_NAME = "resort-exchange";
  private static final String EXCHANGE_TYPE = "direct";

  private static final String USER_NAME = "rainbow"; // rabbitmq user name
  private static final String PASSWORD = "123456";   // rabbitmq password
  private static final int PORT_NUMBER = 5672;
  private static final int TIME_OUT = 0;

  private static ConnectionFactory factory = null;
  private static Connection connection = null;

  private static void setup() throws IOException, TimeoutException {
    if (factory == null) {
      factory = new ConnectionFactory();
      factory.setHost(HOST_NAME);
      factory.setPort(PORT_NUMBER);
      //factory.setUsername(USER_NAME);
      //factory.setPassword(PASSWORD);
      factory.setRequestedHeartbeat(TIME_OUT);
    }

    if (connection == null) {
      connection = factory.newConnection();
    }
  }

  public static void sendAMessage(LiftRide input) throws Exception {
    // construct a connection if first time connect
    setup();

    try (Channel channel = connection.createChannel()) {
      // fanout exchange
      channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);

      // bind skier queue and exchange
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

      // create a channel after connection is established
      // sends one message once
      channel.basicQos(20); // prefetch value

      String message = input.toString();
      channel.basicPublish(EXCHANGE_NAME, "", null,
          message.getBytes(StandardCharsets.UTF_8));

      //System.out.println(" [x] Sent '" + message + "'");
    }
  }
}
package server;

import info.LiftRide;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

/**
 * Sends a lift ride log generated in the doPost process to the rabbitmq to be consume later
 * by the Consumer
 */
public class Server {
  private static final String REQUEST_QUEUE_NAME = "mq";
  private static final String HOST_NAME = "localhost";
  private static final int PORT_NUMBER = 5672;

  private static ConnectionFactory factory = null;

  private static void setupConnection() {
    if (factory == null) {
      // connects to the server
      factory = new ConnectionFactory();
      factory.setHost(HOST_NAME);
      factory.setPort(PORT_NUMBER);
    }
  }

  public static void sendAMessage(LiftRide input) throws Exception {
    // construct a connection factory if first time connect
    setupConnection();
    // create a channel after connection is established
    // sends one message once
    try (Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {
      channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);

      channel.basicQos(1); // limit the number of unacknowledged messages to 1

      String message = input.toString();
      channel.basicPublish("", REQUEST_QUEUE_NAME, null,
          message.getBytes(StandardCharsets.UTF_8));
      System.out.println(" [x] Sent '" + message + "'");
    }
  }

  public static void main(String[] args) throws Exception {
    LiftRide ride = new LiftRide(123,1, 100);
    Server.sendAMessage(ride);
  }
}
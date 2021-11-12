package consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import info.LiftRide;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Sends a lift ride log generated in the doPost process to the rabbitmq to be consume later
 * by the Consumer
 */
public class Sender {
  private static final String HOST_NAME = "172.31.82.248"; // private rabbitMQ address
  //private static final String HOST_NAME = "localhost";
  private static final String REQUEST_QUEUE_NAME = "mq";
  private static final String USER_NAME = "rainbow";
  private static final String PASSWORD = "123456";
  private static final int PORT_NUMBER = 5672;

  private static ConnectionFactory factory = null;
  private static Connection connection = null;
  private static ObjectPool<Channel> channelObjectPool = null;
  private static final int POOL_SIZE = 50;

  private static void setup() throws IOException, TimeoutException {
    if (factory == null) {
      // connects to the server
      factory = new ConnectionFactory();
      factory.setHost(HOST_NAME);
      factory.setPort(PORT_NUMBER);
      factory.setUsername(USER_NAME);
      factory.setPassword(PASSWORD);
    }

    if (connection == null) {
      connection = factory.newConnection();
    }

    /*
    if (channelObjectPool == null) {
      GenericObjectPoolConfig config = new GenericObjectPoolConfig();
      config.setMaxTotal(POOL_SIZE);
      channelObjectPool = new GenericObjectPool<Channel>(config);
    }
     */
  }

  public static void sendAMessage(LiftRide input) throws Exception {
    // construct a connection if first time connect
    setup();

    // open a new channel, use try to automatically close channel
    try (Channel channel = connection.createChannel()) {
      // create a channel after connection is established
      // sends one message once
      channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);

      channel.basicQos(1); // limit the number of unacknowledged messages to 1

      String message = input.toString();
      channel.basicPublish("", REQUEST_QUEUE_NAME, null,
          message.getBytes(StandardCharsets.UTF_8));

      System.out.println(" [x] Sent '" + message + "'");
    }
  }

  public static void main(String[] args) throws Exception {
    LiftRide ride = new LiftRide(1, 2, 3);
    Sender.sendAMessage(ride);
  }
}
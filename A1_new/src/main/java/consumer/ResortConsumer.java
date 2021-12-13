package consumer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Multi-threaded consumer, pulls message from RabbitMQ, and stores a record of individual
 * lift rides for each skier in the hash map
 */
public class ResortConsumer {

  //private static final String HOST_NAME = "3.92.133.65"; // public rabbitmq address
  private static final String HOST_NAME = "localhost";
  private static final int PORT_NUMBER = 5672;
  private static final String USER_NAME = "rainbow"; // remote rabbitmq user name
  private static final String PASSWORD = "123456"; // remote rabbitmq password
  private static final int NUM_THREADS = 128;
  private static final int TIME_OUT = 0;

  private static ConnectionFactory factory = null;
  private static Connection connection = null; // threads share one connection

  private static final Thread[] threads = new Thread[NUM_THREADS];

  private static void setupConnection() throws Exception {
    // creates a factory for connection and channel
    if (factory == null) {
      factory = new ConnectionFactory();
      //factory.setUsername(USER_NAME);
      //factory.setPassword(PASSWORD);
      factory.setHost(HOST_NAME);
      factory.setPort(PORT_NUMBER);
      factory.setRequestedHeartbeat(TIME_OUT);
    }

    if (connection == null) {
      connection = factory.newConnection();
    }

    // create threads, connections, and channels
    for (int i = 0; i < NUM_THREADS; i++) {
      threads[i] = new ResortConsumerThread(connection);
    }

    System.out.println("waiting for messages...");
  }

  /**
   * Pulls message from RabbitMQ and store in a hashmap
   */
  public static void consumeMessages() throws Exception {
    // set up for the connection for the first time
    setupConnection();

    // let threads consume messages
    for (Thread thread : threads) {
      thread.start();
    }
  }

  public static void main(String[] args) throws Exception {
    ResortConsumer.consumeMessages();
  }
}


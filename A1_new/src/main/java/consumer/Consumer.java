package consumer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import info.LiftRide;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-threaded consumer, pulls message from RabbitMQ, and stores a record of individual
 * lift rides for each skier in the hash map
 */
public class Consumer {
  private static final String HOST_NAME = "localhost";
  private static final int PORT_NUMBER = 5672;
  private static final int NUM_THREADS = 10;

  private static ConnectionFactory factory = null;
  private static Connection connection = null; // threads share one connection

  private static final ConsumerThread[] threads = new ConsumerThread[NUM_THREADS];
  private static final Map<Integer, List<LiftRide>> skierToRides = new ConcurrentHashMap<>();

  private static void setupConnection() throws Exception {
    // creates a factory for connection and channel
    if (factory == null) {
      factory = new ConnectionFactory();
      factory.setHost(HOST_NAME);
      factory.setPort(PORT_NUMBER);
    }
    if (connection == null) {
      connection = factory.newConnection();
    }
    // create threads, connections, and channels
    for (int i = 0; i < NUM_THREADS; i++) {
      threads[i] = new ConsumerThread(connection, skierToRides);
    }
  }

  private static void consumeMessages() throws Exception {
    // set up for the connection for the first time
    setupConnection();

    // let threads to consume messages
    for (ConsumerThread thread : threads) {
      thread.start();
    }
  }

  public static void main(String[] args) throws Exception {
    Consumer.consumeMessages();
  }
}

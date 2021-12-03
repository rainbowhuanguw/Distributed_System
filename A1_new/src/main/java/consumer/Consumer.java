package consumer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import database.ResortDBConnector;
import database.SkierDBConnector;
import info.LiftRide;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-threaded consumer, pulls message from RabbitMQ, and stores a record of individual
 * lift rides for each skier in the hash map
 */
public class Consumer {
  //private static final String HOST_NAME = "52.201.226.244"; // public rabbitmq address
  private static final String HOST_NAME = "localhost";
  private static final int PORT_NUMBER = 5672;
  private static final String USER_NAME = "rainbow"; // rabbitmq user name
  private static final String PASSWORD = "123456"; // rabbitmq password
  private static final int NUM_THREADS = 2;

  private static ConnectionFactory factory = null;
  private static Connection connection = null; // threads share one connection

  private static final ConsumerThread[] threads = new ConsumerThread[NUM_THREADS];

  // outer key : skierId, value : nested map
  // first-level key : seasonId, value : nested map
  // second-level key : dayId, value : nested map
  // third-level key : liftId, value : number of lift rides
  private static final Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>> skierToRides
      = new ConcurrentHashMap<>();

  private static void setupConnection() throws Exception {
    // creates a factory for connection and channel
    if (factory == null) {
      factory = new ConnectionFactory();
      //factory.setUsername(USER_NAME);
      //factory.setPassword(PASSWORD);
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

    System.out.println("waiting for messages...");
  }

  /**
   * Pulls message from RabbitMQ and store in a hashmap
   */
  public static void consumeMessages() throws Exception {
    // set up for the connection for the first time
    setupConnection();

    // let threads consume messages
    for (ConsumerThread thread : threads) {
      thread.start();
    }
  }

  /**
   * After hashmap is done, write to database
   */
  public static void writeToDB() throws SQLException {
    for (int skierId : skierToRides.keySet()) {
      for (int season : skierToRides.get(skierId).keySet()) {
        for (int day : skierToRides.get(skierId).get(season).keySet()) {
          for (int liftId : skierToRides.get(skierId).get(skierId).get(day).keySet()) {
            SkierDBConnector.write(
                skierId,
                season,
                day,
                liftId,
                skierToRides.get(skierId).get(skierId).get(day).get(liftId)
            );
          }
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Consumer.consumeMessages();
    Consumer.writeToDB();
  }
}

package consumer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import database.ResortDBConnector;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import util.Counter;

/**
 * Multi-threaded consumer, pulls message from RabbitMQ, and stores a record of individual
 * lift rides for each skier in the hash map
 */
public class ResortConsumer {

  //private static final String HOST_NAME = "52.201.226.244"; // public rabbitmq address
  private static final String HOST_NAME = "localhost";
  private static final int PORT_NUMBER = 5672;
  private static final String USER_NAME = "rainbow"; // rabbitmq user name
  private static final String PASSWORD = "123456"; // rabbitmq password
  private static final int NUM_THREADS = 10;
  private static final int totalMessages = 82; // TODO: delivered requests 128 threads

  private static ConnectionFactory factory = null;
  private static Connection connection = null; // threads share one connection
  // count how many messages have been processed
  private static final CountDownLatch countDownLatch = new CountDownLatch(totalMessages);

  private static final Counter counter = new Counter();
  private static final Thread[] threads = new Thread[NUM_THREADS];

  // outer key : resortId, value : nested map
  // first-level key : season, value : nested map
  // second-level key : day, value : nested map
  // third-level key: hour, value: nested map
  // fourth-level key: skierId, value : nested map
  // fifth-level key : liftId, value : number of lift rides
  private static final Map<Integer, Map<Integer, Map<Integer,
                            Map<Integer, Map<Integer, Map<Integer, Integer>>>>>> resortToRides
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
      threads[i] = new ResortConsumerThread(connection, resortToRides, countDownLatch, counter);
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

    // wait for all consumer threads to finish
    countDownLatch.await();

    // close connection manually
    connection.close();
  }

  /**
   * After hashmap is done, write to database
   */
  public static void writeToDB() throws SQLException {
    for (int resortId : resortToRides.keySet()) {
      for (int season : resortToRides.get(resortId).keySet()) {
        for (int day : resortToRides.get(resortId).get(season).keySet()) {
          for (int hour : resortToRides.get(resortId).get(season).get(day).keySet()) {
            for (int liftId : resortToRides.get(resortId).get(season).get(day).
                                                get(hour).keySet()) {
              for (int skierId : resortToRides.get(resortId).get(season).get(day).get(hour)
                                              .get(liftId).keySet()) {
                ResortDBConnector.write(
                    resortId,
                    season,
                    day,
                    liftId,
                    skierId,
                    resortToRides.get(resortId).get(season).get(day).get(hour)
                        .get(liftId).get(skierId)
                );
              }
            }
          }
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    ResortConsumer.consumeMessages();
    ResortConsumer.writeToDB();
  }
}


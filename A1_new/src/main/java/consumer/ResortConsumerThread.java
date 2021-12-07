
package consumer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import util.Counter;

/**
 * Custom thread class to consume messages from rabbitmq
 */
public class ResortConsumerThread extends Thread {
  private static final String REQUEST_QUEUE_NAME = "resortQueue";
  private static final String DELIM = ",";

  private static Map<Integer, Map<Integer, Map<Integer,
      Map<Integer, Map<Integer, Map<Integer, Integer>>>>>> resortToRides;
  private static Channel channel;
  private static CountDownLatch countDownLatch;
  private static Counter counter;

  public ResortConsumerThread(Connection connection, Map<Integer, Map<Integer, Map<Integer,
      Map<Integer, Map<Integer, Map<Integer, Integer>>>>>> resortToRides,
      CountDownLatch countDownLatch, Counter counter)
      throws Exception {
    // set up channel
    channel = connection.createChannel();
    ResortConsumerThread.resortToRides = resortToRides;
    ResortConsumerThread.countDownLatch = countDownLatch;
    ResortConsumerThread.counter = counter;
  }

  @Override
  public void run() {
    try {
      consumeAMessage();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void consumeAMessage() throws Exception {
    // stays active to listen
    channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);

    // gets callback/messages from queue
    DeliverCallback deliverCallback = (consumerTag, deliver) -> {
      String message = new String(deliver.getBody(), StandardCharsets.UTF_8);
      System.out.println("[x] Received '" + message + "'");

      // parse the message into key and values in map
      String[] split = message.split(DELIM);
      int skierId = Integer.parseInt(split[0]),
          liftId = Integer.parseInt(split[1]),
          hour = Integer.parseInt(split[2]),
          season = Integer.parseInt(split[3]),
          day = Integer.parseInt(split[4]);

      // outer-level key check resortId
      resortToRides.putIfAbsent(skierId, new ConcurrentHashMap<>());
      // first-level key check - season
      resortToRides.get(skierId).putIfAbsent(season, new ConcurrentHashMap<>());
      // second-level key check - day
      resortToRides.get(skierId).get(season).putIfAbsent(day, new ConcurrentHashMap<>());
      // third-level key check - hour
      resortToRides.get(skierId).get(season).get(day).putIfAbsent(hour, new ConcurrentHashMap<>());
      // fourth-level key check - lift ride id
      resortToRides.get(skierId).get(season).get(day).get(hour).putIfAbsent(liftId,
          new ConcurrentHashMap<>());
      // fifth-level - skierid
      int count = resortToRides.get(skierId).get(season).get(day).get(hour).get(liftId)
          .getOrDefault(skierId, 0);

      // update value
      resortToRides.get(skierId).get(season).get(day).get(hour).get(liftId).put(skierId, count + 1);

      // count down
      countDownLatch.countDown();
      //counter.increment();
      //System.out.println(" " + counter.getVal());
    };

    // auto ack
    channel.basicConsume(REQUEST_QUEUE_NAME, true, deliverCallback, consumerTag -> { });
  }
}
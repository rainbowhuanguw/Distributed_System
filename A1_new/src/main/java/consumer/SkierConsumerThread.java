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
public class SkierConsumerThread extends Thread {
  private static final String SKIER_QUEUE_NAME = "skierQueue";
  private static final String DELIM = ",";

  private static Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>> skierToRides;
  private static Channel channel;
  private static CountDownLatch countDownLatch;
  private static Counter counter;

  public SkierConsumerThread( Connection connection,
      Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>> map,
      CountDownLatch countDownLatch, Counter counter)
      throws Exception {
    // set up channel
    channel = connection.createChannel();
    SkierConsumerThread.skierToRides = map;
    SkierConsumerThread.countDownLatch = countDownLatch;
    SkierConsumerThread.counter = counter;
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
    channel.queueDeclare(SKIER_QUEUE_NAME, false, false, false, null);

    // gets callback/messages from queue
    DeliverCallback deliverCallback = (consumerTag, deliver) -> {
      String message = new String(deliver.getBody(), StandardCharsets.UTF_8);
      System.out.println("[x] Received '" + message + "'");

      // parse the message into key and values in map
      String[] split = message.split(DELIM);
      int skierId = Integer.parseInt(split[0]),
          liftId = Integer.parseInt(split[1]),
          time = Integer.parseInt(split[2]),
          seasonId = Integer.parseInt(split[3]),
          dayId = Integer.parseInt(split[4]);

      // outer key check
      skierToRides.putIfAbsent(skierId, new ConcurrentHashMap<>());
      // first level key check - season
      skierToRides.get(skierId).putIfAbsent(seasonId, new ConcurrentHashMap<>());
      // second level key check - day
      skierToRides.get(skierId).get(seasonId).putIfAbsent(dayId, new ConcurrentHashMap<>());
      // third level key check - lift ride id
      int count = skierToRides.get(skierId).get(seasonId).get(dayId).getOrDefault(liftId, 0);
      // update value
      skierToRides.get(skierId).get(seasonId).get(dayId).put(liftId, count + 1);

      // count down
      countDownLatch.countDown();
      //counter.increment();
      //System.out.println(" " + counter.getVal());
    };

    // auto ack
    channel.basicConsume(SKIER_QUEUE_NAME, true, deliverCallback, consumerTag -> { });
  }
}
package consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import info.LiftRide;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom thread class to consume messages from rabbitmq
 */
public class ConsumerThread extends Thread {
  private static final String REQUEST_QUEUE_NAME = "mq";
  private static final String DELIM = ",";

  private static Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>> skierToRides;
  private static Channel channel;

  public ConsumerThread(Connection connection,
      Map<Integer, Map<Integer, Map<Integer, Map<Integer, Integer>>>>map) throws Exception {
    // set up channel
    channel = connection.createChannel();
    skierToRides = map;
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
      //System.out.println("[x] Received '" + message + "'");

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
      skierToRides.get(skierId).get(seasonId).get(dayId).putIfAbsent(liftId, 0);

      // update value
      int res = skierToRides.get(skierId).get(seasonId).get(dayId).get(liftId);
      skierToRides.get(skierId).get(seasonId).get(dayId).put(liftId, res + 1);
    };

    // auto ack
    channel.basicConsume(REQUEST_QUEUE_NAME, true, deliverCallback, consumerTag -> { });
  }
}
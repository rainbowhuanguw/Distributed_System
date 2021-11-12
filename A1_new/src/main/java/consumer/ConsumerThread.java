package consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import info.LiftRide;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom thread class to consume messages from rabbitmq
 */
public class ConsumerThread extends Thread {
  private static final String REQUEST_QUEUE_NAME = "mq";
  private static Map<Integer, List<LiftRide>> skierToRides;
  private static Channel channel;

  public ConsumerThread(Connection connection, Map<Integer, List<LiftRide>> map)
      throws Exception {
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
      String[] split = message.split(",");
      int skierId = Integer.parseInt(split[0]), liftId = Integer.parseInt(split[1]),
          time = Integer.parseInt(split[2]);
      skierToRides.putIfAbsent(skierId, new ArrayList<>());
      skierToRides.get(skierId).add(new LiftRide(skierId, liftId, time));
    };

    // auto ack
    channel.basicConsume(REQUEST_QUEUE_NAME, true, deliverCallback, consumerTag -> { });
  }
}
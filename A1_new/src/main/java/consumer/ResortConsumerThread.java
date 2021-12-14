
package consumer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import database.ResortDBWriter;
import java.sql.SQLException;

/**
 * Custom thread class to consume messages from rabbitmq
 */
public class ResortConsumerThread extends Thread {
  private static final String QUEUE_NAME = "resortQueue";

  private static Channel channel;
  private final ResortDBWriter writer;

  public ResortConsumerThread(Connection connection)
      throws Exception {
    // set up channel
    channel = connection.createChannel();
    writer = new ResortDBWriter();
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
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");

      try {
        writer.write(message);
        // positively acknowledge a single delivery, the message will be discarded
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

      } catch (SQLException throwables) {
        throwables.printStackTrace();
        // negatively acknowledge a single delivery, the message will be discarded
        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
      }
    };

    // manual ack
    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
  }
}
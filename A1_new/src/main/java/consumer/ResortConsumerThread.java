
package consumer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import database.ResortDBConnector;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Custom thread class to consume messages from rabbitmq
 */
public class ResortConsumerThread extends Thread {
  private static final String QUEUE_NAME = "resortQueue";

  private static Channel channel;

  public ResortConsumerThread(Connection connection)
      throws Exception {
    // set up channel
    channel = connection.createChannel();
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

    // gets callback/messages from queue
    DeliverCallback deliverCallback = (consumerTag, deliver) -> {
      String message = new String(deliver.getBody(), StandardCharsets.UTF_8);
      //System.out.println("[x] Received '" + message + "'");

      try {
        ResortDBConnector.write(message);
      } catch (SQLException throwables) {
        throwables.printStackTrace();
      }
    };

    // auto ack
    channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
  }
}
package consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Envelope;
import database.SkierDBWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Custom thread class to consume messages from rabbitmq
 */
public class SkierConsumerThread extends Thread {
  private static final String QUEUE_NAME = "skierQueue";

  private static Channel channel;

  public SkierConsumerThread( Connection connection)
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
        SkierDBWriter.write(message);
      } catch (SQLException throwables) {
        throwables.printStackTrace();
      }
    };

    // manual ack
    channel.basicConsume(QUEUE_NAME, false, "",
        new DefaultConsumer(channel) {
          @Override
          public void handleDelivery(String consumerTag, Envelope envelope,
              AMQP.BasicProperties properties, byte[] body) throws IOException {
            long deliveryTag = envelope.getDeliveryTag();
            // positively acknowledge a single delivery, the message will be discarded
            channel.basicAck(deliveryTag, true); // batch process
          }
        });
  }
}
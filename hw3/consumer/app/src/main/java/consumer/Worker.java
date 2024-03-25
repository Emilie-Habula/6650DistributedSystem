package consumer;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import consumer.dbclient.DDBClient;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class Worker implements Runnable {
  private Gson gson = new Gson();// Initialize a Gson object for JSON serialization
  private static final String QUEUE_NAME = "LIFT_RIDE"; // Initializethe RabbitMQ queue where the data will be consumed.

  private Connection connection;

  private DDBClient ddbClient;

  public Worker(Connection connection) {
    this.connection = connection;
    this.ddbClient = new DDBClient();
  }

  @Override
  public void run() {
    try {
      Channel channel = connection.createChannel();
      channel.queueDeclare(QUEUE_NAME, true, false, false, null);
      channel.basicConsume(QUEUE_NAME, true, new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
            byte[] body) throws IOException {
          String message = new String(body, "UTF-8");
          System.out.println("[x] Received '" + message + "'");
          try {
            LiftRide liftRide = gson.fromJson(message, LiftRide.class);
            ddbClient.PutItem(liftRide);
          } catch (JsonSyntaxException | JsonIOException e) {
            e.printStackTrace();
            throw e;
          }
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}

package com.bulaha.consumer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class LiftRideConsumer {

  private static final String SERVER = "ec2-54-245-158-151.us-west-2.compute.amazonaws.com";

  private static final String USERNAME = "bulaha";

  private static final String PASSWORD = "password";

  private static final int PORT = 5672;
  private final static int NUM_THREADS = 5; // Number of threads for consuming messages
  private final ConcurrentHashMap<Integer, Integer> skierLiftRidesMap = new ConcurrentHashMap<>();

  public void consume() throws IOException, TimeoutException {
    // Set up connection to RabbitMQ server
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(SERVER);
    factory.setPort(PORT);
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);

    // Create thread pool for consuming messages
    ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
    Connection connection = factory.newConnection(executorService);

    // Start consuming messages
    for (int i = 0; i < NUM_THREADS; i++) {
      executorService.execute(new Worker(connection, skierLiftRidesMap));
    }
  }

  public static void main(String[] args) throws Exception {
    LiftRideConsumer consumer = new LiftRideConsumer();
    consumer.consume();
  }
}

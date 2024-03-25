package com.upic.upicserver;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.upic.upicserver.rmqpool.RMQChannelFactory;
import com.upic.upicserver.rmqpool.RMQChannelPool;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * The servlet class extends HttpServlet to handle HTTP requests.
 * It overrides the doPost method to handle POST requests.
 * Path parameters are extracted from the request URL using req.getPathInfo().split("/").
 * Input parameters are validated, and appropriate error responses are sent if validation fails.
 * A Gson object is used for JSON serialization.
 * A LiftRide object is created with a skier id and a message ("bula").
 * The response is converted to a JSON string and sent back with the appropriate content type.
 */
public class WriteNewLiftRideServlet extends HttpServlet {
  private Gson gson = new Gson();// Initialize a Gson object for JSON serialization

  private static final String QUEUE_NAME = "LIFT_RIDE"; // Initializethe RabbitMQ queue where the data will be sent.
  private static final String SERVER = "ec2-54-245-158-151.us-west-2.compute.amazonaws.com";

  private static final String USERNAME = "bulaha";

  private static final String PASSWORD = "password";

  private static final int PORT = 5672;
  private RMQChannelPool channelPool;

  public void init() throws ServletException {
    // construct new connection
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(SERVER);
    factory.setPort(PORT);
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);
    Connection conn;

    try {
      conn = factory.newConnection();
    } catch (IOException | TimeoutException e) {
      throw new ServletException(e);
    }
    System.out.println("INFO: RabbitMQ connection established");
    channelPool = new RMQChannelPool(100, new RMQChannelFactory(conn));
    System.out.println("INFO: RabbitMQ pool created");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    // Validate input params
    String[] pathParams = req.getPathInfo()
        .split("/");// Split the path parameters from the request URL
    System.out.println(Arrays.toString(pathParams));// Print the path parameters (for debugging)

    // Check if the number of path parameters is not equal to 8
    if (pathParams.length != 8) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "parameters mismatch");
      return;
    }

    // Check if the resort id is present or not numeric
    if (StringUtils.isEmpty(pathParams[1]) || !StringUtils.isNumeric(pathParams[1])) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "unsupported resort id");//Return a 400 Bad Request with an error message
      return;
    }

    // Check if the season id is present or equals to 2024
    if (StringUtils.isEmpty(pathParams[3]) || !pathParams[3].equals("2024")) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "unsupported season id");//Return a 400 Bad Request with an error message
      return;
    }

    // Check if the skier id is present or not numeric
    if (StringUtils.isEmpty(pathParams[7]) || !StringUtils.isNumeric(pathParams[7])) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "unsupported skier id");// Return a 400 Bad Request with an error message
      return;
    }
    // Check if the day id is present or equals to 1
    if (StringUtils.isEmpty(pathParams[5]) || !pathParams[5].equals("1")) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "unsupported day id");// Return a 400 Bad Request with an error message
      return;
    }

    // Validate resortID
    int resortID = Integer.parseInt(pathParams[1]);
    if (resortID < 1 || resortID > 10) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Invalid resortID");// Return a 400 Bad Request with an error message
      return;
    }

    // Validate seasonID
    String seasonID = pathParams[3];
    if (!seasonID.equals("2024")) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Invalid seasonID");// Return a 400 Bad Request with an error message
      return;
    }

    // Validate dayID
    String dayID = pathParams[5];
    if (dayID.isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Invalid dayID");// Return a 400 Bad Request with an error message
      return;
    }

    // Validate skierID
    int skierID = Integer.parseInt(pathParams[7]);
    if (skierID < 1 || skierID > 100000) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Invalid skierID");// Return a 400 Bad Request with an error message
      return;
    }

    // Deserialize JSON request body to a LiftRide object
    LiftRide liftRide = null;
    try {
      liftRide = gson.fromJson(req.getReader(), LiftRide.class);
    } catch (JsonSyntaxException | JsonIOException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Invalid request body");// Return a 400 Bad Request with an error message
      return;
    }

    // Check if the LiftRide object is valid
    if (!liftRide.isValid()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Invalid LiftRide object");// Return a 400 Bad Request with an error message
      return;
    }

    //set other metadata
    liftRide.setDayId(dayID);
    liftRide.setResortId(resortID);
    liftRide.setSeasonId(seasonID);
    liftRide.setSkierId(skierID);

    // Convert the LiftRide object to a JSON string
    String json = gson.toJson(liftRide);

    // Send the JSON string to the RabbitMQ channel
    try {
      Channel channel = channelPool.borrowObject();
      channel.queueDeclare(QUEUE_NAME, true, false, false, null);
      channel.basicPublish("", QUEUE_NAME, null, json.getBytes());
      channelPool.returnObject(channel);
    } catch (Exception e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to send message to RabbitMQ");
      return;
    }

    // Return success response to the client
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.getWriter().println("Data sent to RabbitMQ");

  }
}
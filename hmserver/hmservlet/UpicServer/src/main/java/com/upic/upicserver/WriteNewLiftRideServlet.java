package com.upic.upicserver;

import com.google.gson.Gson;
import java.io.*;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

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

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Validate input params
    String[] pathParams = req.getPathInfo().split("/");// Split the path parameters from the request URL
    System.out.println(Arrays.toString(pathParams));// Print the path parameters (for debugging)

    // Check if the number of path parameters is not equal to 8
    if(pathParams.length != 8) {
      resp.sendError(400, "parameters mismatch");
      return;
    }
    // Check if the resort id is not numeric
    if(!StringUtils.isNumeric(pathParams[1])) {
      resp.sendError(400, "unsupported resort id");//Return a 400 Bad Request with an error message
      return;
    }
    // Check if the skier id is not numeric
    if(!StringUtils.isNumeric(pathParams[7])) {
      resp.sendError(400, "unsupported skier id");// Return a 400 Bad Request with an error message
      return;
    }

    // Generate random response (creating a LiftRide object with skier id and message)
    LiftRide ride = new LiftRide(Integer.parseInt(pathParams[7]),"bula");
    // Convert the LiftRide object to a JSON string
    String respString = this.gson.toJson(ride);

    // Return response
    PrintWriter out = resp.getWriter();// Get the PrintWriter for the response
    resp.setContentType("application/json");// Set the content type to JSON
    resp.setCharacterEncoding("UTF-8");// Set the character encoding
    out.print(respString);// Write the JSON response
    out.flush();// Flush the output
  }
}
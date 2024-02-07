package util;

import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import model.SkierRideEvent;

/**
 * The EventGenerator class is responsible for generating SkierRideEvent instances with random
 * values and managing a blocking queue to store these events.
 * It implements the Runnable interface, allowing it to be executed in a separate thread.
 * The get() method retrieves a SkierRideEvent from the blocking queue (eventQueue).
 * The generateEvents method generates the specified number of events and adds them to
 * the blocking queue.
 * Random values are used for skier id, resort id, lift id, season id, day id, and time.
 * Messages are printed during the generation process to indicate progress.
 * The class uses BlockingDeque (specifically, LinkedBlockingDeque) to safely handle
 * concurrent access to the event queue.
 * Appropriate error handling is implemented, including handling interruptions
 * and printing error messages.
 */

// A class responsible for generating SkierRideEvent instances and managing a blocking queue
public class EventGenerator implements Runnable {
  // Random number generator for generating random values
  private final Random RANDOM = new Random();

  // Number of requests to be generated
  private final int numOfRequests;

  // Blocking queue to store generated SkierRideEvent instances
  private final BlockingDeque<SkierRideEvent> eventQueue = new LinkedBlockingDeque<>();

  // Constructor to initialize the EventGenerator with the number of requests
  public EventGenerator(int numOfRequests) {
    this.numOfRequests = numOfRequests;
  }

  // Run method required by the Runnable interface
  @Override
  public void run() {
    // Invoke the method to generate events
    generateEvents(numOfRequests);
  }

  // Method to retrieve a SkierRideEvent from the eventQueue
  public SkierRideEvent get() {
    try {
      return eventQueue.take();
    } catch (InterruptedException e) {
      // Handle interruption and print an error message
      Thread.currentThread().interrupt();
      System.err.println("Event generation error");
      e.printStackTrace();
      return null;
    }
  }

  // Method to generate SkierRideEvent instances and add them to the eventQueue
  private void generateEvents(int numOfRequests) {
    for(int i = 0; i < numOfRequests; i++) {
      // Generate a new SkierRideEvent with random values
      SkierRideEvent event = new SkierRideEvent(RANDOM.nextInt(100000) + 1,// Random skier id between 1 and 100000
          RANDOM.nextInt(10) + 1, // Random resort id between 1 and 10
          RANDOM.nextInt(40) + 1, // Random lift id between 1 and 40
          "2024", // Season id
          "1",   // Day id
          RANDOM.nextInt(360) + 1 // Random time between 1 and 360
      );
      try {
        // Put the generated event into the eventQueue
        eventQueue.put(event);
        // Print a message every 1000 events generated
        if(i % 1000 == 999) {
          System.out.println("Event " + (i + 1) + " generated");
        }
      } catch (InterruptedException e) {
        // Handle interruption and print an error message
        Thread.currentThread().interrupt();
        System.err.println("Event generation error");
        e.printStackTrace();
      }
    }
    // Print a message when events generation is completed
    System.out.println("Events generation completed");
  }
}

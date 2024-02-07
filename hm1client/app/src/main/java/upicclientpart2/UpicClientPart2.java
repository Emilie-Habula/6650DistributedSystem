package upicclientpart2;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import model.AnalysisResult;
import util.EventGenerator;
import util.FileUtil;
import util.RequestSender;

/**
 * This is an extension of the UpicClientPart1, adding file handling and result analysis.
 * It introduces a FileUtil class for file-related operations.
 * The main method waits for a separate event generator thread to start before
 * creating and starting multiple threads to simulate concurrent requests.
 * The threads are responsible for sending requests and generating events.
 * It measures the number of successful and failed requests, the total runtime, and the throughput.
 * After all threads finish, it performs an analysis on the generated files to
 * calculate mean, median, and p99 times.
 * The program outputs various statistics at the end of execution.
 */

// Main class representing the UpicClientPart2 application
public class UpicClientPart2 {
  // EC2 server path
  private static final String EC2_PATH = "http://ec2-35-87-80-19.us-west-2.compute.amazonaws.com:8080/UpicServer-1.0-SNAPSHOT";

  // Number of threads to be created
  private static final int NUM_THREADS = 32;

  // Number of requests per thread
  private static final int REQUESTS_PER_THREAD = 1000;

  // Total number of requests to be sent
  private static final int TOTAL_REQUESTS = 200000;

  // Main method
  public static void main(String[] args) throws InterruptedException, IOException {
    // Atomic counter for tracking the number of failures
    AtomicInteger failureCount = new AtomicInteger();

    // Latch for synchronization among threads
    CountDownLatch totalLatch = new CountDownLatch(NUM_THREADS * 2);

    // Latch for signaling the event generator thread to start
    CountDownLatch singleLatch = new CountDownLatch(1);

    //Create separate thread to generate events
    EventGenerator eventGenerator = new EventGenerator(TOTAL_REQUESTS);
    new Thread(eventGenerator).start();

    // Utility class for file-related operations
    FileUtil fileUtil = new FileUtil();

    // Array to store threads
    Thread[] threads = new Thread[2 * NUM_THREADS];

    // Record start time for measuring runtime
    long startTime = System.currentTimeMillis();

    System.out.println("Creating threads");

    // Create and start the first set of threads
    for (int i = 0; i < NUM_THREADS; i++) {
      threads[i] = new Thread(new RequestSender(REQUESTS_PER_THREAD, totalLatch, singleLatch, EC2_PATH, i,
          failureCount, eventGenerator, fileUtil));
    }

    for (int i = 0; i < NUM_THREADS; i++) {
      threads[i].start();
    }

    // Wait for the event generator thread to start
    singleLatch.await();

    System.out.println("Creating new threads");

    // Calculate average remaining requests for additional threads
    int avgRemainingRequests = (TOTAL_REQUESTS - REQUESTS_PER_THREAD * NUM_THREADS)/ NUM_THREADS;

    // Create and start the second set of threads
    for (int i = NUM_THREADS; i < 2 * NUM_THREADS; i++) {
      threads[i] = new Thread(new RequestSender(avgRemainingRequests, totalLatch, null, EC2_PATH,
          i, failureCount, eventGenerator, fileUtil));
    }

    for (int i = NUM_THREADS; i < 2 * NUM_THREADS; i++) {
      threads[i].start();
    }

    // Wait for all threads to finish
    totalLatch.await();

    // Record end time for measuring runtime
    long endTime = System.currentTimeMillis();

    // Output results
    System.out.println("Total number of success requests:" + (TOTAL_REQUESTS - failureCount.get()));
    System.out.println("Total number of failure requests:" + failureCount.get());
    System.out.println("Total runtime: " + (endTime - startTime)/1000 + " seconds");
    System.out.println("Total throughout: " + TOTAL_REQUESTS * 1000/(endTime - startTime) + " requests/seconds");

    // Perform analysis on the generated files
    AnalysisResult analysisResult = fileUtil.analysis(TOTAL_REQUESTS);
    if(analysisResult != null) {
      System.out.println("Mean time of all requests:" + analysisResult.getAvgTime() + " milliseconds");
      System.out.println("Median time of all requests:" + analysisResult.getMedianTime() + " milliseconds");
      System.out.println("P99 time of all requests:" + analysisResult.getP99Time() + " milliseconds");
    }
  }
}

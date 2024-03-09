package upicclientpart2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import model.AnalysisResult;
import util.EventGenerator;
import util.FileUtil;
import util.RequestSender;
import util.ResultData;

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
  // single instance address
  //"http://ec2-54-213-225-157.us-west-2.compute.amazonaws.com:8080/UpicServer-1.0-SNAPSHOT";

  // load balance address
  //"http://UpicLB-1428261547.us-west-2.elb.amazonaws.com:80/UpicServer-1.0-SNAPSHOT";
  private static final String EC2_PATH = "http://UpicLB-1428261547.us-west-2.elb.amazonaws.com:80/UpicServer-1.0-SNAPSHOT";
  // Number of threads to be created
  private static final int NUM_THREADS = 32;

  // Number of requests per thread
  private static final int REQUESTS_PER_THREAD = 1000;

  // Total number of requests to be sent
  private static final int TOTAL_REQUESTS = 200000;

  private static final int PHASE2_THREAD_NUMBER = 200;

  private static void writeResultsToCsv(List<ResultData> resultData, String fileName) {
    try (FileWriter writer = new FileWriter(fileName)) {
      writer.append("StartTime,EndTime,StatusCode,Type,RequestTime\n");
      for (ResultData data : resultData) {
        double requestTime = data.endTime() - data.startTime();
        writer.append(String.format("%f,%f,%d,%s,%f\n", data.startTime(), data.endTime(), data.statusCode(), data.type(), requestTime));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  private static void calculateStatistics(List<ResultData> resultData) {
    List<Double> requestTimes = new ArrayList<>();
    Map<Long, Integer> requestsPerSecond = new HashMap<>();

    for (ResultData data : resultData) {
      double requestTime = data.endTime() - data.startTime();
      requestTimes.add(requestTime);

      // 对endTime进行取整处理，转换为秒
      long endSecond = (long) data.endTime() / 1000;
      System.out.println(endSecond);
      requestsPerSecond.put(endSecond, requestsPerSecond.getOrDefault(endSecond, 0) + 1);
    }

    Collections.sort(requestTimes);

    double median = requestTimes.get(requestTimes.size() / 2);
    double average = requestTimes.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    double p99 = requestTimes.get((int) Math.ceil(99.0 / 100.0 * requestTimes.size()) - 1);

    // 打印每一秒的请求数量
    System.out.println("Requests per second:");
    System.out.println(requestsPerSecond.size());
    requestsPerSecond.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> System.out.println("Second " + entry.getKey() + ": " + entry.getValue()));

    // 打印统计值
    System.out.println("Starting to write statistics.");
    System.out.println("Median: " + median);
    System.out.println("Mean: " + average);
    System.out.println("P99: " + p99);
  }
  // Main method
  public static void main(String[] args) throws InterruptedException, IOException {
    // Atomic counter for tracking the number of failures
    AtomicInteger failureCount1 = new AtomicInteger();
    AtomicInteger failureCount2 = new AtomicInteger();
    //List for 储存了所有thread 里面的sender 内容
    List<RequestSender> requestSenderList = new ArrayList<>();
    // Atomic counter for tracking the number of failures
    AtomicInteger failureCount = new AtomicInteger();

    // Latch for synchronization among threads
    CountDownLatch totalLatch = new CountDownLatch(NUM_THREADS);
    CountDownLatch phase2latch = new CountDownLatch(PHASE2_THREAD_NUMBER);


    //Create separate thread to generate events
    EventGenerator eventGenerator = new EventGenerator(TOTAL_REQUESTS+100);
    new Thread(eventGenerator).start();

    // Array to store threads
    Thread[] threads = new Thread[NUM_THREADS + PHASE2_THREAD_NUMBER];

    // Record start time for measuring runtime
    long startTime = System.currentTimeMillis();

    System.out.println("Creating threads");

    // Create and start the first set of threads
    for (int i = 0; i < NUM_THREADS; i++) {
      RequestSender requestSender = new RequestSender(REQUESTS_PER_THREAD, totalLatch, EC2_PATH, i,
          failureCount, eventGenerator);
      requestSenderList.add(requestSender);
      threads[i] = new Thread(requestSender);
    }

    for (int i = 0; i < NUM_THREADS; i++) {
      threads[i].start();
    }
    totalLatch.await();
    double midTime = System.currentTimeMillis();
    System.out.println("Creating new threads");

    // Calculate average remaining requests for additional threads
    int avgRemainingRequests = (TOTAL_REQUESTS - REQUESTS_PER_THREAD * NUM_THREADS)/ PHASE2_THREAD_NUMBER;

    // Create and start the second set of threads
    for (int i = NUM_THREADS; i < NUM_THREADS + PHASE2_THREAD_NUMBER; i++) {

      RequestSender requestSender = new RequestSender(avgRemainingRequests, phase2latch, EC2_PATH, i,
          failureCount, eventGenerator);
      requestSenderList.add(requestSender);
      threads[i] = new Thread(requestSender);
    }

    for (int i = NUM_THREADS; i < NUM_THREADS + PHASE2_THREAD_NUMBER; i++) {
      threads[i].start();
    }

    // Wait for all threads to finish
    phase2latch.await();

    // Record end time for measuring runtime
    long endTime = System.currentTimeMillis();

    //for
    List<ResultData> resultData = new ArrayList<>();
    for(RequestSender requestSender : requestSenderList){
      resultData.addAll(requestSender.getResults());
    }


    writeResultsToCsv(resultData, "results.csv");
    calculateStatistics(resultData);

    // Output results
    System.out.println("Phase1 of success requests:" + (NUM_THREADS * REQUESTS_PER_THREAD - failureCount1.get()));
    System.out.println("Phase1 of failure requests:" + failureCount1.get());
    System.out.println("Phase1 Total runtime: " + (midTime - startTime)/1000 + " seconds");
    System.out.println("Phase1 Total throughout: " + NUM_THREADS * REQUESTS_PER_THREAD * 1000/(endTime - startTime) + " requests/seconds");

    // Output results
    System.out.println("Phase2 of success requests:" + (TOTAL_REQUESTS - NUM_THREADS * REQUESTS_PER_THREAD - failureCount2.get()));
    System.out.println("Phase2 of failure requests:" + failureCount2.get());
    System.out.println("Phase2 Total runtime: " + (endTime - midTime)/1000 + " seconds");
    System.out.println("Phase2 Total throughout: " + (TOTAL_REQUESTS - NUM_THREADS * REQUESTS_PER_THREAD) * 1000/(endTime - startTime) + " requests/seconds");
  }
}

package util;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import model.SkierRideEvent;

/**
 * The RequestSender class is responsible for sending requests to the SkiersApi to write new lift rides.
 * It implements the Runnable interface, allowing it to be executed in a separate thread.
 * Two constructors are provided, allowing the optional injection of a FileUtil instance for
 * writing data to a CSV file.
 * The run method contains the logic for processing each request,
 * including retries in case of failures.
 * The start time and end time are recorded for measuring request processing time.
 * Progress messages are printed every 100 processed requests.
 * The class uses AtomicInteger (failureCount) to count the number of failed requests.
 * Error handling is implemented for ApiException, including printing error messages and stack traces.
 */

// A class responsible for sending requests to the SkiersApi to write new lift rides
public class RequestSender implements Runnable {

  private final static int MAX_RETRY = 5;

  private final int requestsToSend;

  private final CountDownLatch totalLatch;


  private final SkiersApi apiInstance;

  private final int threadNum;

  private final AtomicInteger failureCount;

  private final EventGenerator eventGenerator;

  private final FileUtil fileUtil;

  private List<ResultData> results = new ArrayList<>();

  public RequestSender(int requestsToSend, CountDownLatch totalLatch, String path,
      int threadNum, AtomicInteger failureCount,
      EventGenerator eventGenerator) {
    this(requestsToSend, totalLatch, path, threadNum, failureCount, eventGenerator, null);
  }

  public RequestSender(int requestsToSend, CountDownLatch totalLatch, String path,
      int threadNum, AtomicInteger failureCount,
      EventGenerator eventGenerator, FileUtil fileUtil) {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(path);
    this.apiInstance = new SkiersApi(apiClient);
    this.requestsToSend = requestsToSend;
    this.totalLatch = totalLatch;
    this.threadNum = threadNum;
    this.failureCount = failureCount;
    this.eventGenerator = eventGenerator;
    this.fileUtil = fileUtil;
  }

  @Override
  public void run() {
    System.out.println("Thread " + threadNum + " started");
    for (int i = 0; i < requestsToSend; i++) {
      SkierRideEvent event = eventGenerator.get();
      LiftRide liftRide = new LiftRide();
      liftRide.setLiftID(event.getLiftId());
      liftRide.setTime(event.getTime());

      long startTime = System.currentTimeMillis();
      int statusCode = 0;
      int retry;
      for(retry = 0; retry < MAX_RETRY; retry++) {
        try {
          ApiResponse<Void> response = apiInstance.writeNewLiftRideWithHttpInfo(liftRide,
              event.getResortId(), event.getSeasonId(), event.getDayId(), event.getSkierId());
          statusCode = response.getStatusCode();
          if (statusCode / 100 == 2) {
            if (i % 100 == 99) {
              System.out.println("Thread " + threadNum + " processed " + (i + 1) + " requests");
            }
            break;
          }
        } catch (Exception e) {
          // continue the retry
        }
      }
      if(retry == MAX_RETRY) {
        failureCount.incrementAndGet();
        System.out.println("Thread " + threadNum + " failed to process " + (i+1) + " requests");
      }
      long endTime = System.currentTimeMillis();
      results.add(new ResultData(startTime, endTime, statusCode, "POST"));
    }
    totalLatch.countDown(); // Signal that this thread has completed


    System.out.println("Thread " + threadNum + " completed");
  }

  public List<ResultData> getResults(){
    return results;
  }
}

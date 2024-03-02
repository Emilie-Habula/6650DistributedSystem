package upicclientpart1;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

public class OneThreadClient {

  public static void main(String[] args) {
    //构建API sender，循环发送10000个请求，记录总时间并打印
    long startTime = System.currentTimeMillis();
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath("http://ec2-54-202-208-244.us-west-2.compute.amazonaws.com:8080/UpicServer-1.0-SNAPSHOT");
    SkiersApi skiersApi = new SkiersApi(apiClient);
    for(int i = 0; i < 10000;i++){
        try {
          LiftRide liftRide = new LiftRide();
          liftRide.setTime(1);
          liftRide.setLiftID(2);
          skiersApi.writeNewLiftRide(liftRide, 3, "2024", "1", 1);
          System.out.println("Completed: " + i + " out of 10000");
        } catch (ApiException e) {
          throw new RuntimeException(e);
        }
    }
    long endTime = System.currentTimeMillis();
    System.out.println("Average response time: " + (endTime - startTime) / 10000.0 + " ms");
    System.out.println("Throughput:" + 10000 / ((endTime - startTime)/ 1000.0) + " /s");



  }

}

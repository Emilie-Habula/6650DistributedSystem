import io.swagger.client.*;
import io.swagger.client.model.*;
import io.swagger.client.api.ResortsApi;

public class ResortsApiExample {

  public static void main(String[] args) {

    ResortsApi apiInstance = new ResortsApi();
    try {
      ResortsList result = apiInstance.getResorts();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResortsApi#getResorts");
      e.printStackTrace();
    }
  }
}
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.Random;

public class SkierApiExample {

  public static void main(String[] args) {
    long startTime = System.currentTimeMillis();
    SkiersApi apiInstance = new SkiersApi();
    Random random = new Random();
    String basePath = "http://34.220.121.130:8080/hw1_war/";
    // String basePath = "http://localhost:8080/HW1_war_exploded/";
    ApiClient client = apiInstance.getApiClient();
    client.setBasePath(basePath);
    Integer resortID = 56; // Integer | ID of the resort of interest
    int success = 0;
    int failed = 0;
    for (int i = 0; i < 10000; i++){
      try {
        LiftRide body = new LiftRide();
        body.liftID(random.nextInt(40) + 1);
        body.time(random.nextInt(90) + 67);
        body.waitTime(random.nextInt(10));
        apiInstance.writeNewLiftRide(body, resortID, "2019", "7", random.nextInt(812));
        success += 1;
      } catch (ApiException e) {
        failed += 1;
        System.err.println("Exception when calling SkiersApi#writeNewLiftRide");
        e.printStackTrace();
      }
    }
    long endTime = System.currentTimeMillis();
    long wallTime = endTime - startTime;
    long throughPut = 1000 * (success + failed) / wallTime;

    System.out.println("10000 single thread Result:");
    System.out.println("-----------------------------------------------");
    System.out.println("Number of successful requests sent: " + success);
    System.out.println("Number of unsuccessful requests: " + failed);
    System.out.println("The total run time(wall time): " + wallTime + " milliseconds");
    System.out.println("The total throughput in requests per second " + throughPut);
  }
}

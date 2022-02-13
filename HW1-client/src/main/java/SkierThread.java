import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class SkierThread extends Thread {
  private static int RETRY_TIMES = 5;

  private Integer resortID;
  private String seasonID;
  private String dayID;
  private Integer startSkierID;
  private Integer endSkierID;
  private Integer startTime;
  private Integer endTime;
  private Integer liftID;
  private int numPostRequests;
  private CountDownLatch latch;
  private CountDownLatch curLatch;
  private Results results;

  public SkierThread(Integer resortID, String seasonID, String dayID,
      Integer startSkierID, Integer endSkierID, Integer startTime, Integer endTime,
      Integer liftID, int numPostRequests, CountDownLatch latch, CountDownLatch curLatch, Results results) {
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.startSkierID = startSkierID;
    this.endSkierID = endSkierID;
    this.startTime = startTime;
    this.endTime = endTime;
    this.liftID = liftID;
    this.numPostRequests = numPostRequests;
    this.latch = latch;
    this.curLatch = curLatch;
    this.results = results;
  }

  @Override
  public void run() {
    // TODO: use input args
    String BASE_PATH = "http://34.220.121.130:8080/hw1_war/";
    // String BASE_PATH = "http://localhost:8080/HW1_war_exploded/";
    SkiersApi apiInstance = new SkiersApi();
    ApiClient client = apiInstance.getApiClient();
    client.setBasePath(BASE_PATH);
    for (int i = 0; i < this.numPostRequests; i++) {
      LiftRide liftRide = new LiftRide();
      Integer skierID = ThreadLocalRandom.current().nextInt(this.endSkierID - this.startSkierID) + this.startSkierID;
      liftRide.liftID(ThreadLocalRandom.current().nextInt(this.liftID) + 1);
      liftRide.time(ThreadLocalRandom.current().nextInt(this.endTime - this.startTime) + this.startTime);
      liftRide.waitTime(ThreadLocalRandom.current().nextInt(10));
      for (int j = 0; j < RETRY_TIMES; j++){
        try {
          apiInstance.writeNewLiftRide(liftRide, this.resortID, this.seasonID, this.dayID, skierID);
          this.results.incrementSuccessfulPost(1);
          break;
        } catch (ApiException e) {
          this.results.incrementFailedPost(1);
          System.err.println("Exception when calling SkierApi#writeNewLiftRide, tried " + j + " times");
          e.printStackTrace();
        }
      }
    }
    try {
      // System.out.println("Thread countdown");
      latch.countDown();
      curLatch.countDown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

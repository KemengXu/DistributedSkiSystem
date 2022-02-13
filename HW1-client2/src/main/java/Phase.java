import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Phase {
  private int numThreads;
  private int numSkiers;
  private Integer resortID;
  private String seasonID;
  private String dayID;
  private Integer numLifts;
  private int startTime;
  private int endTime;
  private int numPostRequests;
  private CountDownLatch latch;
  private CountDownLatch curLatch;
  private Results results;
  private List<Record> part2Records;

  public Phase(int numThreads, int numSkiers, Integer resortID, String seasonID,
      String dayID, Integer numLifts, int startTime, int endTime, int numPostRequests,
      CountDownLatch latch, Results results) {
    this.numThreads = numThreads;
    this.numSkiers = numSkiers;
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.numLifts = numLifts;
    this.startTime = startTime;
    this.endTime = endTime;
    this.numPostRequests = numPostRequests;
    this.latch = latch;
    this.curLatch = new CountDownLatch(numThreads);
    this.results = results;
    this.part2Records = new ArrayList<Record>();
  }

  public void run() throws InterruptedException {
    for (int i = 0; i < this.numThreads; i++) {
      int startSkiers = 1 + (i * (this.numSkiers / this.numThreads));
      int endSkiers = (i + 1) * (this.numSkiers / this.numThreads);
      // System.out.println(startSkiers + "!!!!!!!!!!!!!!!!" + endSkiers);
      Thread thread = new SkierThread(this.resortID, this.seasonID, this.dayID, startSkiers,
          endSkiers, this.startTime, this.endTime, numLifts, numPostRequests, latch, curLatch, this.results,
          this.part2Records);
      thread.start();
    }
  }

  public void await() throws InterruptedException {
    curLatch.await();
  }

  public List<Record> getPart2Records(){
    return part2Records;
  }
}

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class UpicSkiClient {
  private static double PHASE1_FACTOR = 0.2;
  private static double PHASE2_FACTOR = 0.6;
  private static double PHASE3_FACTOR = 0.1;
  private static Integer SINGLE_THROUGHPUT = 53;

  public static void main(String[] args) throws InterruptedException {
    InputArgs input = InputArgs.parseArgs(args);
    Integer numThreads = input.numThreads;
    Integer numSkiers = input.numSkiers;
    Integer numLifts = input.numLifts;
    Integer numRuns = input.numRuns;
    String ip = input.ip;
    String port = input.port;

    Results results = new Results();

    long startTime = System.currentTimeMillis();

    // Phase 1
    int numP1Threads = (int) Math.round(numThreads / 4.0);
    int numP1Requests = (int) Math.round((numRuns * PHASE1_FACTOR) * (numSkiers /  (numP1Threads + 0.0)));
    CountDownLatch latch1 = new CountDownLatch((int)Math.ceil(numP1Threads * 0.2));
    Phase phase1 = new Phase(numP1Threads, numSkiers, 5, "2019", "7", numLifts, 1,
        90, numP1Requests, latch1, results);
    phase1.run();

    // Phase 2
    int numP2Requests = (int) Math.round((numRuns * PHASE2_FACTOR) * (numSkiers / (numThreads + 0.0)));
    CountDownLatch latch2 = new CountDownLatch((int)Math.ceil(numThreads * 0.2));
    Phase phase2 = new Phase(numThreads, numSkiers, 5, "2019", "7", numLifts, 91, 360,
        numP2Requests, latch2, results);
    latch1.await();
    phase2.run();

    // Phase 3
    int numP3Threads = (int) Math.round(numThreads * PHASE3_FACTOR);
    int numP3Requests = (int) Math.round(numRuns * 0.1);
    Phase phase3 = new Phase(numP3Threads, numSkiers, 5, "2019", "7", numLifts, 361,
        420, numP3Requests, latch1, results);
    latch2.await();
    phase3.run();

    // wait for all phase to finish
    phase1.await();
    phase2.await();
    phase3.await();

    long endTime = System.currentTimeMillis();
    long wallTime = endTime - startTime;
    int success = results.getSuccessfulPosts();
    int failed = results.getFailedPosts();
    long throughPut = 1000 * (success + failed) / wallTime;
    long predictedMaxThroughput = (numP1Requests*numP1Threads + numP2Requests*numThreads + numP3Requests*numP3Threads)/
        (numP1Requests+numP2Requests+numP3Requests)* SINGLE_THROUGHPUT;
    int predictedMinThroughput = (int) Math.round(SINGLE_THROUGHPUT * numThreads * PHASE3_FACTOR);

    System.out.println("p1=" + numP1Requests + " p2=" + numP2Requests + " p3=" + numP3Requests);
    System.out.println("max predicted throughput=" + predictedMaxThroughput);
    System.out.println("min predicted throughout=" + predictedMinThroughput);
    System.out.println("predicted throughput=" + (predictedMaxThroughput+predictedMinThroughput)*2/3);
    System.out.println("\nClient Part 1 Result:");
    System.out.println("-----------------------------------------------");
    System.out.println("Number of successful requests sent: " + success);
    System.out.println("Number of unsuccessful requests: " + failed);
    System.out.println("The total run time(wall time): " + wallTime + " milliseconds");
    System.out.println("The total throughput in requests per second " + throughPut);

    // part2
    List<Record> recordList = new ArrayList<>();
    recordList.addAll(phase1.getPart2Records());
    recordList.addAll(phase2.getPart2Records());
    recordList.addAll(phase3.getPart2Records());

    RecordProcessor recordProcessor = new RecordProcessor(recordList, throughPut);
    recordProcessor.process();
  }
}

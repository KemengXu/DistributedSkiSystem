import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class UpicSkiClient {
  private static double PHASE1_FACTOR = 0.2;
  private static double PHASE2_FACTOR = 0.6;
  private static double PHASE3_FACTOR = 0.2;

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
    int numReducedThreads = (int) Math.round(numThreads / 4.0);
    int numPhase1Requests = (int) Math.round((numRuns * PHASE1_FACTOR) * (numSkiers /  numReducedThreads));
    CountDownLatch latch1 = new CountDownLatch((int)Math.ceil(numReducedThreads * 0.2));
    Phase phase1 = new Phase(numReducedThreads, numSkiers, 5, "2019", "7", numLifts, 1,
        90, numPhase1Requests, latch1, results);
    phase1.run();

    // Phase 2
    int numPhase2Requests = (int) Math.round((numRuns * PHASE2_FACTOR) * (numSkiers / numThreads));
    CountDownLatch latch2 = new CountDownLatch((int)Math.ceil(numThreads * 0.2));
    Phase phase2 = new Phase(numThreads, numSkiers, 5, "2019", "7", numLifts, 91, 360,
        numPhase2Requests, latch2, results);
    latch1.await();
    phase2.run();

    // Phase 3
    int numPhase3Requests = (int) Math.round((numRuns * PHASE3_FACTOR) * (numSkiers /  numReducedThreads));
    Phase phase3 = new Phase(numReducedThreads, numSkiers, 5, "2019", "7", numLifts, 360,
        420, numPhase3Requests, latch1, results);
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

    System.out.println("Client Part 1 Result:");
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

    RecordProcessor recordProcessor = new RecordProcessor(recordList);
    recordProcessor.process();
  }
}

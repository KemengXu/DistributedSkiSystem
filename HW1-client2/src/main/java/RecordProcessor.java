import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Queue;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class RecordProcessor {
  private Queue<Record> part2Records;
  private long throughPut;

  public RecordProcessor(Queue<Record> part2Records, long throughPut) {
    this.part2Records = part2Records;
    this.throughPut = throughPut;
  }

  public void process(){
    writeToCSV();
    printPart2Results();
  }

  private void writeToCSV(){
    try {
      FileWriter csvWriter = new FileWriter( "E:\\NEU\\courses\\6650\\records.csv");
      csvWriter.append("startTime,requestType,latency,responseCode\n");
      for (Record record : part2Records) {
        csvWriter.append(record.toCSVFormat());
      }
      csvWriter.flush();
      csvWriter.close();
    }
    catch (IOException e) {
      System.out.println("Write to CSV failed!");
    }
  }

  private void printPart2Results(){
    DescriptiveStatistics stats = new DescriptiveStatistics();
    List<Long> latencies = new ArrayList<>();
    for (Record record: part2Records){
      stats.addValue(record.getLatency());
      latencies.add(record.getLatency());
    }
    System.out.println("\n\nClient Part 2 Result:");
    System.out.println("-----------------------------------------------");
    System.out.println("Mean response time: " + stats.getMean() + " milliseconds");
    System.out.println("Median response time: " + stats.getPercentile(50) + " milliseconds");
    System.out.println("Throughput: " + throughPut + " requests/second");
    System.out.println("P99: " + stats.getPercentile(99) + " milliseconds");
    System.out.println("Min: " + stats.getMin() + " milliseconds; Max: " + stats.getMax() + " milliseconds");
    Collections.sort(latencies);
  }
}

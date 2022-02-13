import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class RecordProcessor {
  private List<Record> recordList;

  public RecordProcessor(List<Record> recordList) {
    this.recordList = recordList;
  }

  public void process(){
    writeToCSV();
    printPart2Results();
  }

  private void writeToCSV(){
    try {
      FileWriter csvWriter = new FileWriter( "E:\\NEU\\courses\\6650\\records.csv");
      csvWriter.append("startTime,requestType,latency,responseCode\n");
      for (Record record : this.recordList) {
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
    for (Record record: recordList){
      stats.addValue(record.getLatency());
      latencies.add(record.getLatency());
    }
    System.out.println("\n\nClient Part 2 Result:");
    System.out.println("----------------------------------------------- total: " + latencies.size());
    System.out.println("Mean response time: " + stats.getMean() + " milliseconds");
    System.out.println("Median response time: " + stats.getPercentile(50) + " milliseconds");
    System.out.println("Throughput: " + 1000 * latencies.size() / stats.getSum() + " requests/second");
    System.out.println("P99: " + stats.getPercentile(99) + " milliseconds");
    System.out.println("Min: " + stats.getMin() + " milliseconds; Max: " + stats.getMax() + " milliseconds");
    Collections.sort(latencies);
  }
}

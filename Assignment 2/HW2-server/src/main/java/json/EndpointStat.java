package json;

public class EndpointStat {
  private String URL;
  private String operation;
  private Integer mean;
  private Integer max;

  public EndpointStat(String URL, String operation, Integer mean, Integer max) {
    this.URL = URL;
    this.operation = operation;
    this.mean = mean;
    this.max = max;
  }

  public String getURL() {
    return URL;
  }

  public String getOperation() {
    return operation;
  }

  public Integer getMean() {
    return mean;
  }

  public Integer getMax() {
    return max;
  }
}

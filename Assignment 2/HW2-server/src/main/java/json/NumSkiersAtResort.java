package json;

public class NumSkiersAtResort {
  private String time;
  private Integer numSkiers;

  public NumSkiersAtResort(String time, Integer numSkiers) {
    this.time = time;
    this.numSkiers = numSkiers;
  }

  public String getTime() {
    return time;
  }

  public Integer getNumSkiers() {
    return numSkiers;
  }
}

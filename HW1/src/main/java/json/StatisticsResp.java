package json;

import java.util.List;

public class StatisticsResp {
  private List<EndpointStat> endpointStats;

  public StatisticsResp(List<EndpointStat> endpointStats) {
    this.endpointStats = endpointStats;
  }

  public List<EndpointStat> getEndpointStats() {
    return endpointStats;
  }
}

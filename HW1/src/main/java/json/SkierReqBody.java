package json;

public class SkierReqBody {
  private Integer time;
  private Integer liftID;
  private Integer waitTime;

  public SkierReqBody(Integer time, Integer liftID, Integer waitTime) {
    this.time = time;
    this.liftID = liftID;
    this.waitTime = waitTime;
  }

  public Integer getTime() {
    return time;
  }

  public Integer getLiftID() {
    return liftID;
  }

  public Integer getWaitTime() {
    return waitTime;
  }
}

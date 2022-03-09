package json;

public class SkierResort {
  private String seasonID;
  private Integer totalVert;

  public SkierResort(String seasonID, Integer totalVert){
    this.seasonID = seasonID;
    this.totalVert = totalVert;
  }

  public Integer getTotalVert() {
    return totalVert;
  }

  public String getSeasonID() {
    return seasonID;
  }
}

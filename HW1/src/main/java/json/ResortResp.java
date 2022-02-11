package json;

import java.util.List;

public class ResortResp {
  private List<Resort> resorts;

  public ResortResp(List<Resort> resorts) {
    this.resorts = resorts;
  }

  public List<Resort> getResorts() {
    return resorts;
  }
}

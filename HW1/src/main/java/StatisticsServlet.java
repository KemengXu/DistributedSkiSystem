import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import json.EndpointStat;
import json.Message;
import json.Resort;
import json.ResortResp;
import json.StatisticsResp;

@WebServlet(name = "StatisticsServlet")
public class StatisticsServlet extends HttpServlet {
  private Gson gson  = new Gson();

  protected void doGet(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_OK);
      List<EndpointStat> endpointStats = new ArrayList<EndpointStat>();
      endpointStats.add(new EndpointStat("/resorts", "GET", 11, 198));
      StatisticsResp statisticsResp = new StatisticsResp(endpointStats);
      res.getWriter().write(gson.toJson(statisticsResp));
      return;
    }

    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    Message msg = new Message("string");
    res.getWriter().write(gson.toJson(msg));
  }
}

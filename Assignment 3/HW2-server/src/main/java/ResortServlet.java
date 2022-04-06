import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import json.Message;
import json.NumSkiersAtResort;
import json.Resort;
import json.ResortReqBody;
import json.ResortResp;
import json.Seasons;
import json.SkierReqBody;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "ResortServlet")
public class ResortServlet extends HttpServlet {
  private static final int DAY_MIN = 1;
  private static final int DAY_MAX = 366;

  private final static String QUEUE_NAME = "ResortServletPostQueue";

  private Gson gson  = new Gson();

  private ObjectPool<Channel> pool;

  public void init() {
    this.pool = new GenericObjectPool<Channel>(new ConnectionPoolFactory());
  }

  protected void doPost(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts) || urlParts.length != 3) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      Message msg = new Message("string");
      res.getWriter().write(gson.toJson(msg));
    } else {
      try {
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = req.getReader().readLine()) != null) {
          sb.append(s);
        }

        ResortReqBody resortReqBody = gson.fromJson(sb.toString(), ResortReqBody.class);
        assert(isBodyValid(resortReqBody));

        JsonObject resortInfo = new JsonObject();
        resortInfo.addProperty("resortID", Integer.valueOf(urlParts[1]));
        resortInfo.addProperty("year", resortReqBody.getYear());

        Channel channel = null;
        try {
          channel = pool.borrowObject();
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          channel.basicPublish("", QUEUE_NAME, null, resortInfo.toString().getBytes());
//          System.out.println(" [x] Sent '" + resortInfo.toString() + "'");
        } catch (Exception e) {
          throw new RuntimeException("Unable to borrow buffer from pool" + e.toString());
        } finally {
          try {
            if (null != channel) {
              pool.returnObject(channel);
            }
          } catch (Exception e) {
            System.out.println("Error when returning channel");
          }
        }

        res.setStatus(HttpServletResponse.SC_CREATED);
      } catch (Exception ex) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_OK);
      List<Resort> resorts = new ArrayList<Resort>();
      resorts.add(new Resort("string", 0));
      ResortResp resortResp = new ResortResp(resorts);
      res.getWriter().write(gson.toJson(resortResp));
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      Message msg = new Message("string");
      res.getWriter().write(gson.toJson(msg));
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      if (urlParts.length == 3){
        List<String> seasonArray = new ArrayList<String>();
        seasonArray.add("string");
        Seasons seasons = new Seasons(seasonArray);
        res.getWriter().write(gson.toJson(seasons));
      } else {
        NumSkiersAtResort numSkiersAtResort = new NumSkiersAtResort("Mission Ridge", 78999);
        res.getWriter().write(gson.toJson(numSkiersAtResort));
      }
    }
  }

  private boolean isBodyValid(ResortReqBody resortReqBody) {
    if(resortReqBody.getYear() != null){
      return true;
    }
    return false;
  }

  private boolean isUrlValid(String[] urlPath) {
    if(urlPath.length == 7) {
      return isNumeric(urlPath[1]) &&
          urlPath[2].equals("seasons") &&
          isNumeric(urlPath[3]) &&
          urlPath[3].length() == 4 &&
          urlPath[4].equals("day") &&
          isNumeric(urlPath[5]) &&
          Integer.parseInt(urlPath[5]) >= DAY_MIN &&
          Integer.parseInt(urlPath[5]) <= DAY_MAX &&
          urlPath[6].equals("skiers");
    } else if(urlPath.length == 3){
      return isNumeric(urlPath[1]) &&
          urlPath[2].equals("seasons");
    } else if(urlPath.length == 1){
      return true;
    }
    return false;
  }

  private boolean isNumeric(String s) {
    if(s == null || s.equals("")) return false;
    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException ignored) { }
    return false;
  }
}

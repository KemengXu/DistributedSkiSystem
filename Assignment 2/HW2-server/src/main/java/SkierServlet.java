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
import json.SkierResort;
import json.SkierReqBody;
import json.Vertical;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {
  private static final int DAY_MIN = 1;
  private static final int DAY_MAX = 366;
  private final static String QUEUE_NAME = "SkierServletPostQueue";

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

    if (!isUrlValid(urlParts) || urlParts[2].equals("vertical")) {
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

        SkierReqBody skierReqBody = gson.fromJson(sb.toString(), SkierReqBody.class);
        assert(isBodyValid(skierReqBody));

        JsonObject liftInfo = new JsonObject();
        liftInfo.addProperty("resortID", Integer.valueOf(urlParts[1]));
        liftInfo.addProperty("seasonID", Integer.valueOf(urlParts[3]));
        liftInfo.addProperty("dayID", Integer.valueOf(urlParts[5]));
        liftInfo.addProperty("skierId", Integer.valueOf(urlParts[7]));
        liftInfo.addProperty("time", skierReqBody.getTime());
        liftInfo.addProperty("liftId", skierReqBody.getLiftID());
        liftInfo.addProperty("waitTime", skierReqBody.getWaitTime());

        Channel channel = null;
        try {
          channel = pool.borrowObject();
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          channel.basicPublish("", QUEUE_NAME, null, liftInfo.toString().getBytes());
//          System.out.println(" [x] Sent '" + liftInfo.toString() + "'");
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

//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("52.32.221.65");
//        factory.setPort(5672);
//        factory.setUsername("xkmmmm");
//        factory.setPassword("asdf123");
//        try (Connection connection = factory.newConnection();
//            Channel channel = connection.createChannel()) {
//          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//          String message = "Hello World!";
//          channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
//          System.out.println(" [x] Sent '" + message + "'");
//        }catch (Exception e){
//          System.out.println(e + "!!!!!!!!!!!!!!!!");
//        }


        res.setStatus(HttpServletResponse.SC_CREATED);
      } catch (Exception ex) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    }
  }

  private boolean isBodyValid(SkierReqBody skierReqBody) {
    if(skierReqBody.getLiftID() != null &&
        skierReqBody.getTime() != null &&
        skierReqBody.getWaitTime() != null){
      return true;
    }
    return false;
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
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

    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      Message msg = new Message("string");
      res.getWriter().write(gson.toJson(msg));
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      if (urlParts[2].equals("vertical")){
        List<SkierResort> dummySkierResorts = new ArrayList<SkierResort>();
        dummySkierResorts.add(new SkierResort("string", 0));
        Vertical vert = new Vertical(dummySkierResorts);
        res.getWriter().write(gson.toJson(vert));
      } else {
        res.getWriter().write("34507");
      }
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    if(urlPath.length == 8) {
      return isNumeric(urlPath[1]) &&
          urlPath[2].equals("seasons") &&
          isNumeric(urlPath[3]) &&
          urlPath[3].length() == 4 &&
          urlPath[4].equals("days") &&
          isNumeric(urlPath[5]) &&
          Integer.parseInt(urlPath[5]) >= DAY_MIN &&
          Integer.parseInt(urlPath[5]) <= DAY_MAX &&
          urlPath[6].equals("skiers") &&
          isNumeric(urlPath[7]);
    } else if(urlPath.length == 3){
      return isNumeric(urlPath[1]) &&
          urlPath[2].equals("vertical");
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

import com.google.gson.Gson;
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

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {
  private static final int DAY_MIN = 1;
  private static final int DAY_MAX = 366;

  private Gson gson  = new Gson();

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

    if (!isUrlValid(urlParts) || urlParts[2].equals("vertical")) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      Message msg = new Message("string");
      res.getWriter().write(gson.toJson(msg));
    } else {
      try {
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = req.getReader().readLine()) != null) {
          // TODO: check request body
          sb.append(s);
        }

        SkierReqBody skierReqBody = gson.fromJson(sb.toString(), SkierReqBody.class);
        //TODO: process skierReqBody
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

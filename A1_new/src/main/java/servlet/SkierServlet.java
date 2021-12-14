package servlet;

import com.google.gson.Gson;
import database.ResortDBReader;
import database.SkierDBReader;
import info.LiftRide;
import java.io.BufferedReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import sender.ResortMessageSender;
import sender.SkierMessageSender;
import util.URLVerifier;

@WebServlet(name = "servlet.SkierServlet", value = "/servlet.SkierServlet")
public class SkierServlet extends HttpServlet {
  private static final String DELIM = "/";
  private static final Gson gson = new Gson();
  protected static final String RESORT_TYPE = "resorts";
  protected static final String SKIER_TYPE = "skiers";
  private static final Map<Integer, Integer> resortDayToNumSkiers = new ConcurrentHashMap<>();
  private static final Map<Integer, Integer> skierToVertical = new ConcurrentHashMap<>();
  private static final Map<Integer, Map<Integer, Integer>> dayToUniqueNumSkiers = new ConcurrentHashMap<>();
  private static final SkierDBReader skierDBReader = new SkierDBReader(skierToVertical,
                                                                        dayToUniqueNumSkiers);
  private static final ResortDBReader resortDBReader = new ResortDBReader(resortDayToNumSkiers);

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html"); // output text

    String urlPath = request.getRequestURI();
    System.out.println(urlPath);

    // check if we have a URL
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split(DELIM);

    // not a valid url
    if (!URLVerifier.isValidURL(urlParts)) {
      response.getWriter().write("Invalid URL");
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    String type = URLVerifier.getType(urlParts);
    // process requests starting with resorts
    if (type.equals(RESORT_TYPE)) {
      int day = URLVerifier.getDayId(urlParts);
      int season = URLVerifier.getSeasonId(urlParts);
      int resortId = URLVerifier.getResortId(urlParts);

      // GET request 1 - resort
      try {
        int res = resortDBReader.getSkierOfDay(resortId, season, day);
        response.getWriter().write("resort id: " + resortId +
            "\nseason id: " + season +
            "\nday id: " + day +
            "\nnumber of skiers: " + res);
        response.setStatus(HttpServletResponse.SC_OK);
      } catch (SQLException throwables) {
        throwables.printStackTrace();
        response.getWriter().write("Doesn't work");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    } else if (type.equals(SKIER_TYPE)) {
      // process requests starting with skiers

      // GET request 3
      if (urlParts.length == 5) {
        try {
          int vertical = skierDBReader.getVertical(Integer.parseInt(urlParts[3]));
          response.getWriter().write("Vertical: " + vertical);
          response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException throwables) {
          throwables.printStackTrace();
          response.getWriter().write("Doesn't work");
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
      } else if (urlParts.length == 10) {

        // GET request 2
        int resortId = URLVerifier.getResortId(urlParts);
        int season = URLVerifier.getSeasonId(urlParts);
        int day = URLVerifier.getDayId(urlParts);
        int skierId = URLVerifier.getSkierId(urlParts);
        try {
          int numLiftRides = skierDBReader.getNumLiftRides(resortId, season, day, skierId);
          response.getWriter().write(
              "resort id: " + resortId +
              "\nseason id: " + season +
              "\nday id" + day +
              "\nnumber of lift rides " + numLiftRides);
          response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException throwables) {
          throwables.printStackTrace();
          response.getWriter().write("Doesn't work");
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
      } else {
        response.getWriter().write("Invalid request");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json"); // output text

    Gson gson = new Gson();
    String urlPath = request.getRequestURI();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // check and send response
    String[] urlParts = urlPath.split(DELIM);

    // get request body, user input in "body" by default
    BufferedReader inputReader = request.getReader();
    LiftRide liftRide = toLiftRide(inputReader); // convert to string

    if (URLVerifier.isValidURL(urlParts)) {
      // put message to rabbitMQ by calling sender
      String type = URLVerifier.getType(urlParts);

      // send skier messages
      if (type.equals(SKIER_TYPE)) {
        try {
          SkierMessageSender.sendAMessage(liftRide);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // send resort messages
      if (type.equals(RESORT_TYPE)) {
        try {
          ResortMessageSender.sendAMessage(liftRide);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      response.getWriter().write(gson.toJson(liftRide));
      response.setStatus(HttpServletResponse.SC_OK);

    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  /**
   * Make a LiftRide class object based on request body
   */
  public LiftRide toLiftRide(BufferedReader bufferIn) throws IOException {
    StringBuilder builder = new StringBuilder();

    String line;
    while((line = bufferIn.readLine()) != null) {
      builder.append(line);
    }

    return gson.fromJson(builder.toString(), LiftRide.class);
  }
}
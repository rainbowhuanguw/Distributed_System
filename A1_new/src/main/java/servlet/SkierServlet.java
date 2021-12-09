package servlet;

import com.google.gson.Gson;
import info.LiftRide;
import java.io.BufferedReader;
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

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html"); // output text

    String urlPath = request.getPathInfo();

    // check if we have a URL
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split(DELIM);


    if (URLVerifier.isValidURL(urlParts)) {
      response.getWriter().write("<h1>" + "It works!" + "</h1>\n");
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
      if (type.equals(SKIER_TYPE)) {
        try {
          SkierMessageSender.sendAMessage(liftRide);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else if (type.equals(RESORT_TYPE)) {
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
package client1;

import com.google.gson.Gson;
import java.io.BufferedReader;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "client1.SkierServlet", value = "/client1.SkierServlet")
public class SkierServlet extends HttpServlet {
  private static final String DELIM = "/";
  private static final Gson gson = new Gson();

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/html"); // output text

    String urlPath = request.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    // check and send response
    String[] urlParts = urlPath.split(DELIM);

    if (URLVerifier.isValidSkierUrl(urlParts)) {
      response.getWriter().write("<h1>" + "It works!" + "</h1>\n");
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json"); // output text

    Gson gson = new Gson();
    String urlPath = request.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // make response
    // get request body, user input in "body" by default
    BufferedReader inputReader = request.getReader();
    Response out = toResponse(inputReader); // convert to string

    // check and send response
    String[] urlParts = urlPath.split(DELIM);

    if (URLVerifier.isValidSkierUrl(urlParts)) {
      response.getWriter().write(gson.toJson(out));
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  /**
   * Make client1.Response class object based on request body
   * @param bufferIn request input
   * @return client1.Response object
   * @throws IOException
   */
  public Response toResponse(BufferedReader bufferIn) throws IOException {
    StringBuilder builder = new StringBuilder();

    String line;
    while((line = bufferIn.readLine()) != null) {
      builder.append(line);
    }
    return gson.fromJson(builder.toString(), Response.class);
  }
}

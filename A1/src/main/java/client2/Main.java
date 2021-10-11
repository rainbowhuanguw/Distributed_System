package client2;

import client1.Response;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Main {

  public static void main(String[] args) {
    String PREFIX = "http://54.82.252.108:8080/A1_war/skiers/"
        + "resort1/seasons/season1/days/day1/skiers/";

    HttpClient httpClient = HttpClient.newBuilder()
        .version(Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(20))
        .build();

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < 500; i++) {
      Response res = new Response(11, 11);

      HttpRequest request = HttpRequest.newBuilder()
          // turn time and lift id as request body, then convert into json
          .POST(HttpRequest.BodyPublishers.ofString(res.toString()))
          .uri(URI.create(PREFIX + 123)) // put skier id into url
          .setHeader("Client", "skier:" + 123) // add request header
          .build();

      HttpResponse<String> response = null;
      try {
        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }

    long endTime = System.currentTimeMillis();

    System.out.println((endTime - startTime) / 500.0);
  }
}

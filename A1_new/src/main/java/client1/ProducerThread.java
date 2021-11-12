package client1;

import info.LiftRide;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import util.Counter;
import util.RandomNumGenerator;
import util.RangeGenerator;

/**
 * Custom thread class
 */
public class ProducerThread extends Thread {

  private static final String PREFIX = "http://"
      + "3.89.20.1:8080/A2_war" +            // for ec2
      // + "localhost:8080/A2_war_exploded" +   // for local
      "/skiers/resort1/seasons/season1/days/day1/skiers/";

  private static final int SUCCESS = 200;
  private final int skierIdStart;
  private final int skierIdEnd;
  private final int numPosts;
  private final int startTime;
  private final int endTime;
  private final int numLifts;

  private final HttpClient client;
  private final Counter failureCounter;

  public ProducerThread(int threadId, int numLifts, int numThreads, int numSkiers,
      int numPosts, HttpClient client, Counter failureCounter) {
    // calculate skier id range
    int[] idRange = RangeGenerator.getSkierIdRange(numSkiers/numThreads, threadId);
    this.skierIdStart = idRange[0];
    this.skierIdEnd = idRange[1];

    int[] timeRange = RangeGenerator.getTimeRange(numThreads, threadId);
    this.startTime = timeRange[0];
    this.endTime = timeRange[1];

    this.numPosts = numPosts;
    this.numLifts = numLifts;
    this.client = client;
    this.failureCounter = failureCounter;
  }

  @Override
  public void run() {
    for (int i = 0; i < numPosts; i++) {
      // generate random id, time value, and lift id
      int skierId = RandomNumGenerator.generateNum(skierIdStart, skierIdEnd);
      int timeValue = RandomNumGenerator.generateNum(startTime, endTime);
      int liftId = RandomNumGenerator.generateNum(1, numLifts);

      // send one post request
      try {
        sendPostRequest(skierId, timeValue, liftId);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Sends out one post request to the server
   */
  public void sendPostRequest(int skierId, int timeValue, int liftId) throws Exception {
    // create a response object
    LiftRide liftRide = new LiftRide(skierId, liftId, timeValue);

    // send http request
    HttpRequest request = HttpRequest.newBuilder()
        // turn time and lift id as request body, then convert into json
        .POST(HttpRequest.BodyPublishers.ofString(liftRide.toJsonString()))
        .uri(URI.create(PREFIX + skierId)) // put skier id into url
        .setHeader("Client", "skier:" + skierId) // add request header
        .build();

    HttpResponse<String> response = null;
    try {
      response = client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      //this.failureCounter.increment();
      e.printStackTrace();
    }

    // print response headers
    assert response != null;

    if (response.statusCode() != SUCCESS) this.failureCounter.increment();
  }
}

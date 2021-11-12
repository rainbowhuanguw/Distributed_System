package client2;

import info.InfoPackage;
import util.Counter;
import util.RandomNumGenerator;
import util.RangeGenerator;
import info.LiftRide;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Custom thread class
 */
public class ProducerThread extends Thread {
  private static final String PREFIX = "http://"
      + "3.89.20.1:8080/A2_war" +            // for ec2
      // + "localhost:8080/A2_war_exploded" +   // for local
      "/skiers/resort1/seasons/season1/days/day1/skiers/";

  private static final int SUCCESS = 200;
  private static final String POST = "POST";
  private static final int MIN_LIFT = 1;
  private static final int TRY_TIME = 5;

  private final int skierIdStart;
  private final int skierIdEnd;
  private final int numPosts;
  private final int startTime;
  private final int endTime;
  private final int numLifts;

  private final HttpClient client;
  private final Counter failureCounter;

  private final ConcurrentLinkedQueue<InfoPackage> infoQueue;

  public ProducerThread(int threadId,  int numLifts, int numThreads, int numSkiers,
      int numPosts, HttpClient client, Counter failureCounter,
      ConcurrentLinkedQueue<InfoPackage> infoQueue) {
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

    this.infoQueue = infoQueue;
  }

  @Override
  public void run() {
    for (int i = 0; i < numPosts; i++) {
      // generate random id, time value, and lift id
      int skierId = RandomNumGenerator.generateNum(skierIdStart, skierIdEnd);
      int timeValue = RandomNumGenerator.generateNum(startTime, endTime);
      int liftId = RandomNumGenerator.generateNum(MIN_LIFT, numLifts);

      // send one post request
      sendPostRequest(skierId, timeValue, liftId);
    }
  }
  
  /**
   * Sends out one post request to the server
   */
  public void sendPostRequest(int skierId, int timeValue, int liftId) {
    // create a response object, store in the queue
    LiftRide res = new LiftRide(skierId, liftId, timeValue);

    HttpRequest request = HttpRequest.newBuilder()
        // turn time and lift id as request body, then convert into json
        .POST(HttpRequest.BodyPublishers.ofString(res.toJsonString()))
        .uri(URI.create(PREFIX + skierId)) // put skier id into url
        .setHeader("Client", "skier:" + skierId) // add request header
        .version(Version.HTTP_1_1)
        .build();

    long startTime = System.currentTimeMillis();

    HttpResponse<String> response = null;

    int count = 0;
    String code;

    while (count < TRY_TIME) {
      try {
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        code = response.statusCode() + "";
        if (code.startsWith("5") || code.startsWith("4")) count++;
        else break;
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }

    long endTime = System.currentTimeMillis();

    // print response headers
    if (response.statusCode() != SUCCESS) this.failureCounter.increment();

    // add to queue
    infoQueue.add(new InfoPackage(startTime, endTime, POST, response.statusCode()));
  }
}

package client2;

import info.InfoPackage;
import java.util.concurrent.CountDownLatch;
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
  //private static final String HOST = "http://52.90.110.2:8080/A3_war"; // for ec2
  private static final String HOST = "http://localhost:8080/A3_war_exploded"; // for local
  private static final String SEASON = "/seasons/";
  private static final String DAY = "/days/";
  public static final String SKIER = "/skiers/";
  public static final String RESORT = "/resorts/";

  private static final int SUCCESS = 200;
  private static final String POST = "POST";
  private static final int MIN_LIFT = 1;
  private static final int TRY_TIME = 5;
  private static final int MAX_RESORT_ID = 10; // assuming only ten resorts available

  private final int skierIdStart;
  private final int skierIdEnd;
  private final int numPosts;
  private final int startTime;
  private final int endTime;
  private final int numLifts;

  private final HttpClient client;
  private final Counter failureCounter;
  private final Counter allCounter;
  private final CountDownLatch roundCounter;
  private final CountDownLatch latch;

  private final ConcurrentLinkedQueue<InfoPackage> infoQueue;

  public ProducerThread(int threadId,  int numLifts, int numThreads, int numSkiers,
      int numPosts, HttpClient client, Counter failureCounter, Counter allCounter,
      CountDownLatch roundCounter, CountDownLatch latch, ConcurrentLinkedQueue<InfoPackage> infoQueue) {
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
    this.allCounter = allCounter;
    this.roundCounter = roundCounter;
    this.latch = latch;
    this.infoQueue = infoQueue;
  }

  @Override
  public void run() {
    for (int i = 0; i < numPosts; i++) {
      // generate random id, time value, and lift id
      int skierId = RandomNumGenerator.generateNum(skierIdStart, skierIdEnd);
      int resortId = RandomNumGenerator.generateNum(1, MAX_RESORT_ID);
      int timeValue = RandomNumGenerator.generateNum(startTime, endTime);
      int liftId = RandomNumGenerator.generateNum(MIN_LIFT, numLifts);
      int seasonId = RandomNumGenerator.generateYear();
      int dayId = RandomNumGenerator.generateDay();

      // send one skier post request
      //sendPostRequest(SKIER, resortId, skierId, timeValue, liftId, seasonId, dayId);

      // send one resort post request
      sendPostRequest(RESORT, resortId, skierId, timeValue, liftId, seasonId, dayId);
    }

    roundCounter.countDown();

    latch.countDown();
  }

  private static String generateURL(String type, int resortId, int skierId, int seasonId, int dayId) {
    String suffix = resortId + SEASON + seasonId + DAY + dayId + SKIER + skierId;

    if (type.equals(RESORT) || type.equals(SKIER)) {
      return HOST + type + suffix;
    }

    return "";
  }
  
  /**
   * Sends out one post request to the server
   */
  public void sendPostRequest(String type, int resortId, int skierId, int timeValue, int liftId,
      int seasonId, int dayId) {
    // create a response object
    LiftRide liftRide = new LiftRide(skierId, liftId, timeValue, seasonId, dayId, resortId);
    // create a url
    String url = generateURL(type, resortId, skierId, seasonId, dayId);

    //System.out.println(url);
    // skip empty urls
    if (url.isEmpty()) return;

    // send requests, process by servlet internally
    HttpRequest request = HttpRequest.newBuilder()
        // turn time and lift id as request body, then convert into json
        .POST(HttpRequest.BodyPublishers.ofString(liftRide.toJsonString()))
        // put skier id into url
        .uri(URI.create(url))
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

    allCounter.increment();
  }
}

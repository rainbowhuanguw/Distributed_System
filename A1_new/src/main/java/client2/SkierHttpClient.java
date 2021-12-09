package client2;

import info.InfoPackage;
import util.Counter;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Client with information about each request's response time, including median, average, and
 * 99th percent. Information is saved in a concurrentHashQueue.
 */
public class SkierHttpClient {
  private static final int MIN = 0;
  private static final int MIN_THREAD = 1;
  private static final int MAX_THREAD = 512;
  private static final int MAX_SKIER = 100000;
  private static final int DEFAULT_LIFT = 40;
  private static final int MIN_LIFT = 5;
  private static final int MAX_LIFT = 60;
  private static final int DEFAULT_RUN = 10;
  private static final int MAX_RUN = 20;
  private static final int DIVISOR = 4;
  private static final double STARTUP_FACTOR = 0.2;
  private static final double PEAK_FACTOR = 0.6;
  private static final double COOLDOWN_FACTOR = 0.1;
  private static final double RUN_FACTOR = 0.1; // factor to start another phase
  private static final String SKIP_COMMAND = "NULL";
  private static final int TIME_OUT_LIMIT = 200000;

  private static final HttpClient httpClient = HttpClient.newBuilder()
      .version(Version.HTTP_1_1)
      .connectTimeout(Duration.ofSeconds(TIME_OUT_LIMIT))
      .build();

  private static final Scanner scanner = new Scanner(System.in);

  /**
   * Validates if a given num is within range [min, max]
   */
  private static int validate(int num, int min, int max) {
    while (num < min || num > max) {
      System.out.println("Invalid, specify a number within range : [" + min +
          "," + max + "]");
      num = scanner.nextInt();
    }
    return num;
  }

  /**
   * Runs a phase, e.g. peak, cooldown etc.
   */
  private static void runPhase(double factor, int numThreads, int numRuns, int numSkiers,
      int numLifts, Counter failureCounter, Counter allCounter, CountDownLatch roundCounter,
      CountDownLatch latch, ConcurrentLinkedQueue<InfoPackage> infoQueue) {

    // calculate number of posts
    int numPosts;
    if (factor == STARTUP_FACTOR || factor == PEAK_FACTOR) {
      numPosts = (int) (factor * numRuns * numSkiers / numThreads);
    } else {
      numPosts = (int) factor * numRuns;
    }

    // create numThreads threads
    for (int i = 0; i < numThreads; i++) {
      // create and start threads
      Thread thread = new ProducerThread(i, numLifts, numThreads, numSkiers, numPosts,
          httpClient, failureCounter, allCounter, roundCounter, latch, infoQueue);
      thread.start(); // start
    }
  }

  public static void main(String[] args) throws Exception {
    // accept number of threads
    System.out.print("Enter number of threads to run: ");
    int numThreads = validate(scanner.nextInt(), MIN_THREAD, MAX_THREAD);

    // accept number of skiers
    System.out.print("Enter number of skiers: ");
    int numSkiers = validate(scanner.nextInt(), MIN, MAX_SKIER);

    // accept number of ski lifts
    System.out.print("Enter number of ski lifts, enter NULL to use default value (" +
        DEFAULT_LIFT + "): ");
    String liftString = scanner.next();
    int numLifts = liftString.equalsIgnoreCase(SKIP_COMMAND) ? DEFAULT_LIFT :
        validate(Integer.parseInt(liftString), MIN_LIFT, MAX_LIFT);

    // accept mean number of ski lifts
    System.out.print("Enter number of ski lifts each skier rides each day,"
        + " enter NULL to use default value (" + DEFAULT_RUN + "): ");
    String runString = scanner.next();
    int numRuns = runString.equalsIgnoreCase(SKIP_COMMAND) ? DEFAULT_RUN :
        validate(Integer.parseInt(runString), MIN, MAX_RUN);

    System.out.println(numThreads + " " + numSkiers + " " + numLifts + " " + numRuns);

    // counter for failed responses
    Counter failureCounter = new Counter(), allCounter = new Counter();

    // count down latch for counting threads
    CountDownLatch latch = new CountDownLatch((numThreads / DIVISOR) * 2 + numThreads);

    // stores info pack
    ConcurrentLinkedQueue<InfoPackage> infoQueue = new ConcurrentLinkedQueue<>();

    // mark start time
    long start = System.currentTimeMillis();


    CountDownLatch roundCounter1 = new CountDownLatch((int) Math.ceil(numThreads * RUN_FACTOR / DIVISOR)),
        roundCounter2 = new CountDownLatch((int) Math.ceil(numThreads * RUN_FACTOR)),
        roundCounter3 = new CountDownLatch((int) Math.ceil(numThreads * RUN_FACTOR / DIVISOR));

    // start phase 1
    System.out.println("starting phase 1");
    runPhase(STARTUP_FACTOR, numThreads / DIVISOR, numRuns, numSkiers, numLifts,
        failureCounter, allCounter, roundCounter1, latch, infoQueue);

    // start phase 2
    roundCounter1.await();
    System.out.println("starting phase 2");
    runPhase(PEAK_FACTOR, numThreads, numRuns, numSkiers, numLifts, failureCounter, allCounter,
        roundCounter2, latch, infoQueue);

    // start phase 3
    roundCounter2.await();
    System.out.println("starting phase 3");
    runPhase(COOLDOWN_FACTOR, numThreads / DIVISOR, numRuns, numSkiers, numLifts,
        failureCounter, allCounter, roundCounter3, latch, infoQueue);

    // counter to wait for all three phases to finish
    latch.await();

    long end = System.currentTimeMillis();

    int totalRequests = allCounter.getCount();

    long sumLatency = 0; // calculate average latency
    List<Long> latencies = new ArrayList<>(); // calculate median
    long maxLatency = 0, currLatency;

    for (InfoPackage infoPackage : infoQueue){
      currLatency = infoPackage.getLatency();
      sumLatency += currLatency;
      latencies.add(currLatency);
      maxLatency = Math.max(maxLatency, currLatency);
    }

    long averageLatency = sumLatency / totalRequests;

    Collections.sort(latencies);
    long medianLatency = latencies.get(latencies.size() / 2);
    long p99Latency = latencies.get((int) Math.round(latencies.size() * 0.99));

    System.out.println("**************************************");
    System.out.println("number of requests: " + totalRequests);
    System.out.println("number of failures: " + failureCounter.getCount());
    System.out.println("total response time (ms): " + (end - start));
    System.out.println("throughput (request/s): " + (totalRequests * 1000.0) / (end - start));
    System.out.println("average response time (ms): " + averageLatency);
    System.out.println("median response time (ms): " + medianLatency);
    System.out.println("max response time (ms): " + maxLatency);
    System.out.println("99 percentile response time (ms): " + p99Latency);
  }
}



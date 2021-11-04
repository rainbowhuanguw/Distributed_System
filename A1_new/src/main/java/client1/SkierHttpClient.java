package client1;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Client with information about number of success and failure, throughput, etc.
 * Saves liftRide information to RabbitMQ
 */
public class SkierHttpClient {
  private static final int MIN = 0;
  private static final int MIN_THREAD = 1;
  private static final int MAX_THREAD = 256;
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
      int numLifts, List<Thread> allThreads, Counter failureCounter)
      throws InterruptedException {

    // keep track of count of threads created
    final Counter finishedThreads = new Counter();
    CountDownLatch createdThreads = new CountDownLatch(numThreads);

    // array to store threads
    Thread[] tids = new Thread[numThreads];

    // calculate number of posts
    int numPosts;
    if (factor == STARTUP_FACTOR || factor == PEAK_FACTOR)
      numPosts = (int) (factor * numRuns * numSkiers / numThreads);
    else numPosts = (int) factor * numRuns;

    // create numThreads threads
    for (int i = 0; i < numThreads; i++) {
      // create and start threads
      Thread thread = new RequestThread(i, numLifts, numThreads, numSkiers, numPosts,
          httpClient, failureCounter);
      tids[i] = thread; // store in the array to check for liveliness within this phase
      allThreads.add(thread); // store in the list to shared across three phases

      createdThreads.countDown(); // count
      thread.start(); // start
    }

    // count number of alive threads
    while (finishedThreads.getVal() < Math.ceil(RUN_FACTOR * numThreads)) {
      for (Thread curr : tids) {
        if (!curr.isAlive()) finishedThreads.increment();
        if (finishedThreads.getVal() >= Math.ceil(RUN_FACTOR * numThreads)) break;
      }
    }

    // run next phase when factor * numThreads is reached
    if (factor == STARTUP_FACTOR) { // run phase 2
      runPhase(PEAK_FACTOR, numThreads * DIVISOR, numRuns, numSkiers, numLifts,
          allThreads, failureCounter);
    } else if (factor == PEAK_FACTOR) { // run phase 3
      runPhase(COOLDOWN_FACTOR, numThreads / DIVISOR, numRuns, numSkiers, numLifts,
          allThreads, failureCounter);
    }

    createdThreads.await(); // wait till all threads within this phase are created
  }

  public static void main(String[] args) throws Exception {
    // accept number of threads
    System.out.print("Please enter number of threads to run:");
    int numThreads = validate(scanner.nextInt(), MIN_THREAD, MAX_THREAD);

    // accept number of skiers
    System.out.print("Please enter number of skiers:");
    int numSkiers = validate(scanner.nextInt(), MIN, MAX_SKIER);

    // accept number of ski lifts
    System.out.print("Please enter number of ski lifts, enter NULL to use default value (" +
        DEFAULT_LIFT + "): ");
    String liftString = scanner.next();
    int numLifts = liftString.equalsIgnoreCase(SKIP_COMMAND) ? DEFAULT_LIFT :
        validate(Integer.parseInt(liftString), MIN_LIFT, MAX_LIFT);

    // accept mean number of ski lifts
    System.out.print("Please enter number of ski lifts each skier rides each day,"
        + " enter NULL to use default value (" + DEFAULT_RUN + "): ");
    String runString = scanner.next();
    int numRuns = runString.equalsIgnoreCase(SKIP_COMMAND) ? DEFAULT_RUN :
        validate(Integer.parseInt(runString), MIN, MAX_RUN);

    System.out.println(numThreads + " " + numSkiers + " " + numLifts + " " + numRuns);

    // log start time
    List<Thread> allThreads = new ArrayList<>();
    Counter failureCounter = new Counter();

    long startTime = System.currentTimeMillis();

    // start by doing phase 1
    runPhase(STARTUP_FACTOR, numThreads / DIVISOR, numRuns, numSkiers, numLifts, allThreads, failureCounter);

    // counter to wait for all three phases to finish
    for (Thread thread : allThreads) thread.join();

    // log end time
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    int totalRequests = (int)(numSkiers * numRuns * STARTUP_FACTOR) +
        (int)(numSkiers * numRuns * PEAK_FACTOR) +
        (int)(numRuns * COOLDOWN_FACTOR * numThreads / DIVISOR);

    System.out.println("**************************************");
    System.out.println("number of requests: " + totalRequests);
    System.out.println("number of failures: " + failureCounter.getVal());
    System.out.println("total number of threads created in 3 phases: " + allThreads.size());
    System.out.println("duration (ms): " + duration);
    System.out.println("throughput(requests/s): " + (totalRequests * 1000.0 / duration));

  }


}



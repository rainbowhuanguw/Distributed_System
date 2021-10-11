package client1;

/**
 * Generates a number range given the batch size and thread id
 */
public class RangeGenerator {

  private static final int BASE = 1;
  private static final int DIVISOR = 4;

  // [starting id, ending id]
  public static int[] getSkierIdRange(int batchSize, int threadId) {
    int prev = batchSize * Math.max(threadId - 1, 0);
    return new int[] {prev + BASE, prev + BASE + batchSize};
  }

  public static int[] getTimeRange(int numThreads, int threadId) {
    int prev = numThreads * DIVISOR * Math.max(threadId - 1, 0);
    return new int[] {prev + BASE, prev + BASE + numThreads * DIVISOR};
  }
}

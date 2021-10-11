package client1;

public class CommandVerifier {
  private static final int ARG_NUM = 5;
  private static final int MIN_THREAD = 1;
  private static final int MAX_THREAD = 256;
  private static final int MAX_SKIER= 100000;
  private static final int DEFAULT_LIFT = 40;
  private static final int MIN_LIFT = 5;
  private static final int MAX_LIFT = 60;
  private static final int DEFAULT_RUN = 10;
  private static final int MAX_RUN = 20;

  public boolean verify (int[] args) {
    if (args.length != ARG_NUM) return false;
    int threads = args[0], skiers = args[1], lifts = args[2], meanRuns = args[3];

    //TODO: handle default value
    return !isWithinRange(threads, MIN_THREAD, MAX_THREAD) || !isWithinRange(skiers, 0, MAX_SKIER) ||
        !isWithinRange(lifts, MIN_LIFT, MAX_LIFT) || !isWithinRange(meanRuns, 0, MAX_RUN);
  }

  private boolean isWithinRange(int num, int min, int max) {
    return num >= min && num <= max;
  }

}

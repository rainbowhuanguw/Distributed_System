package util;

import java.security.SecureRandom;

/**
 * Generates a random number within the given range
 */
public class RandomNumGenerator {
  private static final SecureRandom random = new SecureRandom();
  private static final int MIN_YEAR = 2000;
  private static final int MAX_YEAR = 2021;
  private static final int MIN_DAY = 1;
  private static final int MAX_DAY = 365;

  public static int generateNum(int min, int max) {
    return random.nextInt(max - min + 1) + min;
  }

  public static int generateYear() {
    return generateNum(MIN_YEAR, MAX_YEAR);
  }

  public static int generateDay() {
    return generateNum(MIN_DAY, MAX_DAY);
  }
}

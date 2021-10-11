package client1;

import java.security.SecureRandom;

/**
 * Generates a random number within the given range
 */
public class RandomNumGenerator {
  private static final SecureRandom random = new SecureRandom();

  public static int generateNum(int min, int max) {
    return random.nextInt(max - min + 1)+ min;
  }
}

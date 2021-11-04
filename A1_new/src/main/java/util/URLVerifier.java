package util;

import java.util.Arrays;
import java.util.List;

/**
 * Verifies urls
 */
public class URLVerifier {

  private static final int URL_LEN = 8;
  private static final String SEASON = "seasons";
  private static final String DAY = "days";
  private static final String SKIER = "skiers";
  private static final List<String> keywords = Arrays.asList(SEASON, DAY, SKIER);

  /**
   * Checks if a given list of url parts fits the
   * valid format: {resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
   * // url parts : [, resortID, seasons, seasonID, days, dayId, skiers, skierID]
   * // example url : http://localhost:8080/A1_war_exploded/skiers/r1/seasons1/s1/days/d1/skiers/123
   */
  public static boolean isValidSkierUrl(String[] urlParts) {
    if (urlParts.length < URL_LEN) return false;

    // examining from index 2
    for (int i = 2; i < URL_LEN; i += 2) {
      if (!urlParts[i].equals(keywords.get(i/2 - 1))) return false;
    }
    return true;
  }

}

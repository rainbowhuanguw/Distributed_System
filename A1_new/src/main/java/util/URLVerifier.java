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
  private static final String RESORT = "resorts";
  private static final List<String> keywords = Arrays.asList(SEASON, DAY, SKIER);

  /**
   * Checks if a given list of url parts fits the
   * valid format: skiers OR resorts/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
   * // url parts : [ , resortID, seasons, seasonID, days, dayId, skiers, skierID]
   * // example url : /A3_war_exploded/resorts/9/seasons/2017/days/264/skiers/8
   */
  public static boolean isValidURL(String[] urlParts) {
    if (urlParts.length < URL_LEN) return false;

    String type = getType(urlParts);
    if (!type.equals(SKIER) && !type.equals(RESORT)) return false;

    // examining from index 4
    for (int i = 4; i < URL_LEN; i += 2) {
      if (!urlParts[i].equals(keywords.get(i/2 - 2))) return false;
    }

    return true;
  }

  public static String getType(String[] urlParts) {
    return urlParts[2];
  }

  public static String getVertical(String[] urlParts) { return urlParts[4]; }

  public static int getResortId(String[] urlParts) {
    return Integer.parseInt(urlParts[3]);
  }

  public static int getSeasonId(String[] urlParts) {
    return Integer.parseInt(urlParts[5]);
  }

  public static int getDayId(String[] urlParts) {
    return Integer.parseInt(urlParts[7]);
  }

  public static int getSkierId(String[] urlParts) {
    return Integer.parseInt(urlParts[9]);
  }
}

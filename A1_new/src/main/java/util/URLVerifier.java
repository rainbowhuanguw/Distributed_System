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
   * valid format1: /A3_war_exploded/resorts/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
   * valid format2:/A3_war_exploded/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers
   * valid format3:/A3_war_exploded/skiers/{skierID}/vertical
   */
  public static boolean isValidURL(String[] urlParts) {
    if (urlParts.length <=2 ) return false;

    String type = getType(urlParts);
    if (!type.equals(SKIER) && !type.equals(RESORT)) return false;

    if (urlParts.length == 9 || urlParts.length == 10) {
      // examining from index 4
      if (!urlParts[4].equals("seasons") || !urlParts[6].equals("days") || !urlParts[8].equals("skiers")) {
        return false;
      }
      return true;
    } else if (urlParts.length == 5) {
      if (!urlParts[4].equals("vertical")) return false;
      return true;
    }

    return false;
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

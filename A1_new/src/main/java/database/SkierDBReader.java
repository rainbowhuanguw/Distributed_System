package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class SkierDBReader {
  private static final String DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver";
  private static final String DB_NAME = "skier"; // zhe's
                                      // "skiers";
  private static final String DATABASE_URL =
      //"jdbc:mysql://localhost:3306/" +
          //"jdbc:mysql://database-1.citnt9myvbnx.us-east-1.rds.amazonaws.com:3306/" +
          "jdbc:mysql://database-1.cnm5oojpbws5.us-east-1.rds.amazonaws.com:3306/" + // zhe's
          DB_NAME + "?createDatabaseIfNotExist=true";

  private static final String USERNAME = "admin"; //"root";
  private static final String PASSWORD = "12345678"; //"";
  private static final String MAX_POOL = "100";

  private static final String TABLE_NAME = "liftRide"; // zhe's
      // "t1";

  private static Properties properties = null;
  private static Connection connection = null;

  private static Map<Integer, Integer> skierToVertical;
  private static Map<Integer, Map<Integer, Integer>> dayToUniqueNumSkiers;

  public SkierDBReader(Map<Integer, Integer> skierToVertical, Map<Integer, Map<Integer, Integer>>
      dayToUniqueNumSkiers) {
    connect();

    SkierDBReader.skierToVertical = skierToVertical;
    SkierDBReader.dayToUniqueNumSkiers = dayToUniqueNumSkiers;
  }


  private static Properties getProperties() {
    if (properties == null) {
      properties = new Properties();
      properties.setProperty("user", USERNAME);
      properties.setProperty("password", PASSWORD);
      properties.setProperty("MaxPooledStatements", MAX_POOL);
    }
    return properties;
  }

  /**
   * Connect to database
   */
  private void connect() {
    if (connection == null) {
      try {
        Class.forName(DATABASE_DRIVER);
        // connect and create database if not exists
        connection = DriverManager.getConnection(DATABASE_URL, getProperties());
      } catch (ClassNotFoundException | SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Gets the number of liftrides taken by a specific skier on a given day in a given resort
   */
  public int getNumLiftRides(int resortId, int seasonId, int dayId, int skierId)
      throws SQLException {

    if (dayToUniqueNumSkiers.containsKey(dayId) &&
        dayToUniqueNumSkiers.get(dayId).containsKey(skierId)) {
      return dayToUniqueNumSkiers.get(dayId).get(skierId);
    }

    PreparedStatement statement = connection.prepareStatement(
        "SELECT COUNT(*) AS res FROM " + DB_NAME + "." + TABLE_NAME +
            " WHERE resortId=(?) AND seasonId=(?) AND dayId=(?) AND skierId=(?);");

    /*
    PreparedStatement statement = connection.prepareStatement(
        "SELECT COUNT(*) AS res FROM " + DB_NAME + "." + TABLE_NAME +
            " WHERE resortId=(?) AND season=(?) AND day=(?) AND skierId=(?);");
     */

    statement.setInt(1, resortId);
    statement.setInt(2, seasonId);
    statement.setInt(3, dayId);
    statement.setInt(4, skierId);

    ResultSet set = statement.executeQuery();
    int res = set.next() ? set.getInt("res") : 0;

    dayToUniqueNumSkiers.putIfAbsent(dayId, new ConcurrentHashMap<>());
    dayToUniqueNumSkiers.get(dayId).putIfAbsent(skierId, res);

    return res;
  }

  public int getVertical(int skierId) throws SQLException {
    if (skierToVertical.containsKey(skierId)) return skierToVertical.get(skierId);

    PreparedStatement statement = connection.prepareStatement(
        "SELECT SUM(vertical) AS res FROM " + DB_NAME + "." + TABLE_NAME +
            " WHERE skierId=(?);");

    /*
        PreparedStatement statement = connection.prepareStatement(
        "SELECT COUNT(*) AS res FROM " + DB_NAME + "." + TABLE_NAME +
            "WHERE skierId=(?);");

        statement.setInt(1, skierId);
    */

    statement.setInt(1, skierId);

    ResultSet set = statement.executeQuery();
    int res = set.next() ? set.getInt("res") : 0;

    skierToVertical.putIfAbsent(skierId, res);
    return res;
  }
}
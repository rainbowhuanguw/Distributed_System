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
import javax.xml.transform.Result;

public class ResortDBReader {
  private static final String DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver";
  private static final String DB_NAME = "resort"; // zhe's
  // "resorts"; // my db
  private static final String DATABASE_URL =
      //"jdbc:mysql://localhost:3306/" + // local db
      //"jdbc:mysql://database-2.citnt9myvbnx.us-east-1.rds.amazonaws.com:3306/" + // remote db
      "jdbc:mysql://database-3.cnm5oojpbws5.us-east-1.rds.amazonaws.com:3306/" + // zhe's
          DB_NAME + "?createDatabaseIfNotExist=true";
  private static final String USERNAME = "admin"; //"root"; //
  private static final String PASSWORD =  "12345678"; //""; //
  private static final String MAX_POOL = "100";

  private static final String TABLE_NAME = "liftRide"; // zhe's
                                            // "t1";
  private static Properties properties = null;
  private static Connection connection = null;
  private static Map<Integer, Integer> resortsDayToNumSkiers;

  private static Properties getProperties() {
    if (properties == null) {
      properties = new Properties();
      properties.setProperty("user", USERNAME);
      properties.setProperty("password", PASSWORD);
      properties.setProperty("MaxPooledStatements", MAX_POOL);
    }
    return properties;
  }

  public ResortDBReader(Map<Integer, Integer> map) {
    connect();
    resortsDayToNumSkiers = map;
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
   * Gets number of skiers visiting a specific resort on a given day
   */
  public int getSkierOfDay(int resortId, int seasonId, int dayId) throws SQLException {
    // use cached result
    if (resortsDayToNumSkiers.containsKey(dayId)) return resortsDayToNumSkiers.get(dayId);

    // otherwise get from db
    PreparedStatement statement = connection.prepareStatement(
        "SELECT COUNT(DISTINCT skierId) AS res FROM " + DB_NAME + "." + TABLE_NAME +
            " WHERE resortId=(?) AND seasonId=(?) AND dayId=(?);"); // zhe's

    /*
    PreparedStatement statement = connection.prepareStatement(
        "SELECT COUNT(DISTINCT skierId) AS res FROM " + DB_NAME + "." + TABLE_NAME +
            " WHERE resortId=(?) AND season=(?) AND day=(?);")
     */

    statement.setInt(1, resortId);
    statement.setInt(2, seasonId);
    statement.setInt(3, dayId);

    ResultSet set = statement.executeQuery();
    int res = set.next() ? set.getInt("res") : 0;

    // update hashmap
    resortsDayToNumSkiers.putIfAbsent(dayId, res);
    return res;
  }
}
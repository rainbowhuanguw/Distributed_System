package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class SkierDBReader {
  private static final String DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver";
  private static final String DB_NAME = "skiers";
  private static final String DATABASE_URL =
      "jdbc:mysql://localhost:3306/" +
          //"jdbc:mysql://database-1.citnt9myvbnx.us-east-1.rds.amazonaws.com:3306/" +
          DB_NAME + "?createDatabaseIfNotExist=true";
  private static final String USERNAME = "admin";
  private static final String PASSWORD = "12345678";
  private static final String MAX_POOL = "100";
  private static final String DELIM = ",";
  private static final int VERTICAL_LEN = 100;

  private static final String TABLE_NAME = "t1";

  private static final String DROP_TABLE_QUERY = "DROP TABLE " + TABLE_NAME + ";";
  private static final String CREATE_TABLE_QUERY =
      "CREATE TABLE " + TABLE_NAME + "(" +
          "resortId INTEGER NOT NULL, " +
          "season INTEGER NOT NULL, " +
          "day INTEGER NOT NULL, " +
          "hour INTEGER NOT NULL, " +
          "skierId INTEGER NOT NULL," +
          "liftId INTEGER NOT NULL" + ");";

  private static Properties properties = null;
  private static Connection connection = null;

  private static Properties getProperties() {
    if (properties == null) {
      properties = new Properties();
      //properties.setProperty("user", USERNAME);
      //properties.setProperty("password", PASSWORD);
      properties.setProperty("MaxPooledStatements", MAX_POOL);
    }
    return properties;
  }

  /**
   * Connect to database
   */
  private static void connect() {
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
  public static int getNumLiftRides(int resortId, int seasonId, int dayId, int skierId)
      throws SQLException {
    connect();

    PreparedStatement statement = connection.prepareStatement(
        "SELECT COUNT(*) AS res FROM " + DB_NAME + "." + TABLE_NAME +
            " WHERE resortId=(?) AND season=(?) AND day=(?) AND skierId=(?);");

    statement.setInt(1, resortId);
    statement.setInt(2, seasonId);
    statement.setInt(3, dayId);
    statement.setInt(4, skierId);

    ResultSet res = statement.executeQuery();
    return res.getInt("res");
  }

  public static int getVertical(int skierId) throws SQLException {
    connect();

    PreparedStatement statement = connection.prepareStatement(
        "SELECT COUNT(*) AS res FROM " + DB_NAME + "." + TABLE_NAME +
            "WHERE skierId=(?);");

    statement.setInt(1, skierId);
    ResultSet res = statement.executeQuery();
    return res.getInt("res");
  }
}
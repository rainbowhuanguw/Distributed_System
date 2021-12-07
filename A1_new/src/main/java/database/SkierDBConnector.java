package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.swing.plaf.nimbus.State;

public class SkierDBConnector {
  private static final String DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver";
  private static final String DB_NAME = "skiers";
  private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/" +
                                              DB_NAME + "?createDatabaseIfNotExist=true";
  private static final String USERNAME = "root";
  private static final String PASSWORD = "";
  private static final String MAX_POOL = "100";

  private static final String TABLE_NAME = "t1";

  private static final String DROP_TABLE_QUERY = "DROP TABLE " + TABLE_NAME + ";";
  private static final String CREATE_TABLE_QUERY = "CREATE TABLE " + TABLE_NAME + "(" +
          "skierId INTEGER not NULL, " +
          "season INTEGER not NULL, " +
          "day INTEGER not NULL, " +
          "liftId INTEGER not NULL," +
          "numLiftRides INTEGER not NULL" + ");";

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
   * write to database
   */
  public static void write(int skierId, int season, int day, int liftId, int liftRides)
      throws SQLException {
    connect();
    createTable();

    // insert data using prepared statement
    PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?, ?, ?)");

    // 1-based indices
    statement.setInt(1, skierId);
    statement.setInt(2, season);
    statement.setInt(3, day);
    statement.setInt(4, liftId);
    statement.setInt(5, liftRides);

    // execute
    statement.executeUpdate();
  }

  /**
   * Create default table
   */
  private static void createTable() {
    if (connection == null) return;

    try (Statement statement = connection.createStatement();) {
      DatabaseMetaData metadata = connection.getMetaData();
      // check if the table exists
      ResultSet tables = metadata.getTables(null, null, TABLE_NAME, null);

      // drop existing table if exists
      if (tables.next()) {
        statement.executeUpdate(DROP_TABLE_QUERY);
      }
      statement.executeUpdate(CREATE_TABLE_QUERY);
    } catch (SQLException e) {
      e.printStackTrace();
    }
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

  public static void main(String[] args) {
    try {
      SkierDBConnector.write(1,
          2,
          3,
          4,
          5);
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }
}



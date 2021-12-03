package database;

import info.ResortRow;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Establishes connection to a mySQL database
 * modified from https://stackoverflow.com/questions/2839321/connect-java-to-a-mysql-database
  */
public class ResortDBConnector extends GeneralDBConnector {
  private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
  private static final String DB_NAME = "resorts";
  private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/" +
      DB_NAME + "?createDatabaseIfNotExist=true";
  private static final String USERNAME = "root";
  private static final String PASSWORD = "123456";
  private static final String MAX_POOL = "100";

  private static final String TABLE_NAME = "t1";
  private static final String CREATE_TABLE_QUERY = "CREATE TABLE " + TABLE_NAME + "(" +
      "resortId INTEGER not NULL, " +
      "liftId INTEGER not NULL" + ");";

  private static Properties properties = null;
  private static Connection connection = null;

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
   * write to database
   */
  public static void write(int resortId, int liftId) throws SQLException {
    connect();
    createTable();

    // TODO : insert data
    PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO" + TABLE_NAME + "VALUES(?, ?)");

    statement.setInt(1, resortId);
    statement.setInt(1, liftId);

    statement.executeUpdate();
  }

  /**
   * Create default table
   */
  public static void createTable() {
    if (connection == null) return;

    try (Statement statement = connection.createStatement();) {
      DatabaseMetaData metadata = connection.getMetaData();
      // check if the table exists
      ResultSet tables = metadata.getTables(null, null, TABLE_NAME, null);

      // only create table when table doesn't exist
      if (!tables.next()) {
        statement.executeUpdate(CREATE_TABLE_QUERY);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Connect to database
   */
  public static void connect() {
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
}

package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public abstract class GeneralDBConnector {
  protected Connection connection = null;

  /**
   * Connect to database
   */
  public Connection connect(String databaseDriver, String databaseURL, Properties properties) {
    if (connection == null) {
      try {
        Class.forName(databaseDriver);
        // connect and create database if not exists
        connection = DriverManager.getConnection(databaseURL, properties);
      } catch (ClassNotFoundException | SQLException e) {
        e.printStackTrace();
      }
    }

    return connection;
  }

  /**
   * Create a table
   */
  public void createTable(String query, String tableName) {
    if (connection == null) return;

    try (Statement statement = connection.createStatement();) {
      DatabaseMetaData metadata = connection.getMetaData();
      // check if the table exists
      ResultSet tables = metadata.getTables(null, null, tableName, null);

      // only create table when table doesn't exist
      if (!tables.next()) {
        statement.executeUpdate(query);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Disconnect from database
   */
  public void disconnect() {
    if (connection != null) {
      try {
        connection.close();
        connection = null;
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

}

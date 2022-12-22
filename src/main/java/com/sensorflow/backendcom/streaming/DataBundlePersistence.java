package com.sensorflow.backendcom.streaming;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sensorflow.persistence.DatabasePersistenceService;

/**
 * Handles persistence of databundle messages to SQLite DB
 */
@Service
@SuppressWarnings("squid:S2095")
public class DataBundlePersistence {

    @Autowired
    DatabasePersistenceService databasePersistenceService;

    /**
     * On startup connect to database and create the table if it doesnt exist
     * @throws SQLException
     */
    @PostConstruct
    public void init() throws SQLException {
        setup();
    }

    /**
     * Create the table in SQLite database if it doesnt exist
     * @throws SQLException
     */
    private void setup() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS databundles (\n" + "	id integer PRIMARY KEY,\n" + "	databundle text NOT NULL\n" + ");";
        try(Connection dbConnection = databasePersistenceService.getDbConnection();
            Statement statement = dbConnection.createStatement()
        ) {
            statement.execute(sql);
        }
    }

    /**
     * Add data bundle to the table
     * @param dataBundleJsonString
     * @return int generated id
     * @throws SQLException
     */
    public int addDataBundle(String dataBundleJsonString) throws SQLException {
        if(dataBundleJsonString == null){
            throw new IllegalArgumentException("received data bundle null string");
        }
//        int generatedKey = -1;
//        String sqlInsert = "INSERT INTO databundles(databundle) VALUES(?);";
//        try(Connection dbConnection = databasePersistenceService.getDbConnection();
//            PreparedStatement preparedStatement = dbConnection.prepareStatement(sqlInsert)
//        ) {
//            preparedStatement.setString(1, dataBundleJsonString);
//            preparedStatement.executeUpdate();
//            ResultSet rs = preparedStatement.getGeneratedKeys();
//            rs.next();
//            generatedKey = rs.getInt(1);
//
//
//        }
        return 1;

    }

    /**
     * Remove databundle from the table given databundle id
     * @param dataBundleId
     * @throws SQLException
     */
    public void removeDataBundle(int dataBundleId) throws SQLException {
        String sql = "DELETE FROM databundles WHERE id = ?";
        try(Connection dbConnection = databasePersistenceService.getDbConnection();
            PreparedStatement preparedStatement = dbConnection.prepareStatement(sql)
        ) {
            preparedStatement.setInt(1, dataBundleId);
            preparedStatement.executeUpdate();
        }
    }

    String convertIds(Set<Integer> ids){
        // For each id, convert it to a string, and then join it no the next one with a comma
        // We cannot use Strings.join directly, because it will print the whole set as '[1,2,3]' - with the outer brackets
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public void removeDataBundles(Set<Integer> dataBundleIds) throws SQLException {
        String sql = "DELETE FROM databundles WHERE id in (" + convertIds(dataBundleIds) + ")";
        try(Connection dbConnection = databasePersistenceService.getDbConnection();
            PreparedStatement preparedStatement = dbConnection.prepareStatement(sql)
        ) {
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Get a collection of databundles from the database
     * @param limit - maximum number of messages to return
     * @return collection of databundles where key is id, value is the message
     * @throws SQLException
     */
    public Map<Integer,String> getDataBundlesJson(int limit) throws SQLException {
        HashMap<Integer, String> dataBundles = new HashMap<>();
        String sqlQuery = "SELECT * FROM databundles LIMIT ?;";
        try(Connection dbConnection = databasePersistenceService.getDbConnection();
            PreparedStatement preparedStatement = dbConnection.prepareStatement(sqlQuery)
        ) {
            preparedStatement.setInt(1, limit);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                dataBundles.put(resultSet.getInt(1), resultSet.getString(2));
            }
        }
        return dataBundles;
    }

    public int countBundles() throws SQLException {
        String sqlQuery = "SELECT count(*) FROM databundles ";
        int count = 0;
        try(Connection dbConnection = databasePersistenceService.getDbConnection();
            PreparedStatement preparedStatement = dbConnection.prepareStatement(sqlQuery)
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        }
        return count;
    }


    /**
     * Remove all messages from database
     * @throws SQLException
     */
    public void clearAllDatabundles() throws SQLException {
        String sql = "DELETE FROM databundles";
        try(Connection dbConnection = databasePersistenceService.getDbConnection();
            Statement statement = dbConnection.createStatement()
        ) {
            statement.execute(sql);
        }
    }


    public Integer getDbSize() throws SQLException {
        String sql = "SELECT COUNT(*) FROM databundles";
        try(Connection dbConnection = databasePersistenceService.getDbConnection();
            PreparedStatement preparedStatement = dbConnection.prepareStatement(sql)
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
              return  (resultSet.getInt(1));
            }
        }
        return null;
    }

}

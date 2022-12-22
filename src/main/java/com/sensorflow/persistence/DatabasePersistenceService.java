package com.sensorflow.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sensorflow.backendcom.newrelic.ErrorReporter;
import com.sensorflow.backendcom.newrelic.ErrorTypes;
import com.sensorflow.config.GlobalSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Provides Hikari connection pool for DB access
 */
@Service
public class DatabasePersistenceService {

    static final Logger logger = LoggerFactory.getLogger(DatabasePersistenceService.class);
    private static final long ONE_HOUR_MILLIS = 1 * 60 * 60 * 1000l ;
    static final int MAX_TIMEOUTS_PER_HOUR = 10;
    private final int POOL_SIZE = 1;
    private final int LEAK_DETECTION_TIMEOUT_MS = 60 * 1000;

    HikariDataSource dataSource;


    private List<Long> connectionTimeouts = new LinkedList<>();

    @Autowired
    private GlobalSettings globalSettings;
    private HikariConfig hikariConfig;

    /**
     * Initialize hikari connection pool
     */
    @PostConstruct
    void init() {
        createConnectionPool();
    }

    private void createConnectionPool() {
        hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(globalSettings.getJdbcPath());
        hikariConfig.setMaximumPoolSize(POOL_SIZE);
        hikariConfig.setLeakDetectionThreshold(LEAK_DETECTION_TIMEOUT_MS);
        hikariConfig.setConnectionTimeout(60 * 1000l); // how long to wait for a connection
        hikariConfig.setMaxLifetime(1800000); //how long connection will live in the pool

        dataSource = new HikariDataSource(hikariConfig);
    }

    private void connectionTimeoutOccured(SQLTransientConnectionException exception){
        if (!exception.getMessage().contains("Connection is not available, request timed out after")){
            logger.warn("SQLTransientConnectionException which is not connection timeout occured! {}", exception.getMessage());
        }
        try{
            List<Long> connectionTimeoutsBak = new ArrayList<>();
            for (int i = 0; i < connectionTimeouts.size(); i++) {
                Long timeout = connectionTimeouts.get(i);
                if(timeout != null){
                    connectionTimeoutsBak.add(timeout);
                }else{
//                    for some reason connectionTimeouts.removeIf(this::isTimestampTooOld); is throwing NPEs, trying to get to the bottom of it
                    ErrorReporter.reportError("NULL in connection Timeouts", ErrorTypes.GATEWAY_ERROR);
                }
            }
            connectionTimeouts.clear();
            connectionTimeouts.addAll(connectionTimeoutsBak);
            connectionTimeouts.removeIf(this::isTimestampTooOld);
        }catch(NullPointerException e){
            connectionTimeouts.clear();
        }
        connectionTimeouts.add(System.currentTimeMillis());
        if(connectionTimeouts.size() > MAX_TIMEOUTS_PER_HOUR){
            ErrorReporter.reportError("Too many db connection timeouts", ErrorTypes.GATEWAY_ERROR);
            exitProgram();
        }
    }

    private boolean isTimestampTooOld(long timestamp){
        return System.currentTimeMillis() - timestamp > ONE_HOUR_MILLIS;
    }

    void exitProgram(){
        System.exit(1);
    }

    public Connection getDbConnection() throws SQLException {
        Connection connection;
        try {
            connection = dataSource.getConnection();
            connectionTimeouts.clear();
        } catch (SQLTransientConnectionException exception){
            // This try catch is purely for incrementing the WDog Timer
            connectionTimeoutOccured(exception);
            throw exception;
        }
//        DebugConnection debugConnection = new DebugConnection(connection);
//        return debugConnection;
        return connection;
    }

//    this method attempts to reset the data source, but it doesn;t seem to work, we get datasource locked currently
//    if we use this
    private Connection resetDataSource() throws SQLException {
        if(dataSource != null && !dataSource.isClosed()){
            dataSource.close();
        }
        dataSource = new HikariDataSource(hikariConfig);
        Connection connection = dataSource.getConnection();
        connectionTimeouts.clear();
        return connection;
    }

}

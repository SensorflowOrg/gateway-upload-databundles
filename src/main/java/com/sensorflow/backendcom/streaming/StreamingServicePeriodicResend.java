package com.sensorflow.backendcom.streaming;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonParseException;
import com.sensorflow.backendcom.newrelic.ErrorReporter;
import com.sensorflow.backendcom.newrelic.MetricReporter;
import com.sensorflow.backendcom.streaming.model.DataBundle;
import com.sensorflow.config.GlobalSettings;
import com.sensorflow.gateway.health.OnlineCheckService;

/**
 * Periodically resends data bundle messages stored in SQLite database
 */
@Service
public class StreamingServicePeriodicResend {

    private static final int MAX_BUNDLE_NUMBER = 50;

    private static final int PERIODIC_RESEND_TIMER_TIMEOUT_MS = 2000;

    private static final long PERIODIC_RESEND_TIMER_PERIOD_MS = PERIODIC_RESEND_TIMER_TIMEOUT_MS + 5000L;

    private static Logger logger = LoggerFactory.getLogger(StreamingServicePeriodicResend.class);


    @Autowired
    DataBundlePersistence dataBundlePersistence;
//
//    @Autowired
//    HttpRequestMaker httpRequestMaker;

    @Autowired
    OnlineCheckService onlineCheckService;

    @Autowired
    GlobalSettings globalSettings;

    @Autowired
    StreamingService streamingServiceController;

    @Value("${UNIT_TEST_ENABLED:false}")
    boolean unitTestEnabled;

    ScheduledExecutorService timerService = Executors.newSingleThreadScheduledExecutor();

    private int totalBundles;
    private int currentBundles;

    /**
     * On statrtup initialize and start periodic executor service, register resend task function and
     */
    @PostConstruct
    public void init(){
        try {
            totalBundles =  dataBundlePersistence.getDbSize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        timerService.scheduleAtFixedRate(this::periodicResendTask, PERIODIC_RESEND_TIMER_PERIOD_MS, PERIODIC_RESEND_TIMER_PERIOD_MS, TimeUnit.MILLISECONDS);
        logger.info("Starting Periodic resend");

    }


    /**
     * Executes in a single thread at a fixed rate, but not in parallel, i.e. if the thread is running longer than expected,
     * the next execution will be scheduled as soon as the previous finished, but not earlier
     */
    void periodicResendTask() {
        try {
            logger.info("{} bundles in DB",  dataBundlePersistence.getDbSize());
            if (onlineCheckService.isOnline()) {
                Map<Integer, String> dataBundles = dataBundlePersistence.getDataBundlesJson(MAX_BUNDLE_NUMBER);
                currentBundles += dataBundles.size();
                if (dataBundles != null && dataBundles.size() > 0) {
                    logger.info("Databundle sending {} / {} bundles",  currentBundles, totalBundles);
                    for (Map.Entry<Integer, String> entry : dataBundles.entrySet()) {
                        parseAndTransmitBundle(entry);
                    }
                }
            } else {
                logger.info("Databundle resend: sending bundles is suspended due to gateway being offline");
            }
            Integer dbSize = dataBundlePersistence.getDbSize();
            if(dbSize>0){
                MetricReporter.recordMetric(MetricReporter.STORED_BUNDLES, dbSize);
            }
        } catch (Exception e) {
            ErrorReporter.reportError(e);
        }
    }

    private void parseAndTransmitBundle(Map.Entry<Integer, String> entry) throws SQLException {
        try{
            DataBundle dataBundle = DataBundle.fromJson(entry.getValue());
            dataBundle.setId(entry.getKey());
            streamingServiceController.manageDataBundle(dataBundle);
        } catch(Exception e){
            logger.error("error when parseAndTransmitBundle bundle {} {}",entry.getKey(), e.getMessage());
            ErrorReporter.reportError(e);
            dataBundlePersistence.removeDataBundle(entry.getKey());
        }
    }
}

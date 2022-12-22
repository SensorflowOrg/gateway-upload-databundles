package com.sensorflow.backendcom.streaming;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sensorflow.backendcom.hasura.HasuraClient;
import com.sensorflow.backendcom.newrelic.ErrorReporter;
import com.sensorflow.backendcom.streaming.model.DataBundle;
import com.sensorflow.config.GlobalSettings;
import com.sensorflow.gateway.health.OnlineCheckService;


import lombok.extern.slf4j.Slf4j;


/**
 * Streaming Service Controller
 * Used to send data bundles to timescale db and persist data bundles if
 * send was unsuccessful
 */
@Service
@Slf4j
public class StreamingService {
    static final int MAX_QUEUED_REQUESTS = 100;
    static final int QUEUE_MAX_SIZE = 1000;
    static final int QUEUE_MAX_SIZE_BEFORE_FLUSH = 200;
    private static final int MAX_PACKED_DATABUNDLES_PER_TRANSMISSION = 50;

    @Value("${streaming.delay:100}")
    long delayBetweenTransmissionsMs;

    @Value("${streaming.initial:0}")
    long initialDelayMs;

    @Autowired
    DataBundlePersistence dataBundlePersistence;

    @Autowired
    GlobalSettings globalSettings;

    @Autowired
    OnlineCheckService onlineCheckService;

    @Autowired
    HasuraClient hasuraClient;

    @Autowired
    StreamingExecutor streamingExecutor;


    ScheduledExecutorService periodicResendExecutor = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> scheduledFuture;

    @PostConstruct
    public void init(){
        scheduledFuture = periodicResendExecutor.scheduleWithFixedDelay(this::handleBundles, initialDelayMs, delayBetweenTransmissionsMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Why use a BlockingQueue?
     * We need it to be blocking so it is synchronized (between threads),
     * and we can wait until a new packet arrives, rather than polling it
     */
    private final BlockingQueue<DataBundle> dataBundleQueue = new LinkedBlockingQueue<>(QUEUE_MAX_SIZE);

    /**
     * Given data bundle json will be persisted and transmitted asap
     *
     * @param bundle the DataBundle to send
     */
    public void manageDataBundle(DataBundle bundle) {
        if(bundle == null){
            throw new IllegalArgumentException("bundle can't be null");
        }
        if (!dataBundleQueue.offer(bundle)) {
            log.warn("couldn't offer bundle to queue {} {}", bundle, dataBundleQueue.size());
        }
    }

    // This should be a single thread which keeps on running
    void handleBundles() {
        try {
            if (isQueueTooFull()) {
                log.warn("Flushing Databundle Queue - connectivity is unstable and queue too full");
                persistAndFlushDataBundleQueue();
            } else {
                dequeueDataBundles().forEach(this::transferDataBundle);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ErrorReporter.reportError(e);
        } catch (Throwable e) {//Catch all as this can never fail completely, otherwise transmissions would stop
            ErrorReporter.reportError(e);
        }
    }

    boolean isQueueTooFull() {
        return dataBundleQueue.size() > QUEUE_MAX_SIZE_BEFORE_FLUSH;
    }

    void persistAndFlushDataBundleQueue() {
        int count = 0;
        while (!dataBundleQueue.isEmpty()) {
            // Since dataBundlePersistence could fail, don't poll the queue right away. First very we sucessfully wrote
            // to db, and only then remove the databundle from the queue
            DataBundle dataBundle = dataBundleQueue.remove();
            try {
               dataBundlePersistence.addDataBundle(dataBundle.toJson());
            } catch (SQLException e) {
                ErrorReporter.reportError(e);
            }
            count++;
            if (count % 50 == 0) {
                log.warn("Still persisting databundles, persisted {}", count);
            }
        }
    }


    private List<DataBundle> dequeueDataBundles() throws InterruptedException {
        List<DataBundle> databundlesToBeSent = new LinkedList<>();
        int i = 0;
        do {
            // Wait for the first bundle indefinitely, and
            // Take up to <50> bundles from the queue, but only if they're available in the queue,
            // to make sure that we're sending data as soon as it is available

            DataBundle dataBundle = dataBundleQueue.take();
            databundlesToBeSent.add(dataBundle);
            i++;
        } while (i < MAX_PACKED_DATABUNDLES_PER_TRANSMISSION && !dataBundleQueue.isEmpty());
        return databundlesToBeSent;
    }


    /**
     * This will send an inout mutation upstream if the gateway is online and we dio  not have too high of a
     * backlog of queued requests in memory
     *
     * @param bundle the bundle to send
     */
    void transferDataBundle(DataBundle bundle) {
        if(bundle == null ){
            return;
        }else if(bundle.getId() !=null && bundle.getBundledDataType() == null){
            removeDataBundle(bundle);
            return;
        }
        try {
            if (onlineCheckService.isOnline()){
                switch (bundle.getBundledDataType()) {
                    case GATEWAYS:
                        streamingExecutor.upsertGatewayInfos(bundle);
                        break;
                    case NODE_MEASUREMENTS:
                        streamingExecutor.insertNodeMeasurements(bundle);
                        break;
                    case GATEWAY_HEALTH_DATA:
                        streamingExecutor.insertGatewayHealthData(bundle);
                        break;
                    case NODE_JOIN_DATA:
                        streamingExecutor.insertNodeJoinData(bundle);
                        break;
                    case NODE_META_DATA:
                        streamingExecutor.insertNodeMetaData(bundle);
                        break;
                    case NODE_SYSTEM_STATES:
                        streamingExecutor.insertNodeSystemStates(bundle);
                        break;
                    default:
                        removeDataBundle(bundle);
                        break;
                }
            } else {
                log.info("Not sending data to timescale, gateway is offline");
            }
        }catch(Exception e){
            log.info("transferDataBundle error {}", e.getMessage());
            ErrorReporter.reportError(e);
            if(bundle != null && bundle.getId() !=null){
                removeDataBundle(bundle);
            }
        }
    }

    private void removeDataBundle(DataBundle bundle) {
        try {
            dataBundlePersistence.removeDataBundle(bundle.getId());
        } catch (SQLException e) {
            log.info("removeDataBundle error {}", e.getMessage());
            ErrorReporter.reportError(e);
        }
    }

    int getQueueSize() {
        return dataBundleQueue.size();
    }

    public boolean isQueueEmpty() {
        return dataBundleQueue.isEmpty();
    }

}

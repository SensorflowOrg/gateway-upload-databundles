package com.sensorflow.gateway.health;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sensorflow.backendcom.newrelic.ErrorReporter;

/**
 * Online Check Server - periodically checks internet connection and holds the connection status
 */
@Service
public class OnlineCheckService {
    private static final Logger logger = LoggerFactory.getLogger(OnlineCheckService.class);
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Value("${HASURA_ENDPOINT}")
    private String hasuraApiUrl;

    private String hasuraApiSocketUrl;

    private static final int OPEN_PORT = 443;
    private static final int TIMEOUT_MS = 25000;
    private static final int INITIAL_DELAY_MS = 0;
    private static final int PERIOD_MS = 30000;
    private boolean isOnline;


    @PostConstruct
    @SuppressWarnings("squid:S1612")
    public void init() throws URISyntaxException {
        hasuraApiSocketUrl = new URI(hasuraApiUrl).getHost();
        executorService.scheduleAtFixedRate(this::checkOnline, INITIAL_DELAY_MS, PERIOD_MS, TimeUnit.MILLISECONDS);
        logger.debug("Online health checker is pinging this address to check for online: {}", hasuraApiSocketUrl);
    }

    private boolean isReachable(String addr, int openPort, int timeOutMillis) {
        // Any Open port on other machine
        // openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
                return true;
            }catch (IOException ex) {
                return false;
            }
    }

    private void checkOnline(){
        try {
            if(isReachable(hasuraApiSocketUrl, OPEN_PORT, TIMEOUT_MS)){
                isOnline = true;
            }else{
                logger.info("Gateway is offline");
                isOnline = false;
            }
        } catch (Throwable e) {
            ErrorReporter.reportError(e);
        }
    }

    public boolean isOnline() {
        return isOnline;
    }
}

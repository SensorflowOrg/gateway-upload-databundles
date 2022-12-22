package com.sensorflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayUploadDataBundlesApplication {
  private static Logger logger =  LoggerFactory.getLogger(GatewayUploadDataBundlesApplication.class);

  public static void main(String[] args) {
    logger.debug("Starting Sensorflow Gateway data bundles upload");
    SpringApplication.run(GatewayUploadDataBundlesApplication.class, args);
  }
}

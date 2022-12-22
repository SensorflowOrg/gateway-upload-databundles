package com.sensorflow.config;

// import com.sensorflow.airlink.model.LoraRegion;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@PropertySource("classpath:application.yml")
@SuppressWarnings({"squid:S00116","squid:S00100"})
public class GlobalSettings {

    public static final int DOOR_AIRCON_OFF = 0;
    public static final int DOOR_AIRCON_FAN = 1;

    @Value("${STATE_STALE_TIMEOUT: 3600000}") // 30 minutes
    private long STATE_STALE_TIMEOUT;

    @Value("${SENSORFLOW_BME_FIRMWARE: false}")
    private boolean bmeFirmware;

    @Value("${SENSORFLOW_DOOR_SENSOR:-1}")
    int doorSensorBehavior;

    @Value("${GPIO_TIMING_ENABLE: false}")
    private boolean gpioTimingEnable;

	@Value("${BASE_DIRECTORY:/data/}")
	private String baseDirector;

	@Value("${JDBC_PATH}")
    private String jdbcPath;

    @Value("${LOCATION_ID}")
    private String locationId;

    @Value("${OFFLINE_RESTART_TIMEOUT:12}")
    private long offlineRestartTimeout_H;

    @Value("${SEND_TRIGGERS_IMMEDIATELY: false}")
    @Getter private boolean sendTriggersImmediately;

	private int sessionId;



    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    @PostConstruct
    public void init() {
    	if(doorSensorBehavior > 1 || doorSensorBehavior < 0) {
    		doorSensorBehavior = DOOR_AIRCON_FAN;
    	}
    }

    public int getDoorSensorBehavior() {
        return doorSensorBehavior;
    }

    public void setDoorSensorBehavior(int doorSensorBehavior) {
        this.doorSensorBehavior = doorSensorBehavior;
    }

    public boolean isBmeFirmware() {
        return bmeFirmware;
    }


    public boolean isGpioTimingEnable() {
        return gpioTimingEnable;
    }

    public void setGpioTimingEnable(boolean gpioTimingEnable) {
        this.gpioTimingEnable = gpioTimingEnable;
    }

	public String getBaseDirector() {
		return baseDirector;
	}

	public void setBaseDirector(String baseDirector) {
		this.baseDirector = baseDirector;
	}

    public String getJdbcPath() {
        return jdbcPath;
    }

    public String getLocationId() {
        return locationId;
    }

    public long getStateStaleTimeout() {
        return STATE_STALE_TIMEOUT;
    }

    public long getOfflineRestartTimeout_H() {
        return offlineRestartTimeout_H;
    }


}

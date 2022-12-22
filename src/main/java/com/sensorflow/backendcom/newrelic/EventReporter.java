package com.sensorflow.backendcom.newrelic;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import com.sensorflow.backendcom.newrelic.model.Event;
import com.sensorflow.config.SensorflowConfig;
import com.sensorflow.util.Pair;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class EventReporter {
    public static final String NODE_JOINED = "Custom:Nodes:node_join_request";
    public static final String NODE_JOIN_CONFIRMED = "Custom:Nodes:node_join_confirmed";
    public static final String NODE_REBOOT = "Custom:Nodes:node_reboot";
    public static final String GATEWAY_REBOOT = "Custom:Gateways:gateway_reboot";
    public static final String CONCENTRATOR_RESTART = "Custom:Gateways:concentrator_restart";
    public static final String NODE_JOIN_REJECTED = "Custom:Nodes:join_rejected";
    public static final String NODE_INITIALISED = "Custom:Nodes:initialised";
    public static final String NODE_INIT_FAILED = "Custom:Nodes:init_failed";
    public static final String PLANNING_OTAS = "Custom:Gateways:planning_otas";
    public static final String ROLLING_OUT_OTA = "Custom:Gateways:ota_rollout";
    public static final String OTA_FINISHED = "Custom:Gateways:ota_finished";
    public static final String BEACON_CLASH_DETECTED = "Custom:Gateways:beacon_clash";
    public static final String REMOTE_BEACON_CLASH_DETECTED = "Custom:Gateways:remote_beacon_clash";
    public static final String DENSE_BACONS = "Custom:Gateways:dense_beacons";
    public static final String REALIGNING_BEACON = "Custom:Gateways:realigning_beacon";
    public static final String AUTOMATION_PAUSED = "Custom:Autoset:entered_automation_paused";
    public static final String UPDATE_STORED_SETTINGS = "Custom:Autoset:update_stored_settings";
    public static final String NODE_CHANGED_TYPE = "Custom:Nodes:node_changed_type";
    public static final String NONE_AUTOMATION = "Custom:Automation:none_automation";
    public static final String STRAY_NODE = "Custom:Gateways:stray_node";
    public static final String INSTALLATION_MODE = "Custom:Gateways:installation_mode";
    public static final String SUBSCRIPTION_EVENT = "Custom:Gateways:subscription_event";
    public static final String CONFIGURATION_EVENT = "Custom:Gateways:configuration_event";

    private static OkHttpClient httpClient;

    private static SensorflowConfig sensorflowConfig;

    private EventReporter(){}

    public static void recordEvent(String eventName, String key, String value){
        recordEvent(eventName, new Pair<>(key, value));
    }

    @SafeVarargs
    public static void recordEvent(String eventName, Map.Entry<String, Object> ... attributes ){
        if(sensorflowConfig != null) {
            Event event = new Event(eventName, sensorflowConfig.getBalenaDeviceNameAtInit(),
                    sensorflowConfig.getLocationId(), sensorflowConfig.getNewRelicEntityGUID(), attributes);
            recordEvent(event);
        }
    }

    public static void recordEvent(String eventName) {
        recordEvent(new Event(eventName, sensorflowConfig.getBalenaDeviceNameAtInit(),
                sensorflowConfig.getLocationId(), sensorflowConfig.getNewRelicEntityGUID()));
    }

    public static void recordEvent(Event event){
        String json = event.toJson();
        RequestBody body = RequestBody.create(json,
                MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://insights-collector.eu01.nr-data.net/v1/accounts/3209041/events")
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //ignore as it's just monitoring data
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    public static void setSensorflowConfig(SensorflowConfig sensorflowConfig) {
        EventReporter.sensorflowConfig = sensorflowConfig;
        if(sensorflowConfig != null && sensorflowConfig.getNewRelicInsertKey() != null) {
            httpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                            .addHeader("X-Insert-Key", sensorflowConfig.getNewRelicInsertKey())
                            .addHeader("Content-Type", "application/json")
                            .build())).addInterceptor(new GzipRequestInterceptor())
                    .callTimeout(5, TimeUnit.SECONDS)
                    .build();
        }
    }

    public static void recordEvent(String eventName, Throwable e, Map<String, Object> params) {
        if(sensorflowConfig != null) {
            Event event = new Event(eventName, sensorflowConfig.getBalenaDeviceNameAtInit(),
                    sensorflowConfig.getLocationId(), sensorflowConfig.getNewRelicEntityGUID(),
                    e, params);
            recordEvent(event);
        }
    }

    public static void recordEvent(String eventName, Map<String, Object> params) {
        if(sensorflowConfig != null) {
            Event event = new Event(eventName, sensorflowConfig.getBalenaDeviceNameAtInit(),
                    sensorflowConfig.getLocationId(), sensorflowConfig.getNewRelicEntityGUID(), params);
            recordEvent(event);
        }
    }
}

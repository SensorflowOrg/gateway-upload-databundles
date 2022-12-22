package com.sensorflow.backendcom.newrelic;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import com.sensorflow.backendcom.newrelic.model.Metric;
import com.sensorflow.config.SensorflowConfig;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MetricReporter {

    public static final String STORED_BUNDLES = "Custom/BackendCom/stored_bundles";
    public static final String CONNECTED_NODES = "Custom/Nodes/connected_nodes";
    public static final String RESERVED_CONNECTIONS = "Custom/Nodes/reserved_connections";
    public static final String DISCONNECTED_NODES = "Custom/Nodes/disconnected_nodes";
    public static final String REJECTED_NODES = "Custom/Nodes/rejected_nodes";
    public static final String MAX_BEACON_GAP = "Custom/Gateways/max_beacon_gap_ms";
    public static final String FREE_DISK_SPACE_PERCENT = "Custom/Gateways/free_disk_space_percent";
    public static final String THREAD_COUNT = "Custom/Gateways/thread_count";
    public static final String CPU_TEMPERATURE = "Custom/Gateways/cpu_temperature";
    public static final String OTAS_SCHEDULED = "Custom/Gateways/num_otas_scheduled";
    public static final String BEACONS_RECEIVED_ON_GATEWAY = "Custom/Gateways/beacons_received";
    public static final String FLASH_ERASE_COUNT = "Custom/Nodes/flash_erase_count";
    public static final String NVM_HIGH_WATER = "Custom/Nodes/high_water/nvm";
    public static final String IDLE_HIGH_WATER = "Custom/Nodes/high_water/idle";
    public static final String PROCSETT_HIGH_WATER = "Custom/Nodes/high_water/procSetT";
    public static final String AMTXT_IMMEDIATELY = "Custom/Nodes/high_water/amtxtim";
    public static final String JOINT_HIGH_WATER = "Custom/Nodes/high_water/joinT";
    public static final String SYNCT_HIGH_WATER = "Custom/Nodes/high_water/syncT";
    public static final String LISTENT_HIGH_WATER = "Custom/Nodes/high_water/listenT";
    public static final String CONTLISTT_HIGH_WATER = "Custom/Nodes/high_water/ContListT";
    public static final String OTAPREPT_HIGH_WATER = "Custom/Nodes/high_water/OtaPrepT";
    public static final String STARTUP_HIGH_WATER = "Custom/Nodes/high_water/Startup";
    public static final String AMTXT_HIGH_WATER = "Custom/Nodes/high_water/AmTxT";
    public static final String AMRECVT_HIGH_WATER = "Custom/Nodes/high_water/AmRecvT";
    public static final String TIMERTASK_HIGH_WATER = "Custom/Nodes/high_water/TimerTask";
    public static final String SENDMON_HIGH_WATER = "Custom/Nodes/high_water/SendMon";
    public static final String FREE_HEAP = "Custom/Nodes/free_heap";
    public static final String AVG_BEACON_SIGNAL_STRENGTH = "Custom/Nodes/avg_Beacon_signal_strength";
    public static final String MIN_SLEEP_MODE = "Custom/Nodes/min_sleep_mode";
    public static final String NUM_GATEWAYS_TRACKED = "Custom/Nodes/num_gateways_tracked";
    public static final String FAILED_TRANSMISSIONS = "Custom/Nodes/failed_transmissions";
    public static final String MIN_SYNC_DRIFT = "Custom/Nodes/min_sync_drift";
    public static final String MAX_SYNC_DRIFT = "Custom/Nodes/max_sync_drift";
    public static final String SUCCESSFUL_JOIN_ATTEMPTS = "Custom/Nodes/successful_join_attempts";
    public static final String FAILED_JOIN_ATTEMPTS = "Custom/Nodes/failed_join_attempts";
    public static final String BATTERY_LEVEL = "Custom/Nodes/battery_level";
    public static final String DAIKIN_MESSAGES_FROM_SLAVE = "Custom/Nodes/daikin/messages_from_slave";
    public static final String DAIKIN_INVALID_MESSAGES_FROM_SLAVE = "Custom/Nodes/daikin/invalid_messages_from_slave";
    public static final String DAIKIN_SLAVE_RESET_COUNT = "Custom/Nodes/daikin/slave_reset_count";
    public static final String DAIKIN_SLAVE_RESET_CAUSE = "Custom/Nodes/daikin/slave_reset_cause";
    public static final String DAIKIN_SLAVE_CRC_FAIL_COUNT = "Custom/Nodes/daikin/crc_fail_count";
    public static final String DAIKIN_INVALID_LENGTH_COUNT = "Custom/Nodes/daikin/invalid_length_count";
    public static final String DAIKIN_INVALID_TYPE_COUNT = "Custom/Nodes/daikin/invalid_type_count";
    public static final String CURRENT_REQUEST = "Custom/Gateways/currently_executing_requests";

    private MetricReporter(){}

    private static SensorflowConfig sensorflowConfig;

    private static OkHttpClient httpClient;

    @SafeVarargs
    public static void recordMetric(String metricName, float value, Map.Entry<String, Object> ... attributes){
        if(sensorflowConfig != null) {
            Metric m = new Metric(metricName, value, sensorflowConfig.getBalenaDeviceNameAtInit(),
                    sensorflowConfig.getLocationId(),
                    sensorflowConfig.getNewRelicEntityGUID(), attributes);
            String json = m.toJson();
            RequestBody body = RequestBody.create(json,
                    MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("https://metric-api.eu.newrelic.com/metric/v1")
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
    }

    public static void setSensorflowConfig(SensorflowConfig config) {
        sensorflowConfig = config;
        httpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .addHeader("Api-Key", sensorflowConfig.getNewRelicInsertKey())
                        .addHeader("Content-Type", "application/json")
                        .build()))
                .addInterceptor(new GzipRequestInterceptor())
                .callTimeout(5, TimeUnit.SECONDS)
                .build();
    }

}

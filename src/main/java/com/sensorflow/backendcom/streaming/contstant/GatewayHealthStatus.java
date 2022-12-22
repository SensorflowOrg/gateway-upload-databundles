package com.sensorflow.backendcom.streaming.contstant;

import lombok.Getter;

@Getter
public enum GatewayHealthStatus {
    IS_HEALTHY(0), LORA_ERROR(1);

    private final String measurementType = "gateway_status";
    private double value;

    GatewayHealthStatus(double value) {
        this.value = value;
    }
}

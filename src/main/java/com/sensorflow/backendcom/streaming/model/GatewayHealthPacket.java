package com.sensorflow.backendcom.streaming.model;

import com.sensorflow.backendcom.streaming.contstant.GatewayHealthStatus;

import lombok.Getter;

@Getter
public class GatewayHealthPacket implements DataPacket {
    private long time;
    private String measurementType;
    private String balenaId;
    private double value;

    public GatewayHealthPacket(long unixTimestamp, String measurementType, String balenaId, double value) {
        this.time = unixTimestamp / 1000;
        this.measurementType = measurementType;
        this.balenaId = balenaId;
        this.value = value;
    }

    public static GatewayHealthPacket loraError(String balenaId) {
        GatewayHealthStatus gatewayStatus = GatewayHealthStatus.LORA_ERROR;
        return new GatewayHealthPacket(System.currentTimeMillis(), gatewayStatus.getMeasurementType(), balenaId, gatewayStatus.getValue());
    }

    public static GatewayHealthPacket isHealthy(String balenaId) {
        GatewayHealthStatus gatewayStatus = GatewayHealthStatus.IS_HEALTHY;
        return new GatewayHealthPacket(System.currentTimeMillis(), gatewayStatus.getMeasurementType(), balenaId, gatewayStatus.getValue());
    }
}

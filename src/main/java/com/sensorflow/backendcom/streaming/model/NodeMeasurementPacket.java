package com.sensorflow.backendcom.streaming.model;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("squid:S1068")
@Getter
public class NodeMeasurementPacket implements DataPacket {
    private long unixTimestamp;
    @Setter
    private String positionId;
    private String srcMacId;
    private int streamIndex;
    private String measurementType;
    private double measurementValue;

    public NodeMeasurementPacket(
            long unixTimestamp,
            String positionId,
            String srcMacId,
            int streamIndex,
            String measurementType,
            double measurementValue) {
        this.unixTimestamp = unixTimestamp;
        this.positionId = positionId;
        this.srcMacId = srcMacId;
        this.streamIndex = streamIndex;
        this.measurementType = measurementType;
        this.measurementValue = measurementValue;
    }
}

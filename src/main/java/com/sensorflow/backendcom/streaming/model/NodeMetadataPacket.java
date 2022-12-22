package com.sensorflow.backendcom.streaming.model;

import lombok.Getter;

@SuppressWarnings("squid:S1068")
@Getter
public class NodeMetadataPacket implements DataPacket {
    private long unixTimestamp;
    private String nodeMacId;
    private String measurementType;
    private double measurementValue;

    public NodeMetadataPacket(long unixTimestamp,
                              String nodeMacId,
                              String measurementType,
                              double measurementValue) {
        this.unixTimestamp = unixTimestamp/1000;//Backend requires Second accuracy
        this.nodeMacId = nodeMacId;
        this.measurementType = measurementType;
        this.measurementValue = measurementValue;
    }

    public String getNodeMacId() {
        return nodeMacId;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public double getMeasurementValue() {
        return measurementValue;
    }
}

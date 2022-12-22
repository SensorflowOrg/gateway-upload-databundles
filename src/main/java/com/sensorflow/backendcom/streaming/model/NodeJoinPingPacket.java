package com.sensorflow.backendcom.streaming.model;

import lombok.Getter;

@Getter
public class NodeJoinPingPacket implements DataPacket{
    private long time;
    private String balenaId;
    private String nodeMacId;
    private double rssi;
    private double snr;

    public NodeJoinPingPacket(long unixTimestamp, String balenaId, String nodeMacId, double rssi, double snr) {
        this.time = unixTimestamp/1000;
        this.balenaId = balenaId;
        this.nodeMacId = nodeMacId;
        this.rssi = rssi;
        this.snr = snr;
    }
}

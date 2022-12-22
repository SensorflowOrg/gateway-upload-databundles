package com.sensorflow.backendcom.streaming.model;

import lombok.Getter;

@Getter
public class GatewayPacket implements DataPacket {
    private String balenaId;
    private String balenaDeviceName;
    private int gatewayMac;
    private String positionId;
    private String networkConnectionStatus;
    private String wifiMacAddress;
    private String ethernetMAcAddress;
    private String version;

    public GatewayPacket(
            String balenaId,
            String balenaDeviceName,
            int gatewayMac,
            String positionId,
            String networkConnectionStatus,
            String wifiMacAddress,
            String ethernetMAcAddress,
            String version) {
        this.balenaId = balenaId;
        this.balenaDeviceName = balenaDeviceName;
        this.gatewayMac = gatewayMac;
        this.positionId = positionId;
        this.networkConnectionStatus = networkConnectionStatus;
        this.wifiMacAddress = wifiMacAddress;
        this.ethernetMAcAddress = ethernetMAcAddress;
        this.version = version;
    }
}

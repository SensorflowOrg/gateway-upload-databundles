package com.sensorflow.backendcom.streaming.model;

import lombok.Getter;

@Getter
public class NodePacket implements DataPacket{
    private String nodeMacId;
    private String nodeType;
    private String associatedGateway;
    private String nodeSubType;
    private int firmwareVersionNumber;


    public NodePacket(String nodeMacId, String nodeType, String associatedGateway, String nodeSubType, int firmware) {
        this.nodeMacId = nodeMacId;
        this.nodeType = nodeType;
        this.associatedGateway = associatedGateway;
        this.nodeSubType = nodeSubType;
        firmwareVersionNumber = firmware;
    }
}

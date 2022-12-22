package com.sensorflow.backendcom.streaming.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.sensorflow.airlink.packet.RxSystemStatesPacket;

import lombok.Getter;

@Getter
public class NodeSystemStatesPacket implements DataPacket{

    private LocalDateTime timestamp;
    private String nodeMacId;
    private long mcuAsleepS;
    private long mcuAwakeS;
    private long radioListenS;
    private long radioSendingS;
    private long extra1S;
    private long extra2S;

    public NodeSystemStatesPacket(String nodeMacId, RxSystemStatesPacket rxSystemStatesPacket) {
        this.nodeMacId = nodeMacId;
        timestamp = LocalDateTime.ofEpochSecond(rxSystemStatesPacket.getTimestampS(), 0, ZoneOffset.UTC);
        mcuAsleepS = rxSystemStatesPacket.getMcuAsleepS();
        mcuAwakeS = rxSystemStatesPacket.getMcuAwakeS();
        radioListenS = rxSystemStatesPacket.getRadioListenS();
        radioSendingS = rxSystemStatesPacket.getRadioSendingS();
        extra1S = rxSystemStatesPacket.getExtra1S();
        extra2S = rxSystemStatesPacket.getExtra2S();
    }
}

package com.sensorflow.backendcom.streaming.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.sensorflow.airlink.packet.RxDiagnosticPacket;

import lombok.Getter;

@Getter
public class NodeDiagnosticPacket implements DataPacket {

    LocalDateTime time;
    String nodeMacId;
    int flashEraseCount;
    int canceltHw;
    int joinTHw;
    int syncTHw;
    int listenTHw;
    int contListTHw;
    int otaPrepTHw;
    int startupHw;
    int amTxTHw;
    int amRecvTHw;
    int procSetTHw;
    int timerTaskHw;
    int sendMonHw;
    int deviceSpecific1Hw;
    int deviceSpecific2Hw;
    int deviceSpecific3Hw;
    int deviceSpecific4Hw;
    int deviceSpecific5Hw;
    int deviceSpecific6Hw;
    int freeHeap;
    int avgBeaconSignalStrength;
    int minSleepMode;
    int numGatewaysTracked;
    int failedTransmissions;
    int minSyncDriftMs;
    int maxSyncDriftMs;
    int successfulJoinAttempts;
    int failedJoinAttempts;

    public NodeDiagnosticPacket(RxDiagnosticPacket rxDiagnosticPacket) {
        time = LocalDateTime.ofEpochSecond(rxDiagnosticPacket.getTimestampS(), 0, ZoneOffset.UTC);
        nodeMacId = rxDiagnosticPacket.getNodeMacId();
        flashEraseCount = rxDiagnosticPacket.getNvmFlashEraseCount();
        canceltHw = rxDiagnosticPacket.getHighWaterMarks()[0];
        joinTHw = rxDiagnosticPacket.getHighWaterMarks()[1];
        syncTHw = rxDiagnosticPacket.getHighWaterMarks()[2];
        listenTHw = rxDiagnosticPacket.getHighWaterMarks()[3];
        contListTHw = rxDiagnosticPacket.getHighWaterMarks()[4];
        otaPrepTHw = rxDiagnosticPacket.getHighWaterMarks()[5];
        startupHw = rxDiagnosticPacket.getHighWaterMarks()[6];
        amTxTHw = rxDiagnosticPacket.getHighWaterMarks()[7];
        amRecvTHw = rxDiagnosticPacket.getHighWaterMarks()[8];
        procSetTHw = rxDiagnosticPacket.getHighWaterMarks()[9];
        timerTaskHw = rxDiagnosticPacket.getHighWaterMarks()[10];
        sendMonHw = rxDiagnosticPacket.getHighWaterMarks()[11];
//        reservedHw = rxDiagnosticPacket.getHighWaterMarks()[12]; //skipped as not holding data yet
        deviceSpecific1Hw = rxDiagnosticPacket.getHighWaterMarks()[13];
        deviceSpecific2Hw = rxDiagnosticPacket.getHighWaterMarks()[14];
        deviceSpecific3Hw = rxDiagnosticPacket.getHighWaterMarks()[15];
        deviceSpecific4Hw = rxDiagnosticPacket.getHighWaterMarks()[16];
        deviceSpecific5Hw = rxDiagnosticPacket.getHighWaterMarks()[17];
        deviceSpecific6Hw = rxDiagnosticPacket.getHighWaterMarks()[18];
        freeHeap = rxDiagnosticPacket.getFreeHeap();
        avgBeaconSignalStrength = rxDiagnosticPacket.getAvgBeaconSignal();
        minSleepMode = rxDiagnosticPacket.getMinSleepMode();
        numGatewaysTracked = rxDiagnosticPacket.getNumGatewaysSeen();
        failedTransmissions = rxDiagnosticPacket.getNumFailedTransmissions();
        minSyncDriftMs = rxDiagnosticPacket.getMinSyncDrift();
        maxSyncDriftMs = rxDiagnosticPacket.getMaxSyncDrift();
        successfulJoinAttempts = rxDiagnosticPacket.getSucessfullJoinAttempts();
        failedJoinAttempts = rxDiagnosticPacket.getFailedJoinAttempts();
    }
}

package com.sensorflow.airlink.packet;

import java.util.Arrays;

import com.sensorflow.airlink.util.AirlinkUtil;

import lombok.Getter;

@Getter
public class RxDiagnosticPacket {

    private static final int HIGH_WATER_MARKS_LENGTH = 20;
    private static final int NVM_ERASE_COUNT_INDEX = 0;
    private static final int HIGH_WATER_MARKS_INDEX = NVM_ERASE_COUNT_INDEX + 2;
    private static final int FREE_HEAP_INDEX = HIGH_WATER_MARKS_INDEX + HIGH_WATER_MARKS_LENGTH;
    private static final int BEACON_STRENGTH_INDEX = FREE_HEAP_INDEX + 2;
    private static final int SLEEP_NUM_GW_INDEX = BEACON_STRENGTH_INDEX + 1;
    private static final int FAILED_TRANSMISSIONS_INDEX = SLEEP_NUM_GW_INDEX + 1;
    private static final int MIN_SYNC_DRIFT_INDEX = FAILED_TRANSMISSIONS_INDEX + 1;
    private static final int MAX_SYNC_DRIFT_INDEX = MIN_SYNC_DRIFT_INDEX + 1;
    private static final int SUCCESSFUL_JOIN_ATTEMPTS_INDEX = MAX_SYNC_DRIFT_INDEX + 1;
    private static final int FAILED_JOIN_ATTEMPTS_INDEX = SUCCESSFUL_JOIN_ATTEMPTS_INDEX + 1;


    public static final int RX_DIAGNOSTIC_PACKET_TYPE = 15;

    private String nodeMacId;
    private int nvmFlashEraseCount;
    private int[] highWaterMarks = new int[HIGH_WATER_MARKS_LENGTH];
    private int freeHeap;
    private int avgBeaconSignal;
    private int minSleepMode;
    private int numGatewaysSeen;
    private int numFailedTransmissions;
    private int minSyncDrift;
    private int maxSyncDrift;
    private int sucessfullJoinAttempts;
    private int failedJoinAttempts;
    private long timestampS;

    public RxDiagnosticPacket(String nodeMacId, byte[] payload) {
        this.nodeMacId = nodeMacId;
        timestampS = System.currentTimeMillis()/1000;
        nvmFlashEraseCount = AirlinkUtil.readTwoBytes(payload, NVM_ERASE_COUNT_INDEX);
        for (int i = 0; i < HIGH_WATER_MARKS_LENGTH; i++) {
            highWaterMarks[i] = (payload[HIGH_WATER_MARKS_INDEX + i] & 0xFF);
        }
        freeHeap = AirlinkUtil.readTwoBytes(payload, FREE_HEAP_INDEX);
        avgBeaconSignal = (payload[BEACON_STRENGTH_INDEX] & 0xFF);
        minSleepMode = (payload[SLEEP_NUM_GW_INDEX] & 0xC0) >> 6;
        numGatewaysSeen = (payload[SLEEP_NUM_GW_INDEX] & 0x3F) ;
        numFailedTransmissions = (payload[FAILED_TRANSMISSIONS_INDEX] & 0xFF);
        minSyncDrift = (payload[MIN_SYNC_DRIFT_INDEX] & 0xFF) - 127;
        maxSyncDrift = (payload[MAX_SYNC_DRIFT_INDEX] & 0xFF) - 127;
        sucessfullJoinAttempts = (payload[SUCCESSFUL_JOIN_ATTEMPTS_INDEX] & 0xFF);
        failedJoinAttempts = (payload[FAILED_JOIN_ATTEMPTS_INDEX] & 0xFF);
    }

    @Override
    public String toString() {
        return "RxDiagnosticPacket{" +
                ", timeStampS=" + timestampS +
                ", nvmFlashEraseCount=" + nvmFlashEraseCount +
                ", highWaterMarks=" + Arrays.toString(highWaterMarks) +
                ", freeHeap=" + freeHeap +
                ", avgBeaconSignal=" + avgBeaconSignal +
                ", minSleepMode=" + minSleepMode +
                ", numGatewaysSeen=" + numGatewaysSeen +
                ", numFailedTransmissions=" + numFailedTransmissions +
                ", minSyncDrift=" + minSyncDrift +
                ", maxSyncDrift=" + maxSyncDrift +
                ", sucessfullJoinAttempts=" + sucessfullJoinAttempts +
                ", failedJoinAttempts=" + failedJoinAttempts +
                '}';
    }
}

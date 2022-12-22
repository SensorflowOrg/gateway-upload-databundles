package com.sensorflow.airlink.packet;

import com.sensorflow.airlink.util.AirlinkUtil;

import lombok.Getter;

@Getter
public class RxSystemStatesPacket {
    public static final int RX_SYSTEM_STATES_MSG_TYPE = 14;
    private static final int TIMESTAMP_INDEX = 0;
    private static final int MCU_ASLEEP_SECONDS_INDEX = TIMESTAMP_INDEX + 4;
    private static final int MCU_AWAKE_SECONDS_INDEX = MCU_ASLEEP_SECONDS_INDEX + 4;
    private static final int RADIO_LISTEN_SECONDS_INDEX = MCU_AWAKE_SECONDS_INDEX + 4;
    private static final int RADIO_SENDING_SECONDS_INDEX = RADIO_LISTEN_SECONDS_INDEX + 4;
    private static final int EXTRA1_SECONDS_INDEX = RADIO_SENDING_SECONDS_INDEX + 4;
    private static final int EXTRA2_SECONDS_INDEX = EXTRA1_SECONDS_INDEX + 4;

    private long timestampS;
    private long mcuAsleepS;
    private long mcuAwakeS;
    private long radioListenS;
    private long radioSendingS;
    private long extra1S;
    private long extra2S;

    public RxSystemStatesPacket(byte[] payload) {
        timestampS = AirlinkUtil.readFourBytes(payload, TIMESTAMP_INDEX);
        mcuAsleepS = AirlinkUtil.readFourBytes(payload, MCU_ASLEEP_SECONDS_INDEX);
        mcuAwakeS = AirlinkUtil.readFourBytes(payload, MCU_AWAKE_SECONDS_INDEX);
        radioListenS = AirlinkUtil.readFourBytes(payload, RADIO_LISTEN_SECONDS_INDEX);
        radioSendingS = AirlinkUtil.readFourBytes(payload, RADIO_SENDING_SECONDS_INDEX);
        extra1S = AirlinkUtil.readFourBytes(payload, EXTRA1_SECONDS_INDEX);
        extra2S = AirlinkUtil.readFourBytes(payload, EXTRA2_SECONDS_INDEX);
    }

    @Override
    public String toString() {
        return "RxSystemStates{" +
                "timestampS=" + timestampS +
                ", mcuAsleepS=" + mcuAsleepS +
                ", mcuAwakeS=" + mcuAwakeS +
                ", radioListenS=" + radioListenS +
                ", radioSendingS=" + radioSendingS +
                ", extra1S=" + extra1S +
                ", extra2S=" + extra2S +
                '}';
    }
}

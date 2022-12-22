package com.sensorflow.airlink.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class AirlinkUtil {
	
	public static int readTwoBytes (byte[] payload, int offset) {
        return ((payload[offset] & 0xFF) << 8) | (payload[offset + 1] & 0xFF);
    }
    public static long readFourBytes (byte[] payload, int offset) {
        //top byte must be cast to long, otherwise it'll underflow as an int
        return ((((long)payload[offset]) & 0xFF) << 24) | ((payload[offset + 1] & 0xFF) << 16 )  | ((payload[offset + 2] & 0xFF)<< 8) | (payload[offset +3] & 0xFF);
    }

    public static long readFiveBytes (byte[] payload, int offset) {
        //top byte must be cast to long, otherwise it'll underflow as an int
        return ((((long)payload[offset]) & 0xFF) << 32) | ((((long)payload[offset + 1]) & 0xFF) << 24) | ((payload[offset + 2] & 0xFF) << 16 )  | ((payload[offset + 3] & 0xFF)<< 8) | (payload[offset + 4] & 0xFF);
    }

    public static long readEightBytes (byte[] payload, int offset) {
        //top byte must be cast to long, otherwise it'll underflow as an int
        return ByteBuffer.wrap(Arrays.copyOfRange(payload, offset, offset+8)).getLong();
    }
    
	public static String getMacHexString(long mac){
		return Long.toHexString(mac).toUpperCase();
	}

    private AirlinkUtil() {
    }
}

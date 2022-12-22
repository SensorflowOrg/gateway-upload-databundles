package com.sensorflow.backendcom.newrelic.model;

import java.util.Map;

public class SerializationUtil {

    private SerializationUtil(){}

    static void serializeAttributes(StringBuilder builder, Map<String, Object> attributes) {
        String delim = "";
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            builder.append(delim);
            delim = ",";
            if (value instanceof String) {
                builder.append("\"").append(key).append("\":\"").append(value).append("\"");
            } else {
                builder.append("\"").append(key).append("\":").append(value);
            }
        }
    }
}

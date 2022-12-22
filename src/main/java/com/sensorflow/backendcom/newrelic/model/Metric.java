package com.sensorflow.backendcom.newrelic.model;

import java.util.HashMap;
import java.util.Map;

public class Metric {
    String name;
    float value;
    long timestamp;
    Map<String, Object> metricAttributes;

    @SafeVarargs
    public Metric(String name, float value, String host, String locationId, String entityGuid, Map.Entry<String, Object> ... attributes) {
        this.name = name;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
        metricAttributes = new HashMap<>();
        for (Map.Entry<String, Object> attribute : attributes) {
            metricAttributes.put(attribute.getKey(),attribute.getValue());
        }
        metricAttributes.put("host", host);
        metricAttributes.put("locationId", locationId);
        metricAttributes.put("entity.guid", entityGuid);
    }

    public Metric(String name, float value, String host, String locationId, String entityGuid) {
        this.name = name;
        this.value = value;
        timestamp = System.currentTimeMillis();
        metricAttributes = new HashMap<>();
        metricAttributes.put("host", host);
        metricAttributes.put("locationId", locationId);
        metricAttributes.put("entity.guid", entityGuid);
    }

    public String toJson(){
        StringBuilder builder = new StringBuilder();
        builder.append("[{\"metrics\":[{");
        builder.append("\"name\":\"").append(name).append("\",");
        builder.append("\"value\":").append(value).append(",");
        builder.append("\"timestamp\":").append(timestamp).append(",");
        builder.append("\"attributes\":{");
        SerializationUtil.serializeAttributes(builder, metricAttributes);
        builder.append("}}]}]");
        return builder.toString();
    }
}



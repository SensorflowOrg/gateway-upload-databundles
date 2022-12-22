package com.sensorflow.backendcom.newrelic.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Span {
    Map<String, Object> spanAttributes;
    String traceId;
    String id;
    long timestamp;

    @SafeVarargs
    public Span(String traceId, Map.Entry<String, Object> ... attributes) {
        this.traceId = traceId;
        spanAttributes = new HashMap<>();
        timestamp = System.currentTimeMillis();
        for (Map.Entry<String, Object> attribute : attributes) {
            spanAttributes.put(attribute.getKey(),attribute.getValue());
        }
        id = UUID.randomUUID().toString();
    }

    public Span(String traceId, Map<String, Object> params) {
        this.traceId = traceId;
        spanAttributes = new HashMap<>();
        timestamp = System.currentTimeMillis();
        params.forEach((key,value)->spanAttributes.put(key,value));
        id = UUID.randomUUID().toString();
    }

    public String toJson(){
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"trace.id\":\"").append(traceId).append("\",");
        builder.append("\"id\":\"").append(id).append("\",");
        builder.append("\"timestamp\":").append(timestamp).append(",");
        SerializationUtil.serializeAttributes(builder, spanAttributes);
        builder.append("}}");
        return builder.toString();
    }
}

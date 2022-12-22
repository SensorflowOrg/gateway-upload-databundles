package com.sensorflow.backendcom.newrelic.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Trace {
    private final String locationId;
    Map<String, Object> traceAttributes;
    List<Span> spans;
    String traceId;
    String host;
    String serviceName;

    @SafeVarargs
    public Trace(String host, String locationId, Map.Entry<String, Object> ... attributes ) {
        this.locationId = locationId;
        traceId = UUID.randomUUID().toString();
        traceAttributes = new HashMap<>();
        serviceName = "GatewayNodeManager";
        this.host = host;
        spans = new ArrayList<>();
        spans.add(new Span(traceId, attributes));
    }

    public Trace(String balenaDeviceNameAtInit, String locationId, Map<String, Object> params) {
        this.locationId = locationId;
        traceId = UUID.randomUUID().toString();
        traceAttributes = new HashMap<>();
        serviceName = "GatewayNodeManager";
        this.host = balenaDeviceNameAtInit;
        spans = new ArrayList<>();
        spans.add(new Span(traceId, params));
    }

    public String toJson(){
        StringBuilder builder = new StringBuilder();
        builder.append("[{\"common\":{");
        builder.append("\"attributes\":{");
        builder.append("\"service.name\":\"").append(serviceName).append("\",");
        builder.append("\"host\":\"").append(host).append("\",");
        builder.append("\"locationId\":\"").append(locationId).append("\"");
        builder.append("}},");
        builder.append("\"spans\": [");
        String delim = "";
        for (Span span : spans) {
            builder.append(delim);
            delim = ",";
            builder.append(span.toJson());
        }
         builder.append("]}]");
        return builder.toString();
    }
}

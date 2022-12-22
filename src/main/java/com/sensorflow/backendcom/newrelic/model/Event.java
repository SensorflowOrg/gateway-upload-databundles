package com.sensorflow.backendcom.newrelic.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Event {
    String eventType;
    long timestamp;
    String host;
    String locationId;
    private final String entityGuid;

    Map<String, Object> eventAttributes;
    private Throwable exception;

    @SafeVarargs
    public Event(String eventType, String host, String locationId, String entityGuid, Map.Entry<String, Object> ... attributes) {
        this.eventType = eventType;
        this.host = host;
        this.locationId = locationId;
        this.entityGuid = entityGuid;
        this.timestamp = System.currentTimeMillis();
        eventAttributes = new HashMap<>();
        for (Map.Entry<String, Object> attribute : attributes) {
            eventAttributes.put(attribute.getKey(),attribute.getValue());
        }
    }

    public Event(String eventType, String host, String locationId, String entityGuid, Throwable exception, Map<String, Object> params){
        this(eventType,host,locationId,entityGuid);
        this.exception = exception;
        eventAttributes.putAll(params);
    }

    public Event(String eventType, String host, String locationId, String entityGuid, Map<String, Object> params) {
        this(eventType,host,locationId,entityGuid);
        eventAttributes = params;
    }

    public String toJson(){
        StringBuilder builder = new StringBuilder();
        builder.append("[{");
        builder.append("\"eventType\":\"").append(eventType).append("\",");
        builder.append("\"host\":\"").append(host).append("\",");
        builder.append("\"locationId\":\"").append(locationId).append("\",");
        builder.append("\"entityGuid\":\"").append(entityGuid).append("\",");
        builder.append("\"timestamp\":").append(timestamp);

        if(exception != null){
            builder.append(",");
            builder.append("\"error.stack_trace\":\"")
                    .append(serializeStackTrace(exception)
                            .replace("\\t","")
                            .replace("[","")
                            .replace("\",\"",";")
                            .replace("]","")
                            .replace("\\n","")
                            .replace("\\","")
                            .replace("\\b","")
                            .replace("\\f","")
                            .replace("\\r","")
                            .replace("\"",""))
            .append("\"");
        }
        if(!eventAttributes.isEmpty()){
            builder.append(",");
            SerializationUtil.serializeAttributes(builder,eventAttributes);
        }
        builder.append("}]");
        return builder.toString();
    }

    static String serializeStackTrace(Throwable t){
        List<String> stackTrace = new ArrayList<>();
        boolean inner = false;
        while (t != null) {
            if (inner) {
                stackTrace.add(" ");
                stackTrace.add(" caused by " + t.getClass().getName() + ": " + t.getMessage());
            }
            stackTrace.addAll(stackTracesToStrings(t.getStackTrace()));
            t = t.equals(t.getCause()) ? null : t.getCause();
            inner = true;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String sStackTrace = "";
        try {
            JSONArray.writeJSONString(stackTrace, pw);
            sStackTrace = sw.toString();
        } catch (IOException ex) {
//            Error do nothing, this is just monitoring data
            log.error("failed to serialize throwable", ex);
            log.error("throwable that casued the issue:", t);
        }
        return sStackTrace;
    }

    static Collection<String> stackTracesToStrings(StackTraceElement[] stackTraces) {
        if (stackTraces == null || stackTraces.length == 0) {
            return Collections.emptyList();
        }
        List<String> lines = new ArrayList<>(stackTraces.length);
        for (StackTraceElement e : stackTraces) {
            lines.add('\t' + e.toString());
        }

        return lines;
    }
}

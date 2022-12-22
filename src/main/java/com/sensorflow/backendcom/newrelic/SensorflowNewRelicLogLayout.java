package com.sensorflow.backendcom.newrelic;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.newrelic.logging.core.ExceptionUtil;

@Plugin(
        name = "SFNewRelicLayout",
        category = "Core",
        elementType = "layout"
)
public class SensorflowNewRelicLogLayout extends AbstractStringLayout {

    static final String PLUGIN_NAME = "SFNewRelicLayout";
    private final String entityGuid;
    private final String entityName;
    private final String hostname;

    @PluginFactory
    public static com.sensorflow.backendcom.newrelic.SensorflowNewRelicLogLayout factory(
            @PluginAttribute("entityGuid") String entityGuid,
            @PluginAttribute("entityName") String entityName,
            @PluginAttribute("hostname") String hostname
            ) {
        return new com.sensorflow.backendcom.newrelic.SensorflowNewRelicLogLayout(entityGuid,entityName, hostname,
                StandardCharsets.UTF_8);
    }

    private SensorflowNewRelicLogLayout(String entityGuid, String entityName, String hostname, Charset charset) {
        super(charset);
        this.entityGuid = entityGuid;
        this.entityName = entityName;
        this.hostname = hostname;
    }

    public String toSerializable(LogEvent event) {
        StringWriter sw = new StringWriter();

        try {
            JsonGenerator generator = (new JsonFactory()).createGenerator(sw);
            Throwable var4 = null;

            writeToGenerator(event, generator, var4);
        } catch (IOException var16) {
            return var16.toString();
        }

        return sw.toString() + "\n";
    }

    void writeToGenerator(LogEvent event, JsonGenerator generator, Throwable var4) throws IOException {
        try {
            this.writeToGenerator(event, generator);
        } catch (Throwable var14) {
            var4 = var14;
            throw var14;
        } finally {
            if (var4 != null) {
                try {
                    generator.close();
                } catch (Throwable var13) {
                    var4.addSuppressed(var13);
                }
            } else {
                generator.close();
            }
        }
    }

    private void writeToGenerator(LogEvent event, JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeObjectField("message", event.getMessage().getFormattedMessage());
        generator.writeObjectField("timestamp", event.getTimeMillis());
        generator.writeObjectField("thread.name", event.getThreadName());
        generator.writeObjectField("log.level", event.getLevel().toString());
        generator.writeObjectField("logger.name", event.getLoggerName());
        generator.writeObjectField("entity.guid", entityGuid);
        generator.writeObjectField("entity.guids", entityGuid);
        generator.writeObjectField("entity.name", entityName);
        generator.writeObjectField("entity.type", "SERVICE");
        generator.writeObjectField("hostname", hostname);

        if (event.isIncludeLocation() && event.getSource() != null) {
            generator.writeObjectField("class.name", event.getSource().getClassName());
            generator.writeObjectField("method.name", event.getSource().getMethodName());
            generator.writeObjectField("line.number", event.getSource().getLineNumber());
        }

        Map<String, String> map = event.getContextData().toMap();
        if (map != null) {

            for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
                if (( stringStringEntry.getKey()).startsWith("NewRelic:")) {
                    String key = (stringStringEntry.getKey()).substring("NewRelic:".length());
                    generator.writeStringField(key,  stringStringEntry.getValue());
                }
            }
        }

        Throwable throwable = event.getThrown();
        if (throwable != null) {
            generator.writeObjectField("error.class", throwable.getClass().getName());
            generator.writeObjectField("error.message", throwable.getMessage());
            generator.writeObjectField("error.stack", ExceptionUtil.getErrorStack(throwable));
        }

        generator.writeEndObject();
    }

}

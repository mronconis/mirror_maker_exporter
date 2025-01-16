package com.mronconi.mirror.maker.exporter.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import lombok.Getter;
import org.apache.kafka.common.header.Headers;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Getter
@JsonAutoDetect
public class OffsetValue implements Serializable {
    long offset;

    public static class Deserializer<T extends Serializable> implements org.apache.kafka.common.serialization.Deserializer<T> {

        private ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            org.apache.kafka.common.serialization.Deserializer.super.configure(configs, isKey);
        }

        @Override
        public T deserialize(String topic, byte[] objectData) {
            String data = new String(objectData, StandardCharsets.UTF_8);
            Log.tracef("Offset raw value: %s", data);
            try {
                return objectMapper.readerFor(OffsetValue.class).readValue(objectData);
            } catch (IOException e) {
                Log.error("Error on deserialize value: ", e);
                return null;
            }
        }

        @Override
        public T deserialize(String topic, Headers headers, byte[] data) {
            return org.apache.kafka.common.serialization.Deserializer.super.deserialize(topic, headers, data);
        }

        @Override
        public void close() {
            org.apache.kafka.common.serialization.Deserializer.super.close();
        }
    }
}

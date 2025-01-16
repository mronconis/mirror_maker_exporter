package com.mronconi.mirror.maker.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.apache.kafka.common.header.Headers;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Builder
@Getter
public class OffsetKey implements Serializable {
    String connector;
    SourceTopicPartition sourceTopicPartition;

    @Getter
    @JsonAutoDetect
    @EqualsAndHashCode
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class SourceTopicPartition{
        String cluster;
        String topic;
        int partition;
    }

    public static class Deserializer<T extends Serializable> implements org.apache.kafka.common.serialization.Deserializer<T> {

        private ObjectMapper objectMapper = new ObjectMapper();
        private Pattern pattern = Pattern.compile("\\[\\\"([a-zA-Z]+)\\\",(\\{[\\.\\-\\w\\W]+?\\})]");

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            org.apache.kafka.common.serialization.Deserializer.super.configure(configs, isKey);
        }

        @Override
        public T deserialize(String topic, byte[] objectData) {
            String data = new String(objectData, StandardCharsets.UTF_8);
            Log.tracef("Offset raw key: %s", data);
            Matcher matcher = pattern.matcher(data);
            if (matcher.find()) {
                OffsetKey.SourceTopicPartition stp = null;

                try {
                    stp = objectMapper.readerFor(OffsetKey.SourceTopicPartition.class).readValue(matcher.group(2));
                } catch (JsonProcessingException e) {
                    Log.error("Error on deserialize key: ", e);
                }

                return (T) OffsetKey.builder()
                        .connector(matcher.group(1))
                        .sourceTopicPartition(stp)
                        .build();
            }
            return null;
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
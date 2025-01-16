package com.mronconi.mirror.maker.exporter.utils;

import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import io.smallrye.reactive.messaging.kafka.companion.ProducerTask;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public abstract class FixtureUtils {
    final private static String CSV_DELIMITER = "\\|";

    public static ProducerTask fixture(KafkaCompanion companion, String resourcePath) {
        return companion.produceStrings().fromRecords(fixture(resourcePath));
    }

    public static List<ProducerRecord<String, String>> fixture(String resourcePath) {
        return fixture(resourcePath, CSV_DELIMITER);
    }

    public static List<ProducerRecord<String, String>> fixture(String resourcePath, String separetor) {
        try (InputStream resource = getResourceAsStream("./fixture/" + resourcePath);
             InputStreamReader inputStreamReader = new InputStreamReader(resource, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            return bufferedReader.lines()
                    .map(l->l.split(separetor))
                    .map(FixtureUtils::forValues)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Log.error("Error on load fixture", e);
            return null;
        }
    }

    private static ProducerRecord<String, String> forValues(String[] values) {
        if (values.length == 4)
            return new ProducerRecord<>(values[0], Integer.parseInt(values[1]),values[2],values[3]);
        else if (values.length == 3)
            return new ProducerRecord<>(values[0], values[1],values[2]);
        else if (values.length == 2)
            return new ProducerRecord<>(values[0], values[1]);
        else
            throw new IllegalArgumentException("Invalid csv columns length: "+values.length);
    }

    private static InputStream getResourceAsStream(String resourcePath) {
        Set<ClassLoader> classLoadersToSearch = new HashSet();
        classLoadersToSearch.add(Thread.currentThread().getContextClassLoader());
        classLoadersToSearch.add(ClassLoader.getSystemClassLoader());
        classLoadersToSearch.add(KafkaCompanion.class.getClassLoader());
        Iterator it = classLoadersToSearch.iterator();
        while(it.hasNext()) {
            ClassLoader classLoader = (ClassLoader)it.next();
            InputStream stream = classLoader.getResourceAsStream(resourcePath);
            if (stream != null) {
                return stream;
            }

            if (resourcePath.startsWith("/")) {
                stream = classLoader.getResourceAsStream(resourcePath.replaceFirst("/", ""));
                if (stream != null) {
                    return stream;
                }
            }
        }
        throw new IllegalArgumentException("Resource '" + resourcePath + "' not found on classpath.");
    }

}

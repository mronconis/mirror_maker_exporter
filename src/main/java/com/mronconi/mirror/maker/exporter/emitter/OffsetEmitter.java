package com.mronconi.mirror.maker.exporter.emitter;

import com.mronconi.mirror.maker.exporter.model.OffsetKey;
import com.mronconi.mirror.maker.exporter.model.OffsetKey.*;
import com.mronconi.mirror.maker.exporter.model.OffsetValue;
import io.vertx.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.logging.Log;

import java.time.Duration;
import java.util.*;

/**
 *
 */
@Singleton
public class OffsetEmitter implements Emitter {

    @ConfigProperty(name = "offset.storage.topic")
    String topic;

    @ConfigProperty(name = "offset.storage.topic.location")
    String topicLocation;

    @ConfigProperty(name = "offset.connector.name")
    String connectorName;

    @Inject
    Instance<Consumer<OffsetKey, OffsetValue>> consumerInstance;

    @Inject
    EventBus bus;

    /**
     *
     */
    @PostConstruct
    public void init() {
        Log.info("Init offset emitter");
        consumerInstance.get().subscribe(Collections.singletonList(topic));
    }

    /**
     *
     */
    @PreDestroy
    public void destroy() {
        Log.info("Shutdown offset emitter");
        consumerInstance.get().unsubscribe();
        consumerInstance.get().close();
    }

    /**
     * Emit last read offset from source cluster.
     */
    public void emit() {
        ConsumerRecords<OffsetKey, OffsetValue> records = consumerInstance.get().poll(Duration.ofMillis(1000));
        Log.debugf("Polled %s records", records.count());

        Map<TopicPartition, Long> lros = new HashMap<>();

        records.forEach(record -> {
            SourceTopicPartition stp = record.key().getSourceTopicPartition();

            if (hasLastReadOffset(record.key().getConnector(), stp.getTopic())) {
                TopicPartition tp = new TopicPartition(stp.getTopic(), stp.getPartition());
                lros.put(tp, record.value().getOffset());
            }
        });

        if (!lros.isEmpty())
            bus.<Map<TopicPartition, Long>>publish("last-read-offsets", lros);
    }

    /**
     *
     * @param connector
     * @param topic
     * @return
     */
    private boolean hasLastReadOffset(String connector, String topic) {
        return connector.equals(connectorName) && !isTopicExcluded(topic);
    }

    /**
     *
     * @param topic
     * @return
     */
    private boolean isTopicExcluded(String topic ){
        return topic.equals("heartbeats");
    }
}

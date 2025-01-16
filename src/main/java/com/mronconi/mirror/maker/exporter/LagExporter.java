package com.mronconi.mirror.maker.exporter;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.common.TopicPartition;
import io.quarkus.logging.Log;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import com.mronconi.mirror.maker.exporter.config.KafkaConfig.*;

/**
 *
 */
@Singleton
public class LagExporter {

    private Map<TopicPartition, AtomicLong> currentLags = new LinkedHashMap<>();

    @Inject
    Executor executor;

    @Inject
    MeterRegistry registry;

    @Inject
    @Upstream
    AdminClient adminClient;

    /**
     *
     * @param lros
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @ConsumeEvent("last-read-offsets")
    public Uni<Void> consume(Map<TopicPartition,Long> lros) {
        return Uni.createFrom().completionStage(
            CompletableFuture.runAsync(()->{
                try {
                    Map<TopicPartition, Long> lags = calculateLags(lros);
                    createOrUpdateMetrics(lags);
                } catch(Exception e) {
                    Log.error("Error on calculate LAGs: ", e);
                }
            })
        ).emitOn(executor);
    }

    /**
     *
     * @param lros
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Map<TopicPartition, Long> calculateLags(Map<TopicPartition, Long> lros) throws ExecutionException, InterruptedException {
        Map<TopicPartition, Long> leos = getLogEndOffset(lros);
        return leos.entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue() - (lros.get(e.getKey()) + 1)));
    }

    /**
     *
     * @param lros
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Map<TopicPartition, Long> getLogEndOffset(Map<TopicPartition,Long> lros) throws ExecutionException, InterruptedException {
        Map<TopicPartition, OffsetSpec> latestOffsets = lros.entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, e -> OffsetSpec.latest()));
        return adminClient.listOffsets(latestOffsets)
                .all()
                .get()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().offset()));
    }

    /**
     *
     * @param lags
     */
    public void createOrUpdateMetrics(Map<TopicPartition, Long> lags) {
        lags.forEach((tp, lag) -> {
            Log.infof("Topic %s-%s LAG: %s",
                    tp.topic(),
                    tp.partition(),
                    lag);
            if (!this.currentLags.containsKey(tp)) {
                this.currentLags.put(tp, new AtomicLong(lag));
                Tags tags = Tags.of(Metrics.TOPIC_TAG, tp.topic())
                        .and(Metrics.PARTION_TAG, String.valueOf(tp.partition()));
                Gauge.builder(Metrics.MM_LAG_TOPIC_METRIC, this.currentLags.get(tp), Number::doubleValue)
                        .tags(tags)
                        .register(registry);
            } else {
                this.currentLags.get(tp).set(lag);
            }
        });
    }

    public Map<TopicPartition, AtomicLong> getCurrentLags() {
        return currentLags;
    }
}

package com.mronconi.mirror.maker.exporter;

import com.google.common.collect.ImmutableMap;
import io.quarkus.logging.Log;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import io.strimzi.test.container.StrimziKafkaContainer;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class StrimziKafkaContainerSupport implements QuarkusTestResourceLifecycleManager {

    protected StrimziKafkaContainer kafkaContainer;
    protected KafkaCompanion kafkaCompanion;

    @Override
    public Map<String, String> start() {
        Log.info("Start TEST");

        if (kafkaContainer == null) {
            kafkaContainer = new io.strimzi.test.container.StrimziKafkaContainer()
                    .waitForRunning();
            kafkaContainer.start();
        }

        if(kafkaCompanion == null) {
            kafkaCompanion = new KafkaCompanion(kafkaContainer.getBootstrapServers());
        }

        try {
            List<NewTopic> topics = Arrays.asList(
                    new NewTopic("foo", 5, (short) 1),
                    new NewTopic("bar", 5, (short) 1),
                    new NewTopic("offset-topic-mm2", 1, (short) 1));

            kafkaCompanion.getOrCreateAdminClient().createTopics(topics).all().get();
        } catch(ExecutionException|InterruptedException e) {
            Log.error("Error on create kafka resources", e);
        }

        return ImmutableMap.of("kafka.bootstrap.servers", kafkaContainer.getBootstrapServers());
    }

    @Override
    public void stop() {
        Log.info("Stop TEST");
        kafkaCompanion.close();
        kafkaContainer.stop();
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(kafkaCompanion, new TestInjector.AnnotatedAndMatchesType(InjectKafkaCompanion.class, KafkaCompanion.class));
    }
}

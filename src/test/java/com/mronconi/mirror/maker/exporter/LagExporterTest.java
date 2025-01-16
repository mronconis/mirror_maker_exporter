package com.mronconi.mirror.maker.exporter;

import com.mronconi.mirror.maker.exporter.emitter.OffsetEmitter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import io.vertx.core.Vertx;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.*;

import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.time.Duration;
import java.util.stream.IntStream;

import static com.mronconi.mirror.maker.exporter.utils.FixtureUtils.*;
import static java.lang.String.*;
import static com.mronconi.mirror.maker.exporter.emitter.PeriodicEmitterVerticle.of;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(value = StrimziKafkaContainerSupport.class, restrictToAnnotatedClass = true)
public class LagExporterTest {

    @Inject
    LagExporter exporter;

    @Inject
    MeterRegistry registry;

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @BeforeAll
    public static void setup() {
        CDI<Object> ctx = CDI.current();
        Vertx.vertx().deployVerticle(
                of(ctx.select(OffsetEmitter.class).get(), Duration.ofSeconds(5))
        );
    }

    @AfterAll
    public static void cleanup() {
        Vertx.vertx().close();
    }

    @Test
    @Order(0)
    void testWithLag() throws InterruptedException, IOException {
        fixture(companion,"topic-test-dataset-1.csv")
                .awaitCompletion();

        fixture(companion,"topic-mm2-offset-dataset-1.csv")
                .awaitCompletion();

        Thread.sleep(Duration.ofSeconds(10).toMillis());

        IntStream.range(0,4).forEach(idx -> {
            Assertions.assertEquals(1, exporter.getCurrentLags().get(new TopicPartition("foo",idx)).get(),
                    format("There shouldn't be lag for a topic %s-%s between upstream and downstream cluster.", "foo", idx));
        });
    }

    @Test
    @Order(1)
    void testWithoutLag() throws InterruptedException, IOException {
        fixture(companion,"topic-mm2-offset-dataset-2.csv")
                .awaitCompletion();

        Thread.sleep(Duration.ofSeconds(10).toMillis());

        IntStream.range(0,4).forEach(idx -> {
            Assertions.assertEquals(0, exporter.getCurrentLags().get(new TopicPartition("foo",idx)).get(),
                    format("There shouldn't be lag for a topic %s-%s between upstream and downstream cluster.", "foo", idx));
        });
    }

    @Test
    @Order(2)
    void testNegativeLag() throws InterruptedException, IOException {
        fixture(companion,"topic-mm2-offset-dataset-2.csv")
                .awaitCompletion();

        Thread.sleep(Duration.ofSeconds(10).toMillis());

        IntStream.range(0,4).forEach(idx -> {
            Assertions.assertEquals(0, exporter.getCurrentLags().get(new TopicPartition("foo",idx)).get(),
                    format("There shouldn't be lag for a topic %s-%s between upstream and downstream cluster.", "foo", idx));
        });
    }

    @Test
    @Order(3)
    public void testMicrometerMetrics() {
        Assertions.assertEquals(5, registry.getMeters().size(),
                "There should be a total of five entries, one for each metric.");
    }

    @Test
    @Order(4)
    void testUnknownTopicOrPartitionFailure() throws InterruptedException, IOException {
        fixture(companion,"topic-mm2-offset-dataset-4.csv")
                .awaitCompletion();

        Thread.sleep(Duration.ofSeconds(10).toMillis());

        Assertions.assertNull(exporter.getCurrentLags().get(new TopicPartition("failure",0)),
                format("There should be null, topic %s not exist on upstream.", "failure"));
    }
}

package com.mronconi.mirror.maker.exporter;

import com.mronconi.mirror.maker.exporter.emitter.OffsetEmitter;
import static com.mronconi.mirror.maker.exporter.emitter.PeriodicEmitterVerticle.of;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;

import java.time.Duration;

@QuarkusMain
public class Main {

    public static void main(String... args) {
        Quarkus.run(Application.class,
                (exitCode, exception) -> {
                    exception.printStackTrace();
                },
                args);
    }

    public static class Application implements QuarkusApplication {
        @Inject
        OffsetEmitter emitter;

        @Override
        public int run(String... args) throws Exception {
            Vertx.vertx()
                    .deployVerticle(of(emitter, Duration.ofSeconds(5)));
            Quarkus.waitForExit();
            return 0;
        }
    }
}

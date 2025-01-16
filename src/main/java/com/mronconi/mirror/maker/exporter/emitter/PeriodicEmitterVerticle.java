package com.mronconi.mirror.maker.exporter.emitter;

import io.smallrye.mutiny.vertx.core.AbstractVerticle;

import java.time.Duration;

public class PeriodicEmitterVerticle extends AbstractVerticle {

    private Emitter emitter;
    long delay;

    public PeriodicEmitterVerticle(Emitter emitter, long delay){
        this.emitter = emitter;
        this.delay = delay;
    }

    public static PeriodicEmitterVerticle of(Emitter emitter, Duration delay) {
        return new PeriodicEmitterVerticle(emitter, delay.toMillis());
    }

    @Override
    public void start() {
        vertx.setPeriodic(delay, (r) -> {
            emitter.emit();
        });
    }

    @Override
    public void stop() {
    }
}
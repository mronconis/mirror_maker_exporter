package com.mronconi.mirror.maker.exporter.config;

import com.mronconi.mirror.maker.exporter.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.util.Arrays;

@Startup
@ApplicationScoped
public class MetricConfig {
    @Produces
    @Singleton
    public MeterFilter configurePrometheusRegistries() {
        return MeterFilter.commonTags(Arrays.asList(
                Tag.of(Metrics.APP_TAG, "mirror-maker-exporter")));
    }
}

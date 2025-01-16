package com.mronconi.mirror.maker.exporter.config;

import com.mronconi.mirror.maker.exporter.model.OffsetKey;
import com.mronconi.mirror.maker.exporter.model.OffsetValue;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Qualifier;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Properties;

@Startup
@ApplicationScoped
public class KafkaConfig {

    @ConfigProperty(name = "kafka.upstream.bootstrap.servers")
    String upstreamBrokers;

    @ConfigProperty(name = "kafka.upstream.security.protocol")
    String upstreamSecurityProtocol;

    @ConfigProperty(name = "kafka.upstream.sasl.mechanism")
    String upstreamSaslMechanism;

    @ConfigProperty(name = "kafka.upstream.sasl.jaas.config")
    String upstreamSaslJaasConfig;

    @ConfigProperty(name = "kafka.upstream.ssl.truststore.type")
    String upstreamSslTruststoreType;

    @ConfigProperty(name = "kafka.upstream.ssl.truststore.location")
    String upstreamSslTruststoreLocation;

    @ConfigProperty(name = "kafka.upstream.ssl.truststore.password")
    String upstreamSslTruststorePassword;

    @ConfigProperty(name = "kafka.upstream.ssl.keystore.type")
    String upstreamSslKeystoreType;

    @ConfigProperty(name = "kafka.upstream.ssl.keystore.location")
    String upstreamSslKeystoreLocation;

    @ConfigProperty(name = "kafka.upstream.ssl.keystore.password")
    String upstreamSslKeystorePassword;

    @ConfigProperty(name = "kafka.upstream.ssl.key.password")
    String upstreamSslKeyPassword;

    @ConfigProperty(name = "kafka.upstream.ssl.endpoint.identification.algorithm")
    String upstreamSslEndpointIdentificationAlgorithm;

    @ConfigProperty(name = "kafka.downstream.bootstrap.servers")
    String downstreamBrokers;

    @ConfigProperty(name = "kafka.downstream.security.protocol")
    String downstreamSecurityProtocol;

    @ConfigProperty(name = "kafka.downstream.sasl.mechanism")
    String downstreamSaslMechanism;

    @ConfigProperty(name = "kafka.downstream.sasl.jaas.config")
    String downstreamSaslJaasConfig;

    @ConfigProperty(name = "kafka.downstream.ssl.truststore.type")
    String downstreamSslTruststoreType;

    @ConfigProperty(name = "kafka.downstream.ssl.truststore.location")
    String downstreamSslTruststoreLocation;

    @ConfigProperty(name = "kafka.downstream.ssl.truststore.password")
    String downstreamSslTruststorePassword;

    @ConfigProperty(name = "kafka.downstream.ssl.keystore.type")
    String downstreamSslKeystoreType;

    @ConfigProperty(name = "kafka.downstream.ssl.keystore.location")
    String downstreamSslKeystoreLocation;

    @ConfigProperty(name = "kafka.downstream.ssl.keystore.password")
    String downstreamSslKeystorePassword;

    @ConfigProperty(name = "kafka.downstream.ssl.key.password")
    String downstreamSslKeyPassword;

    @ConfigProperty(name = "kafka.downstream.ssl.endpoint.identification.algorithm")
    String downstreamSslEndpointIdentificationAlgorithm;

    @ConfigProperty(name = "max.poll.records")
    String consumerMaxPollRecords;

    @ConfigProperty(name = "auto.offset.reset")
    String consumerAutoOffsetReset;

    @ConfigProperty(name = "consumer.groupId")
    String consumerGroupId;

    @ConfigProperty(name = "consumer.clientId")
    String consumerClientId;

    @Produces
    @ApplicationScoped
    @LookupIfProperty(name = "offset.storage.topic.location", stringValue = "upstream")
    public Consumer<OffsetKey, OffsetValue> createUpstreamConsumer() {
        Properties props = new Properties();
        // Common
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, upstreamBrokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerClientId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, OffsetKey.Deserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OffsetValue.Deserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumerMaxPollRecords);
        // Security
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, upstreamSecurityProtocol);
        props.put(SaslConfigs.SASL_MECHANISM, upstreamSaslMechanism);
        props.put(SaslConfigs.SASL_JAAS_CONFIG, upstreamSaslJaasConfig);
        // SSL
        if (upstreamSecurityProtocol != null && upstreamSecurityProtocol.contains("SSL")) {
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, upstreamSslTruststoreLocation);
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, upstreamSslTruststorePassword);
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, upstreamSslTruststoreType);
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, upstreamSslKeystoreLocation);
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, upstreamSslKeystorePassword);
            props.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, upstreamSslKeyPassword);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, upstreamSslKeystoreType);
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, upstreamSslEndpointIdentificationAlgorithm);
        }
        return new KafkaConsumer<>(props);
    }

    @Produces
    @ApplicationScoped
    @LookupIfProperty(name = "offset.storage.topic.location", stringValue = "downstream")
    public Consumer<OffsetKey, OffsetValue> createDownstreamConsumer() {
        Properties props = new Properties();
        // Common
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, downstreamBrokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerClientId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, OffsetKey.Deserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OffsetValue.Deserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumerMaxPollRecords);
        // Security
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, downstreamSecurityProtocol);
        if (downstreamSecurityProtocol != null && downstreamSecurityProtocol.contains("SASL")) {
            props.put(SaslConfigs.SASL_MECHANISM, downstreamSaslMechanism);
            props.put(SaslConfigs.SASL_JAAS_CONFIG, downstreamSaslJaasConfig);
        }
        // TLS
        if (downstreamSecurityProtocol != null && downstreamSecurityProtocol.contains("SSL")) {
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, downstreamSslTruststoreLocation);
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, downstreamSslTruststorePassword);
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, downstreamSslTruststoreType);
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, downstreamSslKeystoreLocation);
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, downstreamSslKeystorePassword);
            props.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, downstreamSslKeyPassword);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, downstreamSslKeystoreType);
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, downstreamSslEndpointIdentificationAlgorithm);
        }
        return new KafkaConsumer<>(props);
    }

    @Upstream
    @Produces
    @ApplicationScoped
    public AdminClient createUpstreamAdminClient() {
        Properties props = new Properties();
        // Common
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, upstreamBrokers);
        // Security
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, upstreamSecurityProtocol);
        if (upstreamSecurityProtocol != null && upstreamSecurityProtocol.contains("SASL")) {
            props.put(SaslConfigs.SASL_MECHANISM, upstreamSaslMechanism);
            props.put(SaslConfigs.SASL_JAAS_CONFIG, upstreamSaslJaasConfig);
        }
        // TLS
        if (upstreamSecurityProtocol != null && upstreamSecurityProtocol.contains("SSL")) {
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, upstreamSslTruststoreLocation);
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, upstreamSslTruststorePassword);
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, upstreamSslTruststoreType);
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, upstreamSslKeystoreLocation);
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, upstreamSslKeystorePassword);
            props.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, upstreamSslKeyPassword);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, upstreamSslKeystoreType);
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, upstreamSslEndpointIdentificationAlgorithm);
        }
        return AdminClient.create(props);
    }

    @Qualifier
    @Retention(RUNTIME)
    @Target({METHOD, FIELD, PARAMETER, TYPE})
    public @interface Upstream {

    }

    @Qualifier
    @Retention(RUNTIME)
    @Target({METHOD, FIELD, PARAMETER, TYPE})
    public @interface Downstream {

    }
}

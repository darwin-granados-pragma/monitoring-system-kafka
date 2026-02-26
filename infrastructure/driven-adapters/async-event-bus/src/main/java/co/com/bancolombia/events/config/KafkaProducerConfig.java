package co.com.bancolombia.events.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

import static co.com.bancolombia.events.constants.KafkaDefaults.PROVIDER;
import static co.com.bancolombia.events.constants.KafkaPropertyKeys.ADAPTERS_MESSAGING_PREFIX;
import static co.com.bancolombia.events.constants.KafkaPropertyKeys.PROVIDER_NAME;

@Configuration
@ConditionalOnProperty(prefix = ADAPTERS_MESSAGING_PREFIX, name = PROVIDER_NAME, havingValue = PROVIDER)
@EnableConfigurationProperties(KafkaProducerSettings.class)
public class KafkaProducerConfig {

    @Bean
    public SenderOptions<String, String> senderOptions(KafkaProducerSettings kafkaProducerSettings) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerSettings.getBootstrapServers());
        properties.put(ProducerConfig.ACKS_CONFIG, kafkaProducerSettings.getProducer().getAcks());
        properties.put(ProducerConfig.RETRIES_CONFIG, kafkaProducerSettings.getProducer().getRetries());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return SenderOptions.create(properties);
    }

    @Bean
    public KafkaSender<String, String> kafkaSender(SenderOptions<String, String> senderOptions) {
        return KafkaSender.create(senderOptions);
    }

}

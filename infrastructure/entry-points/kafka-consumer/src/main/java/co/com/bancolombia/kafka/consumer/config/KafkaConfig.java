package co.com.bancolombia.kafka.consumer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static co.com.bancolombia.kafka.consumer.constants.KafkaConsumerDefaults.PROVIDER;
import static co.com.bancolombia.kafka.consumer.constants.KafkaConsumerPropertyKeys.ADAPTERS_MESSAGING_PREFIX;
import static co.com.bancolombia.kafka.consumer.constants.KafkaConsumerPropertyKeys.PROVIDER_NAME;

@Configuration
@ConditionalOnProperty(prefix = ADAPTERS_MESSAGING_PREFIX, name = PROVIDER_NAME, havingValue = PROVIDER)
@EnableConfigurationProperties(KafkaConsumerSettings.class)
public class KafkaConfig {

    @Bean
    public ReceiverOptions<String, String> kafkaReceiverOptions(KafkaConsumerSettings settings) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, settings.getConsumer().getGroupId());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, settings.getConsumer().getAutoOffsetReset());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return ReceiverOptions.<String, String>create(properties)
                .subscription(List.of(settings.getTopic().getMain()));
    }

    @Bean
    public KafkaReceiver<String, String> kafkaReceiver(
            ReceiverOptions<String, String> kafkaReceiverOptions) {
        return KafkaReceiver.create(kafkaReceiverOptions);
    }
}

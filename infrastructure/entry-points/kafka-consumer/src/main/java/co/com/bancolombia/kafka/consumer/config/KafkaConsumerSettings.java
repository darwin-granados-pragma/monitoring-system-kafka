package co.com.bancolombia.kafka.consumer.config;

import co.com.bancolombia.kafka.consumer.constants.KafkaConsumerDefaults;
import co.com.bancolombia.kafka.consumer.constants.KafkaConsumerPropertyKeys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = KafkaConsumerPropertyKeys.ADAPTERS_KAFKA_PREFIX)
public class KafkaConsumerSettings {
    private String bootstrapServers = KafkaConsumerDefaults.BOOTSTRAP_SERVERS;
    private Topic topic = new Topic();
    private Consumer consumer = new Consumer();

    @Getter
    @Setter
    public static class Topic {
        private String main = KafkaConsumerDefaults.TOPIC_MAIN;
    }

    @Getter
    @Setter
    public static class Consumer {
        private String groupId = KafkaConsumerDefaults.GROUP_ID;
        private String autoOffsetReset = KafkaConsumerDefaults.AUTO_OFFSET_RESET;
    }
}

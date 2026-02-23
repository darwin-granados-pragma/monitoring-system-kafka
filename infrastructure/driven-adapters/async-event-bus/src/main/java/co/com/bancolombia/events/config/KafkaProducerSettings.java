package co.com.bancolombia.events.config;

import co.com.bancolombia.events.constants.KafkaDefaults;
import co.com.bancolombia.events.constants.KafkaPropertyKeys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = KafkaPropertyKeys.ADAPTERS_KAFKA_PREFIX)
public class KafkaProducerSettings {
    private String bootstrapServers = KafkaDefaults.BOOTSTRAP_SERVERS;
    private Topic topic = new Topic();
    private Producer producer = new Producer();

    @Getter
    @Setter
    public static class Topic {
        private String main = KafkaDefaults.MAIN_TOPIC;
        private String dlt = KafkaDefaults.DLT_TOPIC;
    }

    @Getter
    @Setter
    public static class Producer {
        private String acks = KafkaDefaults.PRODUCER_ACKS;
        private int retries = KafkaDefaults.PRODUCER_RETRIES;
    }
}

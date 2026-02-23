package co.com.bancolombia.kafka.consumer.constants;

public final class KafkaConsumerDefaults {
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String GROUP_ID = "monitoring-system-group";
    public static final String TOPIC_MAIN = "monitoring.logs.main";
    public static final String AUTO_OFFSET_RESET = "earliest";

    private KafkaConsumerDefaults() {
    }
}

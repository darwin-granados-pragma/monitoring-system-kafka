package co.com.bancolombia.events.constants;

public final class KafkaDefaults {
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String MAIN_TOPIC = "monitoring.logs.main";
    public static final String DLT_TOPIC = "monitoring.logs.dlt";
    public static final String PRODUCER_ACKS = "all";
    public static final int PRODUCER_RETRIES = 3;

    private KafkaDefaults() {
    }
}

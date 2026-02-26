package co.com.bancolombia.kinesis.consumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapters.kinesis")
public record KinesisConsumerProperties(String region,
                                        String endpointOverride,
                                        String accessKeyId,
                                        String secretAccessKey,
                                        Stream stream,
                                        Consumer consumer) {
    public record Stream(String main, String dlt) {
    }

    public record Consumer(String shardIteratorType, int pollIntervalMs, int maxRecords) {
    }
}

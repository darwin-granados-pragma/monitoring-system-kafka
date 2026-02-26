package co.com.bancolombia.kinesis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapters.kinesis")
public record KinesisProducerProperties(String region,
                                        String endpointOverride,
                                        String accessKeyId,
                                        String secretAccessKey,
                                        Stream stream) {
    public record Stream(String main, String dlt) {
    }
}

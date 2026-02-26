package co.com.bancolombia.kinesis.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClientBuilder;

import java.net.URI;

@Configuration
@ConditionalOnProperty(prefix = "adapters.messaging",
        name = "provider",
        havingValue = "kinesis",
        matchIfMissing = true)
@EnableConfigurationProperties(KinesisProducerProperties.class)
public class KinesisProducerConfig {

    @Bean
    public KinesisAsyncClient kinesisAsyncClient(KinesisProducerProperties properties) {
        KinesisAsyncClientBuilder builder = KinesisAsyncClient.builder().region(Region.of(properties.region()));

        if (properties.endpointOverride() != null && !properties.endpointOverride().isBlank()) {
            builder.endpointOverride(URI.create(properties.endpointOverride()));
            builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                    properties.accessKeyId(),
                    properties.secretAccessKey()
            )));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}

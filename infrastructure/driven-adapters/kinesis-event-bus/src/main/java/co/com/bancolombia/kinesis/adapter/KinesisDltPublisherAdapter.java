package co.com.bancolombia.kinesis.adapter;

import co.com.bancolombia.kinesis.config.KinesisProducerProperties;
import co.com.bancolombia.model.log.MalformedLogEvent;
import co.com.bancolombia.model.log.gateways.DltPublisherGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;

@Component
@Log4j2
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "adapters.messaging",
        name = "provider",
        havingValue = "kinesis",
        matchIfMissing = true)
public class KinesisDltPublisherAdapter implements DltPublisherGateway {
    private final KinesisAsyncClient kinesisAsyncClient;
    private final KinesisProducerProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publishMalformed(MalformedLogEvent malformedLogEvent) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(malformedLogEvent))
                .flatMap(payload -> Mono.fromFuture(kinesisAsyncClient.putRecord(PutRecordRequest.builder()
                        .streamName(properties.stream().dlt())
                        .partitionKey(malformedLogEvent.traceId())
                        .data(SdkBytes.fromUtf8String(payload))
                        .build())))
                .doOnNext(response -> log.warn("Mensaje enviado a DLT en Kinesis. traceId={}, stream={}, sequenceNumber={}",
                        malformedLogEvent.traceId(),
                        properties.stream().dlt(),
                        response.sequenceNumber()))
                .then();
    }
}

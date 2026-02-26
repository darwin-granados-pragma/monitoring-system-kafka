package co.com.bancolombia.kinesis.adapter;

import co.com.bancolombia.kinesis.config.KinesisProducerProperties;
import co.com.bancolombia.model.log.LogEvent;
import co.com.bancolombia.model.log.PublishResult;
import co.com.bancolombia.model.log.gateways.LogProducerGateway;
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
public class KinesisLogProducerAdapter implements LogProducerGateway {
    private static final String PUBLISHED_STATUS = "PUBLISHED";

    private final KinesisAsyncClient kinesisAsyncClient;
    private final KinesisProducerProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<PublishResult> publish(LogEvent event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .flatMap(payload -> Mono.fromFuture(kinesisAsyncClient.putRecord(PutRecordRequest.builder()
                        .streamName(properties.stream().main())
                        .partitionKey(event.eventId())
                        .data(SdkBytes.fromUtf8String(payload))
                        .build())))
                .doOnNext(response -> log.info("Evento enviado a Kinesis. traceId={}, stream={}, sequenceNumber={}",
                        event.traceId(),
                        properties.stream().main(),
                        response.sequenceNumber()))
                .map(response -> PublishResult.builder()
                        .messageId(event.eventId())
                        .status(PUBLISHED_STATUS)
                        .targetTopic(properties.stream().main())
                        .receivedTopic(event.topic())
                        .build());
    }
}

package co.com.bancolombia.kinesis.consumer;

import co.com.bancolombia.kinesis.consumer.config.KinesisConsumerProperties;
import co.com.bancolombia.model.log.LogEvent;
import co.com.bancolombia.usecase.log.ProcessLogUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.Shard;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Log4j2
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "adapters.messaging",
        name = "provider",
        havingValue = "kinesis",
        matchIfMissing = true)
public class KinesisConsumer {
    private static final String UNKNOWN_REASON = "DESERIALIZATION_OR_VALIDATION_ERROR";

    private final KinesisAsyncClient kinesisAsyncClient;
    private final KinesisConsumerProperties properties;
    private final ProcessLogUseCase processLogUseCase;
    private final ObjectMapper objectMapper;
    private final AtomicReference<String> shardIterator = new AtomicReference<>();

    @EventListener(ApplicationStartedEvent.class)
    public void listenMessages() {
        initializeShardIterator()
                .doOnNext(shardIterator::set)
                .doOnNext(iterator -> log.info("Consumer Kinesis configurado. region={}, stream={}, endpointOverride={}",
                        properties.region(),
                        properties.stream().main(),
                        properties.endpointOverride()))
                .thenMany(Flux.interval(Duration.ZERO, Duration.ofMillis(properties.consumer().pollIntervalMs()))
                        .flatMap(ignored -> pollRecords()))
                .doOnError(error -> log.error("Error consumiendo registros de Kinesis - {}", error.getMessage()))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(2)))
                .subscribe();
    }

    private Mono<String> initializeShardIterator() {
        return Mono.fromFuture(kinesisAsyncClient.describeStream(DescribeStreamRequest.builder()
                        .streamName(properties.stream().main())
                        .limit(1)
                        .build()))
                .map(response -> response.streamDescription().shards())
                .flatMap(this::resolveFirstShard)
                .flatMap(shard -> Mono.fromFuture(kinesisAsyncClient.getShardIterator(GetShardIteratorRequest.builder()
                        .streamName(properties.stream().main())
                        .shardId(shard.shardId())
                        .shardIteratorType(properties.consumer().shardIteratorType())
                        .build())))
                .map(response -> {
                    log.info("Consumer Kinesis inicializado. stream={}, shardIteratorType={}",
                            properties.stream().main(),
                            properties.consumer().shardIteratorType());
                    return response.shardIterator();
                });
    }

    private Mono<Shard> resolveFirstShard(List<Shard> shards) {
        if (shards == null || shards.isEmpty()) {
            return Mono.error(new IllegalStateException("No hay shards disponibles en el stream principal"));
        }
        return Mono.just(shards.getFirst());
    }

    private Mono<Void> pollRecords() {
        String iterator = shardIterator.get();
        if (iterator == null || iterator.isBlank()) {
            return Mono.empty();
        }

        return Mono.fromFuture(kinesisAsyncClient.getRecords(GetRecordsRequest.builder()
                        .shardIterator(iterator)
                        .limit(properties.consumer().maxRecords())
                        .build()))
                .doOnNext(response -> shardIterator.set(response.nextShardIterator()))
                .flatMapMany(response -> Flux.fromIterable(response.records()))
                .map(data -> StandardCharsets.UTF_8.decode(data.data().asByteBuffer()).toString())
                .flatMap(payload -> processPayload(payload, properties.stream().main()))
                .then();
    }

    private Mono<Void> processPayload(String payload, String sourceStream) {
        return Mono.fromCallable(() -> objectMapper.readValue(payload, LogEvent.class))
                .flatMap(logEvent -> processLogUseCase.processValid(logEvent, sourceStream))
                .onErrorResume(error -> processLogUseCase.routeMalformed(
                        payload,
                        UNKNOWN_REASON,
                        sourceStream,
                        extractTraceId(payload)
                ));
    }

    private String extractTraceId(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            JsonNode traceId = node.get("traceId");
            if (traceId != null && !traceId.asText().isBlank()) {
                return traceId.asText();
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }
}

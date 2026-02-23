package co.com.bancolombia.kafka.consumer;

import co.com.bancolombia.model.log.LogEvent;
import co.com.bancolombia.usecase.log.ProcessLogUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;

@Component
@Log4j2
@RequiredArgsConstructor
public class KafkaConsumer {
    private static final String UNKNOWN_REASON = "DESERIALIZATION_OR_VALIDATION_ERROR";

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final ProcessLogUseCase processLogUseCase;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationStartedEvent.class)
    public void listenMessages() {
        kafkaReceiver.receiveAutoAck()
                .flatMap(batch -> batch)
                .flatMap(this::processRecord)
                .doOnError(error -> log.error("Error consumiendo registros de Kafka", error))
                .retry()
                .subscribe();
    }

    private Mono<Void> processRecord(ConsumerRecord<String, String> record) {
        String payload = record.value();
        return Mono.fromCallable(() -> objectMapper.readValue(payload, LogEvent.class))
                .flatMap(logEvent -> processLogUseCase.processValid(logEvent, record.topic()))
                .onErrorResume(error -> processLogUseCase.routeMalformed(
                        payload,
                        UNKNOWN_REASON,
                        record.topic(),
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

package co.com.bancolombia.events.adapter;

import co.com.bancolombia.events.config.KafkaProducerSettings;
import co.com.bancolombia.events.constants.KafkaDefaults;
import co.com.bancolombia.events.constants.KafkaPropertyKeys;
import co.com.bancolombia.model.log.LogEvent;
import co.com.bancolombia.model.log.PublishResult;
import co.com.bancolombia.model.log.gateways.LogProducerGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

@Component
@Log4j2
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = KafkaPropertyKeys.ADAPTERS_MESSAGING_PREFIX,
        name = KafkaPropertyKeys.PROVIDER_NAME,
        havingValue = KafkaDefaults.PROVIDER)
public class KafkaLogProducerAdapter implements LogProducerGateway {
    private static final String PUBLISHED_STATUS = "PUBLISHED";

    private final KafkaSender<String, String> kafkaSender;
    private final KafkaProducerSettings kafkaProducerSettings;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<PublishResult> publish(LogEvent event) {
        return serializeEvent(event)
                .flatMap(payload -> kafkaSender.send(Mono.just(SenderRecord.create(
                                new ProducerRecord<>(
                                        kafkaProducerSettings.getTopic().getMain(),
                                        event.eventId(),
                                        payload
                                ),
                                null
                        )))
                        .next())
                .map(senderResult -> toPublishResult(event, senderResult));
    }

    private Mono<String> serializeEvent(LogEvent event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event));
    }

    private PublishResult toPublishResult(LogEvent event, SenderResult<?> senderResult) {
        log.info("Evento enviado a Kafka. traceId={}, topic={}, partition={}, offset={}",
                event.traceId(),
                kafkaProducerSettings.getTopic().getMain(),
                senderResult.recordMetadata().partition(),
                senderResult.recordMetadata().offset());

        return PublishResult.builder()
                .messageId(event.eventId())
                .status(PUBLISHED_STATUS)
                .targetTopic(kafkaProducerSettings.getTopic().getMain())
                .receivedTopic(event.topic())
                .build();
    }
}

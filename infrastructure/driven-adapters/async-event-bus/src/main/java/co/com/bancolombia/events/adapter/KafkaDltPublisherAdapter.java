package co.com.bancolombia.events.adapter;

import co.com.bancolombia.events.config.KafkaProducerSettings;
import co.com.bancolombia.model.log.MalformedLogEvent;
import co.com.bancolombia.model.log.gateways.DltPublisherGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Component
@Log4j2
@RequiredArgsConstructor
public class KafkaDltPublisherAdapter implements DltPublisherGateway {
    private final KafkaSender<String, String> kafkaSender;
    private final KafkaProducerSettings kafkaProducerSettings;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publishMalformed(MalformedLogEvent malformedLogEvent) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(malformedLogEvent))
                .flatMap(payload -> kafkaSender.send(Mono.just(SenderRecord.create(
                                new ProducerRecord<>(
                                        kafkaProducerSettings.getTopic().getDlt(),
                                        malformedLogEvent.traceId(),
                                        payload
                                ),
                                null
                        )))
                        .next())
                .doOnNext(result -> log.warn(
                        "Mensaje enviado a DLT. traceId={}, topic={}, partition={}, offset={}",
                        malformedLogEvent.traceId(),
                        kafkaProducerSettings.getTopic().getDlt(),
                        result.recordMetadata().partition(),
                        result.recordMetadata().offset()
                ))
                .then();
    }
}

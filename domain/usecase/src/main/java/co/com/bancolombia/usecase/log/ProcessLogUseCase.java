package co.com.bancolombia.usecase.log;

import co.com.bancolombia.model.log.LogEvent;
import co.com.bancolombia.model.log.MalformedLogEvent;
import co.com.bancolombia.model.log.gateways.DltPublisherGateway;
import co.com.bancolombia.usecase.exception.BusinessException;
import co.com.bancolombia.usecase.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import static co.com.bancolombia.usecase.common.ValidationConstants.DESCRIPTION_MAX_LENGTH;
import static co.com.bancolombia.usecase.common.ValidationConstants.TOPIC_MAX_LENGTH;
import static co.com.bancolombia.usecase.common.ValidationConstants.UNKNOWN_TRACE_ID;

@RequiredArgsConstructor
public class ProcessLogUseCase {
    private static final Logger LOGGER = Logger.getLogger(ProcessLogUseCase.class.getName());

    private final DltPublisherGateway dltPublisherGateway;

    public Mono<Void> processValid(LogEvent event, String sourceTopic) {
        return Mono.fromRunnable(() -> validateConsumedEvent(event))
                .then(Mono.fromRunnable(() -> LOGGER.log(
                        Level.INFO,
                        "Log consumido correctamente. traceId={0}, sourceTopic={1}, eventId={2}",
                        new Object[]{event.traceId(), sourceTopic, event.eventId()}
                )));
    }

    public Mono<Void> routeMalformed(String payload, String reason, String sourceTopic, String traceId) {
        MalformedLogEvent malformedLogEvent = MalformedLogEvent.builder()
                .originalPayload(payload)
                .errorReason(reason)
                .sourceTopic(sourceTopic)
                .traceId(traceId == null || traceId.isBlank() ? UNKNOWN_TRACE_ID : traceId)
                .failedAt(Instant.now().toString())
                .build();

        return dltPublisherGateway.publishMalformed(malformedLogEvent)
                .doOnSuccess(ignored -> LOGGER.log(
                        Level.WARNING,
                        "Mensaje malformado enviado a DLT. sourceTopic={0}, traceId={1}",
                        new Object[]{sourceTopic, malformedLogEvent.traceId()}
                ))
                .onErrorMap(error -> new BusinessException(
                        ErrorCode.MALFORMED_MESSAGE_TO_DLT,
                        ErrorCode.MALFORMED_MESSAGE_TO_DLT.getDefaultMessage(),
                        error
                ));
    }

    private void validateConsumedEvent(LogEvent event) {
        if (event == null || event.topic() == null || event.topic().isBlank() ||
                event.description() == null || event.description().isBlank() ||
                event.traceId() == null || event.traceId().isBlank() ||
                event.topic().length() > TOPIC_MAX_LENGTH ||
                event.description().length() > DESCRIPTION_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.KAFKA_CONSUMER_ERROR, "Mensaje consumido invalido");
        }
    }
}

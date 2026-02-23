package co.com.bancolombia.usecase.log;

import co.com.bancolombia.model.log.LogEvent;
import co.com.bancolombia.model.log.LogRequest;
import co.com.bancolombia.model.log.PublishResult;
import co.com.bancolombia.model.log.gateways.LogProducerGateway;
import co.com.bancolombia.usecase.exception.BusinessException;
import co.com.bancolombia.usecase.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static co.com.bancolombia.usecase.common.ValidationConstants.DESCRIPTION_MAX_LENGTH;
import static co.com.bancolombia.usecase.common.ValidationConstants.PUBLISHED_STATUS;
import static co.com.bancolombia.usecase.common.ValidationConstants.TOPIC_MAX_LENGTH;

@RequiredArgsConstructor
public class RouteLogUseCase {
    private final LogProducerGateway logProducerGateway;

    public Mono<PublishResult> route(LogRequest request, String traceId) {
        return Mono.fromSupplier(() -> validateAndBuildEvent(request, traceId))
                .flatMap(logProducerGateway::publish)
                .onErrorMap(BusinessException.class, error -> error)
                .onErrorMap(error -> new BusinessException(
                        ErrorCode.KAFKA_PRODUCER_ERROR,
                        ErrorCode.KAFKA_PRODUCER_ERROR.getDefaultMessage(),
                        error
                ));
    }

    private LogEvent validateAndBuildEvent(LogRequest request, String traceId) {
        if (traceId == null || traceId.isBlank()) {
            throw new BusinessException(ErrorCode.MISSING_TRACE_ID, ErrorCode.MISSING_TRACE_ID.getDefaultMessage());
        }

        if (request == null || request.topic() == null || request.topic().isBlank() ||
                request.description() == null || request.description().isBlank() ||
                request.topic().length() > TOPIC_MAX_LENGTH ||
                request.description().length() > DESCRIPTION_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_BODY, ErrorCode.INVALID_REQUEST_BODY.getDefaultMessage());
        }

        return LogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .traceId(traceId)
                .topic(request.topic().trim())
                .description(request.description().trim())
                .createdAt(Instant.now().toString())
                .build();
    }

    public PublishResult successResult(LogEvent event, String targetTopic) {
        return PublishResult.builder()
                .messageId(event.eventId())
                .status(PUBLISHED_STATUS)
                .targetTopic(targetTopic)
                .receivedTopic(event.topic())
                .build();
    }
}

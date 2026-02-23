package co.com.bancolombia.usecase.exception;

import co.com.bancolombia.usecase.common.ApiMessages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST_BODY(400, ApiMessages.INVALID_BODY),
    MISSING_TRACE_ID(400, ApiMessages.TRACE_ID_REQUIRED),
    KAFKA_PRODUCER_ERROR(500, "Error publicando mensaje en Kafka"),
    KAFKA_CONSUMER_ERROR(500, "Error consumiendo mensaje de Kafka"),
    MALFORMED_MESSAGE_TO_DLT(500, "Mensaje malformado enviado a DLT"),
    INTERNAL_SERVER_ERROR(500, ApiMessages.INTERNAL_ERROR);

    private final int httpStatus;
    private final String defaultMessage;
}

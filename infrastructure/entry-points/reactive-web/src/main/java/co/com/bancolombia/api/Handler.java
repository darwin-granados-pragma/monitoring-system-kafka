package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.ApiErrorResponse;
import co.com.bancolombia.api.dto.ApiSuccessResponse;
import co.com.bancolombia.model.log.LogRequest;
import co.com.bancolombia.usecase.common.ApiHeaders;
import co.com.bancolombia.usecase.common.ApiMessages;
import co.com.bancolombia.usecase.exception.BusinessException;
import co.com.bancolombia.usecase.exception.ErrorCode;
import co.com.bancolombia.usecase.log.RouteLogUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {
    private final RouteLogUseCase routeLogUseCase;

    public Mono<ServerResponse> routeLog(ServerRequest serverRequest) {
        String traceId = serverRequest.headers().firstHeader(ApiHeaders.TRACE_ID);

        return serverRequest.bodyToMono(LogRequest.class)
                .switchIfEmpty(Mono.error(new BusinessException(
                        ErrorCode.INVALID_REQUEST_BODY,
                        ErrorCode.INVALID_REQUEST_BODY.getDefaultMessage()
                )))
                .flatMap(request -> routeLogUseCase.route(request, traceId))
                .flatMap(result -> ServerResponse.ok().bodyValue(ApiSuccessResponse.builder()
                        .data(result)
                        .success(true)
                        .message(ApiMessages.EVENT_PUBLISHED)
                        .build()))
                .onErrorResume(BusinessException.class, error -> ServerResponse
                        .status(error.getErrorCode().getHttpStatus())
                        .bodyValue(ApiErrorResponse.builder()
                                .path(serverRequest.path())
                                .errorCode(error.getErrorCode().name())
                                .message(error.getMessage())
                                .build()))
                .onErrorResume(error -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue(ApiErrorResponse.builder()
                                .path(serverRequest.path())
                                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.name())
                                .message(ApiMessages.INTERNAL_ERROR)
                                .build()));
    }
}

package co.com.bancolombia.model.log;

import lombok.Builder;

@Builder
public record MalformedLogEvent(String originalPayload,
                                String errorReason,
                                String sourceTopic,
                                String traceId,
                                String failedAt) {
}

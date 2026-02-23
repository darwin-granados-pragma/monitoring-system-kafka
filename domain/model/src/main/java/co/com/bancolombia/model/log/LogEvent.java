package co.com.bancolombia.model.log;

import lombok.Builder;

@Builder
public record LogEvent(String eventId,
                       String traceId,
                       String topic,
                       String description,
                       String createdAt) {
}

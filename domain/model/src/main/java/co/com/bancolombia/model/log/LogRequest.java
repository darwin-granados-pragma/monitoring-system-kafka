package co.com.bancolombia.model.log;

import lombok.Builder;

@Builder
public record LogRequest(String topic, String description) {
}

package co.com.bancolombia.model.log;

import lombok.Builder;

@Builder
public record PublishResult(String messageId,
                            String status,
                            String targetTopic,
                            String receivedTopic) {
}

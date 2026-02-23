package co.com.bancolombia.model.log.gateways;

import co.com.bancolombia.model.log.MalformedLogEvent;
import reactor.core.publisher.Mono;

public interface DltPublisherGateway {
    Mono<Void> publishMalformed(MalformedLogEvent malformedLogEvent);
}

package co.com.bancolombia.model.log.gateways;

import co.com.bancolombia.model.log.LogEvent;
import co.com.bancolombia.model.log.PublishResult;
import reactor.core.publisher.Mono;

public interface LogProducerGateway {
    Mono<PublishResult> publish(LogEvent event);
}

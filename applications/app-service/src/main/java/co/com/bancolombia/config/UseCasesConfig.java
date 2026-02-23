package co.com.bancolombia.config;

import co.com.bancolombia.model.log.gateways.DltPublisherGateway;
import co.com.bancolombia.model.log.gateways.LogProducerGateway;
import co.com.bancolombia.usecase.log.ProcessLogUseCase;
import co.com.bancolombia.usecase.log.RouteLogUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {
    @Bean
    public RouteLogUseCase routeLogUseCase(LogProducerGateway logProducerGateway) {
        return new RouteLogUseCase(logProducerGateway);
    }

    @Bean
    public ProcessLogUseCase processLogUseCase(DltPublisherGateway dltPublisherGateway) {
        return new ProcessLogUseCase(dltPublisherGateway);
    }
}

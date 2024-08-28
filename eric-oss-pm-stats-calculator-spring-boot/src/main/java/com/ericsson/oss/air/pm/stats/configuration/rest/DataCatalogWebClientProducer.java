/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.configuration.rest;

import javax.enterprise.inject.Produces;

import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataCatalogWebClientProducer {
    private final CalculatorProperties calculatorProperties;

    @Bean
    public DataCatalogWebClient dataCatalogWebClient() {
        return DataCatalogWebClient.of(
                WebClient.builder()
                         .baseUrl(calculatorProperties.getDataCatalogUrl())
                         .filter(logRequest())
                         .filter(logResponse())
                         .build()
        );
    }

    /**
     * Delegator class on {@link WebClient} to help on injection for CDI managed beans.
     * <br>
     * Can avoid using additional {@link Enum}s to mark {@link Produces} objects.
     */
    @RequiredArgsConstructor(staticName = "of")
    public static final class DataCatalogWebClient {
        @Delegate
        private final WebClient webClient;
    }

    private static @NonNull ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    private static @NonNull ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("Response status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}

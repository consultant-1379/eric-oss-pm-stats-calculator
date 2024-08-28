/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.loader;

import java.time.Duration;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.model.dto.catalog.SchemaResponse.MessageBus;
import com.ericsson.oss.air.pm.stats.test.loader.model.CreateSchemaCommand;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataCategory;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataProviderType;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataService;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataServiceInstance;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataSpace;
import com.ericsson.oss.air.pm.stats.test.loader.model.DataObjects.DataType;
import com.ericsson.oss.air.pm.stats.test.loader.model.MessageSchemaResponse;
import com.ericsson.oss.air.pm.stats.test.loader.model.SchemaObjects.MessageDataTopic;
import com.ericsson.oss.air.pm.stats.test.loader.model.SchemaObjects.MessageSchema;
import com.ericsson.oss.air.pm.stats.test.loader.model.SchemaObjects.MessageStatusTopic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataCatalogLoader {
    private static final WebClient WEB_CLIENT = WebClient.create("http://eric-oss-data-catalog:9590/catalog/v1");

    public static Long postMessageBus(final String address) {
        return WEB_CLIENT.post()
                         .uri("/message-bus")
                         .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                         .body(Mono.just(messageBus(address)), MessageBus.class)
                         .retrieve()
                         .bodyToMono(MessageBus.class)
                         .doOnSuccess(response -> log.info("saved message bus: {}", response))
                         .blockOptional(Duration.ofSeconds(30))
                         .map(MessageBus::getId)
                         .orElse(1L);
    }

    public static void postMessageSchema(final String topic, final DataIdentifier dataIdentifier, final String specRef, final Long messageBusID) {
        WEB_CLIENT.put()
                  .uri("/message-schema")
                  .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                  .body(Mono.just(buildCreateSchemaCommand(topic, dataIdentifier, specRef, messageBusID)), CreateSchemaCommand.class)
                  .retrieve()
                  .bodyToMono(MessageSchemaResponse.class)
                  .blockOptional(Duration.ofSeconds(30))
                  .ifPresent(response -> log.info("Data saved to Dc: {}", response));
    }

    private static MessageBus messageBus(final String address) {
        return MessageBus.builder().name("irrelevant").clusterName("irrelevant").nameSpace("irrelevant").accessEndpoints(List.of(address)).build();
    }

    private static CreateSchemaCommand buildCreateSchemaCommand(final String topic, final DataIdentifier dataIdentifier, final String specRef, final Long messageBusID) {
        return new CreateSchemaCommand(
                new DataSpace(dataIdentifier.dataSpace()),
                DataProviderType.build(),
                MessageStatusTopic.build(messageBusID, topic),
                MessageDataTopic.build(messageBusID, topic),
                DataType.build(dataIdentifier.schema()),
                DataCategory.build(dataIdentifier.category()),
                new MessageSchema(specRef),
                DataService.build(),
                DataServiceInstance.build()
        );
    }
}
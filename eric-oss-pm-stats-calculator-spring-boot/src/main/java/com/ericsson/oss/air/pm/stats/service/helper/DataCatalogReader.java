/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.helper;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.configuration.rest.DataCatalogWebClientProducer.DataCatalogWebClient;
import com.ericsson.oss.air.pm.stats.model.dto.catalog.SchemaResponse;
import com.ericsson.oss.air.pm.stats.model.dto.catalog.SchemaResponse.MessageDataTopic;
import com.ericsson.oss.air.pm.stats.model.dto.catalog.SchemaResponse.MessageSchema;
import com.ericsson.oss.air.pm.stats.model.exception.DataCatalogException;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataCatalogReader {
    private static final ListParameterizedTypeReference LIST_PARAMETERIZED_TYPE_REFERENCE = new ListParameterizedTypeReference();

    private final DataCatalogWebClient dataCatalogWebClient;

    public SchemaDetail getDetails(@NonNull final DataIdentifier dataIdentifier) {
        return validated(
                doGetDetails(dataIdentifier)
                        .stream()
                        .map(SchemaResponse::getMessageSchema)
                        .map(messageSchema -> toSchemaDetail(messageSchema, dataIdentifier.schema()))
                        .collect(Collectors.toSet())
        );
    }

    private List<SchemaResponse> doGetDetails(@NonNull final DataIdentifier dataIdentifier) {
        return dataCatalogWebClient.get()
                                   .uri(uriBuilder -> uriBuilder.path("v1/data-type")
                                                                .queryParam("dataCategory", Objects.requireNonNull(dataIdentifier.category(),
                                                                                                                   "category in identifier is null"))
                                                                .queryParam("schemaName", Objects.requireNonNull(dataIdentifier.schema(),
                                                                                                                 "schema in identifier is null"))
                                                                .queryParam("dataSpace", Objects.requireNonNull(dataIdentifier.dataSpace(),
                                                                                                                "dataSpace in identifier is null"))
                                                                .build()
                                   )
                                   .retrieve()
                                   .bodyToMono(LIST_PARAMETERIZED_TYPE_REFERENCE)
                                   .retry(5)
                                   .doOnError(throwable -> log.error("Error happened reaching DataCatalog", throwable))
                                   .blockOptional(Duration.ofSeconds(30))
                                   .orElseThrow(() -> new DataCatalogException("DataCatalog was not reachable"));
    }

    private static SchemaDetail toSchemaDetail(@NonNull final MessageSchema messageSchema, final String schemaName) {
        final MessageDataTopic messageDataTopic = messageSchema.getMessageDataTopic();
        final String topic = messageDataTopic.getName();
        final String namespace = getNamespace(messageSchema.getSpecificationReference(), schemaName);
        return SchemaDetail.builder().withTopic(topic).withNamespace(namespace).build();
    }

    private static SchemaDetail validated(@NonNull final Collection<? extends SchemaDetail> details) {
        /* Mapping to the schema name and data category is expected to give back only a single topic-messageBus combination */
        /* NOTE: the message schema reference can still differ hence the list at the response level */

        final int expected = 1;
        final int actual = details.size();
        Preconditions.checkArgument(actual == expected, String.format(
                "Couldn't find unique schema detail. Expected size: '%s', Actual size: '%s'", expected, actual
        ));

        return IterableUtils.first(details);
    }

    private static class ListParameterizedTypeReference extends ParameterizedTypeReference<List<SchemaResponse>> {
    }

    private static String getNamespace(final String specRef, final String schemaName) {
        /* it is assumed the SpecRef field in the DC will be in a format of: <schemaSubject> or <schemaSubject>/<subjectVersion>
        where the schemaSubject is expected to be like <dataSpace>.<dataProvider>.<dataCategory>
         */
        final String subject = specRef.contains("/") ? specRef.substring(0, specRef.lastIndexOf('/')) : specRef;
        return subject.replace(String.format(".%s", schemaName), "");
    }
}
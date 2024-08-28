/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.facade;

import static lombok.AccessLevel.PUBLIC;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaRegistryUnreachableException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.util.SchemaSubjectHelper;
import com.ericsson.oss.air.pm.stats.service.validator.helper.DefinitionHelper;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import kpi.model.api.table.definition.api.InpDataIdentifierAttribute;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class SchemaRegistryFacade {

    @Inject
    private SchemaRegistryClient schemaRegistryClient;
    @Inject
    private SchemaSubjectHelper schemaSubjectHelper;

    public Set<ParsedSchema> getSchemasForDefinitions(final Collection<? extends KpiDefinitionEntity> definitions) {
        final Set<DataIdentifier> dataIdentifiers = DefinitionHelper.getDistinctDataIdentifiers(definitions);
        final Set<String> subjects = new HashSet<>(dataIdentifiers.size());

        dataIdentifiers.forEach(dataIdentifier -> subjects.add(schemaSubjectHelper.createSubjectRepresentation(dataIdentifier)));

        return subjects.stream()
                .map(this::getLatestSchemaForSubject)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public List<ParsedSchema> getLatestSchemaForSubject(final String subject) {
        try {
            return schemaRegistryClient.getSchemas(subject, false, true);
        } catch (final IOException | RestClientException e) {
            throw new SchemaRegistryUnreachableException("Schema Registry is not reachable", e);
        }
    }

    public List<ParsedSchema> getLatestSchemaForSubject(final InpDataIdentifierAttribute dataIdentifier) {
        return getLatestSchemaForSubject(schemaSubjectHelper.createSubjectRepresentation(dataIdentifier));
    }
}
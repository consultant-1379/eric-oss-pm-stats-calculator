/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static lombok.AccessLevel.PUBLIC;

import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaInvalidException;
import com.ericsson.oss.air.pm.stats.service.facade.SchemaRegistryFacade;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import kpi.model.KpiDefinitionRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class SchemaExistenceValidator {

    @Inject
    private SchemaRegistryFacade schemaRegistryFacade;

    public void validate(final KpiDefinitionRequest definitions) {

        if (definitions.scheduledSimple() == null) {
            return;
        }

        final Set<kpi.model.api.table.definition.KpiDefinition> simpleDefinitions = definitions
                .scheduledSimple().definitions();

        CollectionHelpers.collectDistinctBy(simpleDefinitions, kpi.model.api.table.definition.KpiDefinition::inpDataIdentifier)
                .forEach(dataIdentifier -> {
                    final List<ParsedSchema> schemas = schemaRegistryFacade.getLatestSchemaForSubject(dataIdentifier);

                    if (schemas.isEmpty()) {
                        throw new SchemaInvalidException(String.format("Schema '%s' is not registered in schema registry.", dataIdentifier.schema()));
                    }
                });
    }
}
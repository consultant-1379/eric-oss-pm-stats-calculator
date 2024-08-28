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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.cache.SchemaDetailCache;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.model.exception.DataCatalogException;
import com.ericsson.oss.air.pm.stats.model.exception.DataCatalogUnreachableException;
import com.ericsson.oss.air.pm.stats.service.helper.DataCatalogReader;

import kpi.model.KpiDefinitionTable;
import kpi.model.ScheduledSimple;
import kpi.model.api.table.Table;
import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.api.InpDataIdentifierAttribute;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class DataIdentifierValidator {

    @Inject
    private DataCatalogReader dataCatalogReader;
    @Inject
    private SchemaDetailCache schemaDetailCache;

    public void validateDataIdentifiers(final ScheduledSimple simpleTables) {
        final List<String> missingIdentifiers = new ArrayList<>();

        getDistinctInputDataIdentifiers(simpleTables).forEach(dataIdentifier -> {
            if (schemaDetailCache.hasValue(dataIdentifier)) {
                log.info("Data identifier '{}' has already been validated toward the DataCatalog", dataIdentifier.value());
                return;
            }

            try {
                schemaDetailCache.put(dataIdentifier, dataCatalogReader.getDetails(DataIdentifier.of(dataIdentifier.value())));
            } catch (final DataCatalogException e) {
                throw new DataCatalogUnreachableException("DataCatalog is not reachable, data identifiers cannot be validated", e);
            } catch (final RuntimeException e) {
                log.error("There is no entry available in the DataCatalog for identifier: '{}'", dataIdentifier.value(), e);
                missingIdentifiers.add(String.format("'%s'", dataIdentifier.value()));
            }
        });

        if (!missingIdentifiers.isEmpty()) {
            throw new DataCatalogException(missingIdentifiers);
        }
    }

    private Set<InpDataIdentifierAttribute> getDistinctInputDataIdentifiers(final KpiDefinitionTable<? extends Table> kpiDefinitionTables) {
        return kpiDefinitionTables.definitions().stream().map(KpiDefinition::inpDataIdentifier).collect(Collectors.toSet());
    }
}

/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers.transform;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PUBLIC;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;
import com.ericsson.oss.air.pm.stats.model.exception.TabularParameterValidationException;
import com.ericsson.oss.air.pm.stats.service.api.TabularParameterService;
import com.ericsson.oss.air.pm.stats.service.validator.helper.TabularParameterHelper;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.SetUtils.SetView;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class TabularParameterValidator {
    @Inject
    private TabularParameterHelper tabularParameterHelper;
    @Inject
    private TabularParameterService tabularParameterService;

    /**
     * Verifies if the requested {@link TabularParameters#getName()} do exist in the database.
     *
     * @param tabularParameters {@link Collection} of {@link TabularParameter} to be verified
     */
    public void checkTabularParameterTableExistsInDatabase(final Collection<TabularParameters> tabularParameters) {
        final Set<String> requestTableNames = loadTabularParameterTableNames(tabularParameters);
        final Set<String> databaseTableNames = loadTabularParameterTableNames();

        final SetView<String> difference = SetUtils.difference(requestTableNames, databaseTableNames);

        if (isNotEmpty(difference)) {
            throw new TabularParameterValidationException(String.format(
                    "The following tabular parameters are not present in the KPI definition database: '%s'",
                    difference
            ));
        }
    }

    /**
     * Verifies if the requested calculation do provide the required {@link TabularParameter} for the calculation.
     * <br>
     * For example if the calculation requires <strong>tabular_parameters://tabular_parameter_table</strong>, but the actual data is not provided by the
     * {@link KpiCalculationRequestPayload} then the request should fail.
     *
     * @param kpiCalculationRequestPayload the payload of the request
     */
    public void checkRequiredTabularParameterSourcesPresent(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        final Set<String> kpiNames = kpiCalculationRequestPayload.getKpiNames();
        final List<TabularParameters> tabularParameters = kpiCalculationRequestPayload.getTabularParameters();

        final Set<String> requestTableNames = loadTabularParameterTableNames(tabularParameters);

        final MultiValuedMap<KpiDefinitionEntity, Relation> unresolvedRelations = new HashSetValuedHashMap<>();
        tabularParameterHelper.filterRelations(kpiNames, TABULAR_PARAMETERS).asMap().forEach((kpiDefinitionEntity, tabularParameterRelations) -> {
            for (final Relation tabularParameterRelation : tabularParameterRelations) {
                if (requestTableNames.contains(tabularParameterRelation.tableName())) {
                    continue;
                }

                unresolvedRelations.put(kpiDefinitionEntity, tabularParameterRelation);
            }
        });

        if (!unresolvedRelations.isEmpty()) {
            final List<String> representations = new ArrayList<>();
            unresolvedRelations.asMap().forEach((kpiDefinitionEntity, relations) ->
                    representations.add('[' + kpiDefinitionEntity.name() + ": " + relations + ']')
            );

            throw new TabularParameterValidationException(String.format(
                    "The following tabular parameters are not present for the triggered KPIs: '%s'",
                    representations
            ));
        }
    }

    //TODO: further extract this method
    public void checkColumnsInCaseHeaderPresent(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        final List<TabularParameters> tabularParametersWithHeader = tabularParameterHelper.getTabularParametersWithHeader(kpiCalculationRequestPayload);
        final Map<String, List<String>> tablesAndColumns =
                tabularParametersWithHeader.stream().collect(Collectors.toMap(TabularParameters::getName, tabularParameter -> tabularParameterHelper.splitHeader(tabularParameter)));

        if (tablesAndColumns.isEmpty()) {
            return;
        }

        final List<KpiDefinitionEntity> definitions = tabularParameterHelper.fetchTabularParameterEntities(kpiCalculationRequestPayload);
        final Set<Reference> references = tabularParameterHelper.getReference(definitions);

        final Set<String> incompleteTabularParameter = new HashSet<>();
        final Set<String> tablesFromDefinitionExpressions = references.stream().map(Reference::tableName).collect(toSet());

        for (final String tableName : tablesFromDefinitionExpressions) {
            if (!tablesAndColumns.containsKey(tableName)) {
                continue;
            }
            final Set<String> columns = references.stream()
                    .filter(reference -> reference.tableName().equals(tableName))
                    .map(Reference::columnOrAliasName).collect(toSet());
            tablesAndColumns.get(tableName).forEach(columns::remove);

            columns.forEach(column -> incompleteTabularParameter.add(tableName + " - " + column));
        }

        if (!incompleteTabularParameter.isEmpty()) {

            final String joined = String.join(", ", incompleteTabularParameter);
            throw new TabularParameterValidationException(
                    String.format("Not all of the required columns are present in the header of following tabular parameters: [%s]", joined)
            );
        }
    }

    private Set<String> loadTabularParameterTableNames(final Collection<TabularParameters> tabularParameters) {
        return new HashSet<>(transform(tabularParameters, TabularParameters::getName));
    }

    private Set<String> loadTabularParameterTableNames() {
        return tabularParameterService.findAllTabularParameters().stream().map(TabularParameter::name).collect(toSet());
    }
}
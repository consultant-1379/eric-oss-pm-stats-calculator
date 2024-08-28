/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.adapter;

import static java.util.Map.entry;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;

import kpi.model.KpiDefinitionRequest;
import kpi.model.OnDemand;
import kpi.model.ScheduledComplex;
import kpi.model.ScheduledSimple;
import kpi.model.api.element.ElementBase;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.complex.ComplexTable;
import kpi.model.complex.table.definition.optional.ComplexDefinitionAggregationElements;
import kpi.model.complex.table.definition.optional.ComplexDefinitionDataLookBackLimit;
import kpi.model.complex.table.definition.optional.ComplexDefinitionDataReliabilityOffset;
import kpi.model.complex.table.definition.optional.ComplexDefinitionExportable;
import kpi.model.complex.table.definition.optional.ComplexDefinitionFilters;
import kpi.model.complex.table.definition.optional.ComplexDefinitionReexportLateData;
import kpi.model.complex.table.definition.required.ComplexDefinitionAggregationType;
import kpi.model.complex.table.definition.required.ComplexDefinitionExecutionGroup;
import kpi.model.complex.table.definition.required.ComplexDefinitionExpression;
import kpi.model.complex.table.definition.required.ComplexDefinitionName;
import kpi.model.complex.table.definition.required.ComplexDefinitionObjectType;
import kpi.model.complex.table.optional.ComplexTableAggregationPeriod;
import kpi.model.complex.table.required.ComplexTableAlias;
import kpi.model.ondemand.OnDemandTable;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionAggregationElements;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionExportable;
import kpi.model.ondemand.table.definition.optional.OnDemandDefinitionFilters;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionAggregationType;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionName;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionObjectType;
import kpi.model.ondemand.table.required.OnDemandTableAggregationPeriod;
import kpi.model.ondemand.table.required.OnDemandTableAlias;
import kpi.model.simple.SimpleTable;
import kpi.model.simple.table.definition.optional.SimpleDefinitionAggregationElements;
import kpi.model.simple.table.definition.optional.SimpleDefinitionDataLookBackLimit;
import kpi.model.simple.table.definition.optional.SimpleDefinitionDataReliabilityOffset;
import kpi.model.simple.table.definition.optional.SimpleDefinitionExportable;
import kpi.model.simple.table.definition.optional.SimpleDefinitionFilters;
import kpi.model.simple.table.definition.optional.SimpleDefinitionInpDataIdentifier;
import kpi.model.simple.table.definition.optional.SimpleDefinitionReexportLateData;
import kpi.model.simple.table.definition.required.SimpleDefinitionAggregationType;
import kpi.model.simple.table.definition.required.SimpleDefinitionExpression;
import kpi.model.simple.table.definition.required.SimpleDefinitionName;
import kpi.model.simple.table.definition.required.SimpleDefinitionObjectType;
import kpi.model.simple.table.optional.SimpleTableAggregationPeriod;
import kpi.model.simple.table.required.SimpleTableAlias;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiDefinitionAdapter {
    public static final String DEFINITION_NAME_KEY = "name";
    public static final String DEFINITION_ALIAS_KEY = "alias";
    public static final String DEFINITION_EXPRESSION_KEY = "expression";
    public static final String DEFINITION_EXECUTION_GROUP_KEY = "execution_group";
    public static final String DEFINITION_OBJECT_TYPE_KEY = "object_type";
    public static final String DEFINITION_AGGREGATION_TYPE_KEY = "aggregation_type";
    public static final String DEFINITION_AGGREGATION_PERIOD_KEY = "aggregation_period";
    public static final String DEFINITION_EXPORTABLE_KEY = "exportable";
    public static final String DEFINITION_FILTER_KEY = "filter";
    public static final String DEFINITION_AGGREGATION_ELEMENTS_KEY = "aggregation_elements";
    public static final String DEFINITION_IDENTIFIER_KEY = "inp_data_identifier";
    public static final String DEFINITION_RELIABILITY_OFFSET_KEY = "data_reliability_offset";
    public static final String DEFINITION_LOOKBACK_LIMIT_KEY = "data_lookback_limit";
    public static final String DEFINITION_REEXPORT_LATE_DATA_KEY = "reexport_late_data";

    public static Set<Definition> toOldModel(@NonNull final KpiDefinitionRequest kpiDefinition) {
        final Set<Definition> definitions = new HashSet<>();

        mapOnDemand(definitions, kpiDefinition);
        mapComplex(definitions, kpiDefinition);
        mapSimple(definitions, kpiDefinition);

        return definitions;
    }

    private static void mapOnDemand(final Collection<Definition> definitions, final KpiDefinitionRequest kpiDefinition) {
        final OnDemand onDemand = kpiDefinition.onDemand();

        if (onDemand == null || onDemand.kpiOutputTables() == null || onDemand.kpiOutputTables().isEmpty()) {
            return;
        }

        final List<OnDemandTable> onDemandTables = onDemand.kpiOutputTables();

        for (final OnDemandTable onDemandTable : onDemandTables) {
            /* Attributes provided by table hierarchy */
            final OnDemandTableAggregationPeriod aggregationPeriod = onDemandTable.aggregationPeriod();
            final OnDemandTableAlias alias = onDemandTable.alias();

            for (final OnDemandKpiDefinition definition : onDemandTable.kpiDefinitions()) {
                /* Attributes provided by definition hierarchy */
                final OnDemandDefinitionName name = definition.name();
                final OnDemandDefinitionExpression expression = definition.expression();
                final OnDemandDefinitionObjectType objectType = definition.objectType();
                final OnDemandDefinitionAggregationType aggregationType = definition.aggregationType();
                final OnDemandDefinitionAggregationElements aggregationElements = definition.aggregationElements();
                final OnDemandDefinitionExportable exportable = definition.exportable();
                final OnDemandDefinitionFilters filters = definition.filters();

                definitions.add(new Definition(Map.ofEntries(
                        entry(DEFINITION_NAME_KEY, name.value()),
                        entry(DEFINITION_ALIAS_KEY, alias.value()),
                        entry(DEFINITION_EXPRESSION_KEY, expression.value()),
                        entry(DEFINITION_OBJECT_TYPE_KEY, objectType.originalValue()),
                        entry(DEFINITION_AGGREGATION_TYPE_KEY, aggregationType.value().name()),
                        entry(DEFINITION_AGGREGATION_PERIOD_KEY, String.valueOf(aggregationPeriod.value())),
                        entry(DEFINITION_AGGREGATION_ELEMENTS_KEY, aggregationElements.value().stream().map(ElementBase::value).collect(Collectors.toList())),
                        entry(DEFINITION_EXPORTABLE_KEY, String.valueOf(exportable.value())),
                        entry(DEFINITION_FILTER_KEY, filters.value().stream().map(ElementBase::value).map(Filter::new).collect(Collectors.toList()))
                )));
            }
        }
    }

    private static void mapComplex(final Collection<Definition> definitions, final KpiDefinitionRequest kpiDefinition) {
        final ScheduledComplex scheduledComplex = kpiDefinition.scheduledComplex();

        if (scheduledComplex == null || scheduledComplex.kpiOutputTables() == null || scheduledComplex.kpiOutputTables().isEmpty()) {
            return;
        }

        final List<ComplexTable> complexTables = scheduledComplex.kpiOutputTables();

        for (final ComplexTable complexTable : complexTables) {
            /* Attributes provided by table hierarchy */
            final ComplexTableAggregationPeriod aggregationPeriod = complexTable.aggregationPeriod();
            final ComplexTableAlias alias = complexTable.alias();

            for (final ComplexKpiDefinition definition : complexTable.kpiDefinitions()) {
                /* Attributes provided by definition hierarchy */
                final ComplexDefinitionName name = definition.name();
                final ComplexDefinitionExpression expression = definition.expression();
                final ComplexDefinitionObjectType objectType = definition.objectType();
                final ComplexDefinitionAggregationType aggregationType = definition.aggregationType();
                final ComplexDefinitionExecutionGroup executionGroup = definition.executionGroup();
                final ComplexDefinitionAggregationElements aggregationElements = definition.aggregationElements();
                final ComplexDefinitionExportable exportable = definition.exportable();
                final ComplexDefinitionFilters filters = definition.filters();
                final ComplexDefinitionDataReliabilityOffset dataReliabilityOffset = definition.dataReliabilityOffset();
                final ComplexDefinitionDataLookBackLimit dataLookBackLimit = definition.dataLookBackLimit();
                final ComplexDefinitionReexportLateData reexportLateData = definition.reexportLateData();

                definitions.add(new Definition(Map.ofEntries(
                        entry(DEFINITION_NAME_KEY, name.value()),
                        entry(DEFINITION_ALIAS_KEY, alias.value()),
                        entry(DEFINITION_EXPRESSION_KEY, expression.value()),
                        entry(DEFINITION_OBJECT_TYPE_KEY, objectType.originalValue()),
                        entry(DEFINITION_AGGREGATION_TYPE_KEY, aggregationType.value().name()),
                        entry(DEFINITION_AGGREGATION_PERIOD_KEY, String.valueOf(aggregationPeriod.value())),
                        entry(DEFINITION_EXECUTION_GROUP_KEY, executionGroup.value()),
                        entry(DEFINITION_AGGREGATION_ELEMENTS_KEY, aggregationElements.value().stream().map(ElementBase::value).collect(Collectors.toList())),
                        entry(DEFINITION_EXPORTABLE_KEY, String.valueOf(exportable.value())),
                        entry(DEFINITION_FILTER_KEY, filters.value().stream().map(ElementBase::value).map(Filter::new).collect(Collectors.toList())),
                        entry(DEFINITION_RELIABILITY_OFFSET_KEY, String.valueOf(dataReliabilityOffset.value())),
                        entry(DEFINITION_LOOKBACK_LIMIT_KEY, String.valueOf(dataLookBackLimit.value())),
                        entry(DEFINITION_REEXPORT_LATE_DATA_KEY, String.valueOf(reexportLateData.value()))
                )));
            }
        }
    }

    private static void mapSimple(final Collection<Definition> definitions, final KpiDefinitionRequest kpiDefinition) {
        final ScheduledSimple scheduledSimple = kpiDefinition.scheduledSimple();

        if (scheduledSimple == null || scheduledSimple.kpiOutputTables() == null || scheduledSimple.kpiOutputTables().isEmpty()) {
            return;
        }

        final List<SimpleTable> simpleTables = scheduledSimple.kpiOutputTables();

        for (final SimpleTable simpleTable : simpleTables) {
            /* Attributes provided by table hierarchy */
            final SimpleTableAggregationPeriod aggregationPeriod = simpleTable.aggregationPeriod();
            final SimpleTableAlias alias = simpleTable.alias();

            for (final SimpleKpiDefinition definition : simpleTable.kpiDefinitions()) {
                /* Attributes provided by definition hierarchy */
                final SimpleDefinitionName name = definition.name();
                final SimpleDefinitionExpression expression = definition.expression();
                final SimpleDefinitionObjectType objectType = definition.objectType();
                final SimpleDefinitionAggregationType aggregationType = definition.aggregationType();
                final SimpleDefinitionAggregationElements aggregationElements = definition.aggregationElements();
                final SimpleDefinitionExportable exportable = definition.exportable();
                final SimpleDefinitionFilters filters = definition.filters();
                final SimpleDefinitionDataReliabilityOffset dataReliabilityOffset = definition.dataReliabilityOffset();
                final SimpleDefinitionDataLookBackLimit dataLookBackLimit = definition.dataLookBackLimit();
                final SimpleDefinitionReexportLateData reexportLateData = definition.reexportLateData();
                final SimpleDefinitionInpDataIdentifier inpDataIdentifier = definition.inpDataIdentifier();

                definitions.add(new Definition(Map.ofEntries(
                        entry(DEFINITION_NAME_KEY, name.value()),
                        entry(DEFINITION_ALIAS_KEY, alias.value()),
                        entry(DEFINITION_EXPRESSION_KEY, expression.value()),
                        // The underlying codebase expects this
                        entry(DEFINITION_OBJECT_TYPE_KEY, objectType.originalValue()),
                        entry(DEFINITION_AGGREGATION_TYPE_KEY, aggregationType.value().name()),
                        entry(DEFINITION_AGGREGATION_PERIOD_KEY, String.valueOf(aggregationPeriod.value())),
                        entry(DEFINITION_AGGREGATION_ELEMENTS_KEY, aggregationElements.value().stream().map(ElementBase::value).collect(Collectors.toList())),
                        entry(DEFINITION_EXPORTABLE_KEY, String.valueOf(exportable.value())),
                        entry(DEFINITION_FILTER_KEY, filters.value().stream().map(ElementBase::value).map(Filter::new).collect(Collectors.toList())),
                        entry(DEFINITION_IDENTIFIER_KEY, DataIdentifier.of(inpDataIdentifier.value())),
                        entry(DEFINITION_RELIABILITY_OFFSET_KEY, String.valueOf(dataReliabilityOffset.value())),
                        entry(DEFINITION_LOOKBACK_LIMIT_KEY, String.valueOf(dataLookBackLimit.value())),
                        entry(DEFINITION_REEXPORT_LATE_DATA_KEY, String.valueOf(reexportLateData.value()))
                )));
            }
        }
    }

}

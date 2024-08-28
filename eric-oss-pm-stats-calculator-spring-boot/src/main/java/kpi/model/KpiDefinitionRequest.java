/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model;

import static java.util.Optional.ofNullable;
import static kpi.model.util.ValidationResults.getOutputTableAliasAndAggPeriod;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kpi.model.api.table.Table;
import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.api.AggregationPeriodAttribute;
import kpi.model.api.table.definition.api.AliasAttribute;
import kpi.model.util.ValidationResults;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.tuple.Pair;

@Data
@Builder
@Jacksonized
@Accessors(fluent = true)
@ToString(includeFieldNames = false)
@JsonNaming(SnakeCaseStrategy.class)
public class KpiDefinitionRequest {
    @JsonUnwrapped
    private final RetentionPeriod retentionPeriod;
    private final OnDemand onDemand;
    private final ScheduledComplex scheduledComplex;
    private final ScheduledSimple scheduledSimple;

    @Builder
    private KpiDefinitionRequest(final RetentionPeriod retentionPeriod, final OnDemand onDemand, final ScheduledComplex scheduledComplex, final ScheduledSimple scheduledSimple) {
        if (Stream.of(scheduledSimple, scheduledComplex, onDemand).allMatch(Objects::isNull)) {
            throw new IllegalArgumentException("At least one kpi definition type is required");
        }
        //TODO: Should be removed after multiple rApp step
        if (retentionPeriod.value() != null) {
            throw new IllegalArgumentException("Retention period cannot be set on collection level!");
        }

        validateOutputTableEmptiness(scheduledSimple);
        validateOutputTableEmptiness(scheduledComplex);
        validateOutputTableEmptiness(onDemand);

        this.retentionPeriod = retentionPeriod;
        this.scheduledSimple = ofNullable(scheduledSimple).orElseGet(ScheduledSimple::empty);
        this.scheduledComplex = ofNullable(scheduledComplex).orElseGet(ScheduledComplex::empty);
        this.onDemand = ofNullable(onDemand).orElseGet(OnDemand::empty);

        validateAliasesAreUniqueForAKpiType();
        if (onDemand != null) {
            validateAliasesAreUniqueForAllKpiType();
        }
    }

    private void validateAliasesAreUniqueForAllKpiType() {
        final Set<Pair<AliasAttribute, AggregationPeriodAttribute>> scheduledOutputTableNames = new HashSet<>();
        scheduledOutputTableNames.addAll(getOutputTableAliasAndAggPeriod(scheduledSimple.kpiOutputTables()));
        scheduledOutputTableNames.addAll(getOutputTableAliasAndAggPeriod(scheduledComplex.kpiOutputTables()));

        if (!scheduledOutputTableNames.isEmpty()) {
            ValidationResults.validateAliasesAreUniqueForAllKpiTypes(getOutputTableAliasAndAggPeriod(onDemand.kpiOutputTables()), scheduledOutputTableNames);
        }
    }

    private void validateOutputTableEmptiness(final KpiDefinitionTable<? extends Table> kpiDefinitionTable) {
        if (kpiDefinitionTable == null) {
            return;
        }
        if (ValidationResults.isEmpty(kpiDefinitionTable.kpiOutputTables())) {
            throw new IllegalArgumentException("At least one kpi table is required");
        }
    }

    private void validateAliasesAreUniqueForAKpiType() {
        ValidationResults.validateAliasesAreUniqueForAKpiType(onDemand.kpiOutputTables());
        ValidationResults.validateAliasesAreUniqueForAKpiType(scheduledComplex.kpiOutputTables());
        ValidationResults.validateAliasesAreUniqueForAKpiType(scheduledSimple.kpiOutputTables());
    }

    public Set<KpiDefinition> definitions() {
        return Stream.of(onDemand, scheduledComplex, scheduledSimple)
                .map(KpiDefinitionTable::definitions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public List<Table> tables() {
        return Stream.of(onDemand, scheduledComplex, scheduledSimple)
                .map(KpiDefinitionTable::kpiOutputTables)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}

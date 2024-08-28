/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import lombok.*;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.EXECUTION_GROUP_ON_DEMAND_CALCULATION;

/**
 * This class wraps all necessary information for kpi calculation.
 */
@Getter
@ToString
public final class KpiCalculationJob {
    private final UUID calculationId;
    private final Timestamp timeCreated;
    private final String executionGroup;
    private final Collection<String> kpiDefinitionNames;
    private final KpiType jobType;

    /* TODO: Calculation limits are needed only for COMPLEX KPI calculations.
             Find a solution to extract type specific attributes to different classes: abstraction, polymorphism etc... */
    private final CalculationLimit calculationLimit;

    @Setter
    private String jmxPort;

    @Builder(setterPrefix = "with")
    private KpiCalculationJob(final UUID calculationId,
                              @NonNull final Timestamp timeCreated,
                              final String executionGroup,
                              final Collection<String> kpiDefinitionNames,
                              final String jmxPort,
                              final KpiType jobType,
                              final CalculationLimit calculationLimit) { //NOSONAR Too many constructor parameters
        this.calculationId = calculationId;
        this.timeCreated = copyOf(timeCreated);
        this.executionGroup = executionGroup;
        this.kpiDefinitionNames = kpiDefinitionNames == null ? Collections.emptySet() : kpiDefinitionNames;
        this.jmxPort = jmxPort;
        this.jobType = jobType;
        this.calculationLimit = calculationLimit;
    }

    public static KpiCalculationJobBuilder defaultsBuilder() {
        return builder()
                .withCalculationId(UUID.randomUUID())
                .withTimeCreated(new Timestamp(System.currentTimeMillis()));
    }

    public boolean isOnDemand() {
        return EXECUTION_GROUP_ON_DEMAND_CALCULATION.equalsIgnoreCase(executionGroup);
    }

    public Collection<String> getKpiDefinitionNames() {
        return Collections.unmodifiableCollection(kpiDefinitionNames);
    }

    public Timestamp getTimeCreated() {
        return copyOf(timeCreated);
    }

    public boolean isComplex() {
        return jobType == KpiType.SCHEDULED_COMPLEX;
    }

    public boolean isSimple() {
        return jobType == KpiType.SCHEDULED_SIMPLE;
    }

    private static Timestamp copyOf(final Timestamp timestamp) {
        return (Timestamp) timestamp.clone();
    }

    /**
     * Lombok and JavaDoc is not working together in the Maven's <strong>release</strong> phase.
     * By adding the class signature JavaDoc can progress - can find this class.
     * <br>
     * <a href="https://stackoverflow.com/questions/51947791/javadoc-cannot-find-symbol-error-when-using-lomboks-builder-annotation">Issue thread</a>
     */
    @SuppressWarnings("all")
    public static final class KpiCalculationJobBuilder {
    }
}
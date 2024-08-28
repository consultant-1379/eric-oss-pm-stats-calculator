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

import static java.util.Collections.emptyList;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kpi.model.complex.ComplexTable;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@Accessors(fluent = true)
@ToString(includeFieldNames = false)
@JsonNaming(SnakeCaseStrategy.class)
public class ScheduledComplex implements KpiDefinitionTable<ComplexTable> {
    private final List<ComplexTable> kpiOutputTables;

    public static ScheduledComplex empty() {
        return new ScheduledComplex(emptyList());
    }
}

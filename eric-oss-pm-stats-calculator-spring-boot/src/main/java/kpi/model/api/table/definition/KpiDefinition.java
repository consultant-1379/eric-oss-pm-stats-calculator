/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.table.definition;

import com.ericsson.oss.air.pm.stats.common.model.attribute.ExpressionAttribute;
import com.ericsson.oss.air.pm.stats.common.model.element.AggregationElement;
import com.ericsson.oss.air.pm.stats.common.model.element.FilterElement;

import kpi.model.api.table.definition.api.AggregationElementsAttribute;
import kpi.model.api.table.definition.api.AggregationTypeAttribute;
import kpi.model.api.table.definition.api.DataLookBackLimitAttribute;
import kpi.model.api.table.definition.api.DataReliabilityOffsetAttribute;
import kpi.model.api.table.definition.api.ExecutionGroupAttribute;
import kpi.model.api.table.definition.api.ExportableAttribute;
import kpi.model.api.table.definition.api.FiltersAttribute;
import kpi.model.api.table.definition.api.InpDataIdentifierAttribute;
import kpi.model.api.table.definition.api.NameAttribute;
import kpi.model.api.table.definition.api.ObjectTypeAttribute;
import kpi.model.api.table.definition.api.ReexportLateDataAttribute;

public interface KpiDefinition {
    <T extends FilterElement> FiltersAttribute<T> filters();

    ExpressionAttribute expression();

    NameAttribute name();

    ObjectTypeAttribute objectType();

    AggregationTypeAttribute aggregationType();

    ExportableAttribute exportable();

    <T extends AggregationElement> AggregationElementsAttribute<T> aggregationElements();

    default DataLookBackLimitAttribute dataLookBackLimit() {
        throw new UnsupportedOperationException();
    }

    default ReexportLateDataAttribute reexportLateData() {
        throw new UnsupportedOperationException();
    }

    default ExecutionGroupAttribute executionGroup() {
        throw new UnsupportedOperationException();
    }

    default DataReliabilityOffsetAttribute dataReliabilityOffset() {
        throw new UnsupportedOperationException();
    }

    default InpDataIdentifierAttribute inpDataIdentifier() {
        throw new UnsupportedOperationException();
    }

    default boolean isScheduledComplex() {
        try {
            executionGroup();
            return true;
        } catch (final UnsupportedOperationException ignored) {
            return false;
        }
    }

    default boolean isSimple() {
        try {
            inpDataIdentifier();
            return true;
        } catch (final UnsupportedOperationException ignored) {
            return false;
        }
    }
}

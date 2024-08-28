/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple.table.optional;

import kpi.model.api.table.OptionalTableAttribute;
import kpi.model.api.table.OptionalTableAttributeContract;
import kpi.model.util.Defaults;

class SimpleTableDataReliabilityOffsetTest implements OptionalTableAttributeContract<Integer> {
    @Override
    public OptionalTableAttribute<Integer> createInstance() {
        return SimpleTableDataReliabilityOffset.of(60);
    }

    @Override
    public String name() {
        return "data_reliability_offset";
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public String representation() {
        return "data_reliability_offset = 60";
    }

    @Override
    public Integer defaultValue() {
        return Defaults.DATA_RELIABILITY_OFFSET;
    }

}
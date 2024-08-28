/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple.table.definition.optional;

import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttributeContract;
import kpi.model.simple.table.optional.SimpleTableDataReliabilityOffset;

class SimpleDefinitionDataReliabilityOffsetTest implements OptionalDefinitionAttributeContract<Integer> {
    @Override
    public OptionalDefinitionAttribute<Integer> createInstance() {
        return SimpleDefinitionDataReliabilityOffset.of(10);
    }

    @Override
    public TableAttribute<Integer> createParentInstance() {
        return SimpleTableDataReliabilityOffset.of(30);
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
        return "data_reliability_offset = 10";
    }

    @Override
    public Class<? extends TableAttribute<Integer>> parentClass() {
        return SimpleTableDataReliabilityOffset.class;
    }

}
/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model._helper._assert;

import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionAssertions {

    public static SimpleKpiDefinitionAssert assertThat(final SimpleKpiDefinition actual) {
        return new SimpleKpiDefinitionAssert(actual, SimpleKpiDefinitionAssert.class);
    }

    public static ComplexKpiDefinitionAssert assertThat(final ComplexKpiDefinition actual) {
        return new ComplexKpiDefinitionAssert(actual, ComplexKpiDefinitionAssert.class);
    }

    public static OnDemandKpiDefinitionAssert assertThat(final OnDemandKpiDefinition actual) {
        return new OnDemandKpiDefinitionAssert(actual, OnDemandKpiDefinitionAssert.class);
    }
}

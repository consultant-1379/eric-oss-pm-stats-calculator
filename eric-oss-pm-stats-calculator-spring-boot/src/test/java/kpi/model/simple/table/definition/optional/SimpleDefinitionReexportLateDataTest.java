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
import kpi.model.simple.table.optional.SimpleTableReexportLateData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleDefinitionReexportLateDataTest implements OptionalDefinitionAttributeContract<Boolean> {
    @Override
    public OptionalDefinitionAttribute<Boolean> createInstance() {
        return SimpleDefinitionReexportLateData.of(true);
    }

    @Override
    public TableAttribute<Boolean> createParentInstance() {
        return SimpleTableReexportLateData.of(false);
    }

    @Override
    public String name() {
        return "reexport_late_data";
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public String representation() {
        return "reexport_late_data = true";
    }

    @Override
    public Class<? extends TableAttribute<Boolean>> parentClass() {
        return SimpleTableReexportLateData.class;
    }

    @Test
    void shouldValidateParentClass() {
        final SimpleDefinitionReexportLateData attribute = SimpleDefinitionReexportLateData.of(true);
        Assertions.assertThat(attribute.parentClass()).isEqualTo(SimpleTableReexportLateData.class);
    }

}
/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.complex.table.definition.optional;

import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttributeContract;
import kpi.model.complex.table.optional.ComplexTableExportable;

class ComplexDefinitionExportableTest implements OptionalDefinitionAttributeContract<Boolean> {
    @Override
    public OptionalDefinitionAttribute<Boolean> createInstance() {
        return ComplexDefinitionExportable.of(true);
    }

    @Override
    public TableAttribute<Boolean> createParentInstance() {
        return ComplexTableExportable.of(false);
    }

    @Override
    public String name() {
        return "exportable";
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public String representation() {
        return "exportable = true";
    }

    @Override
    public Class<? extends TableAttribute<Boolean>> parentClass() {
        return ComplexTableExportable.class;
    }

}

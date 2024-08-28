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

class SimpleTableExportableTest implements OptionalTableAttributeContract<Boolean> {
    @Override
    public OptionalTableAttribute<Boolean> createInstance() {
        return SimpleTableExportable.of(true);
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
    public Boolean defaultValue() {
        return Defaults.EXPORTABLE;
    }
}
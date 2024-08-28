/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.ondemand.table.optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.OptionalTableAttribute;
import kpi.model.api.table.definition.api.ExportableAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.util.Attributes;
import kpi.model.util.Defaults;

public class OnDemandTableExportable extends OptionalTableAttribute<Boolean> implements ExportableAttribute {

    private OnDemandTableExportable(final Boolean value) {
        super(value);
    }

    @JsonCreator
    public static OnDemandTableExportable of(@JsonProperty(Attributes.ATTRIBUTE_IS_EXPORTABLE) final Boolean value) {
        return new OnDemandTableExportable(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_IS_EXPORTABLE;
    }

    @Override
    public Boolean defaultValue() {
        return Defaults.EXPORTABLE;
    }

    @Override
    protected ValidationResult validateValue(final Boolean value) {
        return super.validateValue(value).andIfNotNullThen(value, ValidationResult::valid);
    }
}

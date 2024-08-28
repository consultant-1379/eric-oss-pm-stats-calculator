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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import kpi.model.api.table.api.TableAttribute;
import kpi.model.api.table.definition.OptionalDefinitionAttribute;
import kpi.model.api.table.definition.api.ReexportLateDataAttribute;
import kpi.model.api.validation.ValidationResult;
import kpi.model.complex.table.optional.ComplexTableReexportLateData;
import kpi.model.util.Attributes;

public class ComplexDefinitionReexportLateData extends OptionalDefinitionAttribute<Boolean> implements ReexportLateDataAttribute {

    private ComplexDefinitionReexportLateData(final Boolean value) {
        super(value);
    }

    @JsonCreator
    public static ComplexDefinitionReexportLateData of(@JsonProperty(Attributes.ATTRIBUTE_REEXPORT_LATE_DATA) final Boolean value) {
        return new ComplexDefinitionReexportLateData(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_REEXPORT_LATE_DATA;
    }

    @Override
    protected ValidationResult validateValue(final Boolean value) {
        return super.validateValue(value).andThen(ValidationResult::valid);
    }

    @Override
    public Class<? extends TableAttribute<Boolean>> parentClass() {
        return ComplexTableReexportLateData.class;
    }
}
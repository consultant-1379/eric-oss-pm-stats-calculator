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
import kpi.model.api.table.definition.api.ExportableAttribute;
import kpi.model.complex.table.optional.ComplexTableExportable;
import kpi.model.util.Attributes;

public class ComplexDefinitionExportable extends OptionalDefinitionAttribute<Boolean> implements ExportableAttribute {

    private ComplexDefinitionExportable(final Boolean value) {
        super(value);
    }

    @JsonCreator
    public static ComplexDefinitionExportable of(@JsonProperty(Attributes.ATTRIBUTE_IS_EXPORTABLE) final Boolean value) {
        return new ComplexDefinitionExportable(value);
    }

    @Override
    public String name() {
        return Attributes.ATTRIBUTE_IS_EXPORTABLE;
    }

    @Override
    public Class<? extends TableAttribute<Boolean>> parentClass() {
        return ComplexTableExportable.class;
    }
}

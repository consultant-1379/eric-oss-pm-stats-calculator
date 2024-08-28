/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.output;

import java.util.List;

public interface KpiDefinitionDto {
    String name();

    String expression();

    String objectType();

    String aggregationType();

    List<String> aggregationElements();

    Boolean exportable();

    List<String> filters();

    default Boolean reexportLateData() {
        throw new UnsupportedOperationException();
    }

    default Integer dataReliabilityOffset() {
        throw new UnsupportedOperationException();
    }

    default Integer dataLookbackLimit() {
        throw new UnsupportedOperationException();
    }

    default String executionGroup() {
        throw new UnsupportedOperationException();
    }

    default String inpDataIdentifier() {
        throw new UnsupportedOperationException();
    }
}

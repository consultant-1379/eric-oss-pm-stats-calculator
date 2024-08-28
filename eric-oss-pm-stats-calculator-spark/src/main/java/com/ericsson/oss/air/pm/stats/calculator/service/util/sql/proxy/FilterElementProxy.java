/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.sql.proxy;

import com.ericsson.oss.air.pm.stats.common.model.element.FilterElement;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FilterElementProxy implements FilterElement {
    private final String element;

    @Override
    public String value() {
        return element;
    }

    @Override
    public String name() {
        return "filter-element-proxy";
    }
}

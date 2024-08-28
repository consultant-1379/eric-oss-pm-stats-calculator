/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition;

import static com.ericsson.oss.air.pm.stats.calculator.api.constant.PatternConstants.PATTERN_TO_SPLIT_ON_DOT;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class Filter {
    public static final Filter EMPTY = new Filter(StringUtils.EMPTY);

    private final String name;

    public static Filter of(final String name) {
        return new Filter(name);
    }

    public boolean isConflicting(final Filter filter) {
        final String[] thisTokens = PATTERN_TO_SPLIT_ON_DOT.split(this.name, 2);
        final String[] otherTokens = PATTERN_TO_SPLIT_ON_DOT.split(filter.getName(), 2);
        return thisTokens[0].equalsIgnoreCase(otherTokens[0]) && !thisTokens[1].equalsIgnoreCase(otherTokens[1]);
    }

    public boolean isEmpty() {
        return name == null || name.isEmpty();
    }
}

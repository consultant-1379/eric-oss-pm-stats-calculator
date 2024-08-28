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

import java.util.Objects;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DataIdentifier {
    private static final DataIdentifier EMPTY = new DataIdentifier(StringUtils.EMPTY);
    private final String name;

    public static DataIdentifier of(final String name) {
        return Objects.isNull(name)
                ? EMPTY
                : new DataIdentifier(name);
    }

    public static DataIdentifier of(final String dataSpace, final String category, final String schemaName) {
        return of(String.format("%s|%s|%s", dataSpace, category, schemaName));
    }

    private String splitName(int index) {
        if (this.isEmpty()) {
            return null;
        }
        final String[] splitData = name.split("\\|");
        if (splitData.length != 3) {
            return null;
        }
        return splitData[index].trim();
    }

    public boolean isEmpty() {
        return equals(EMPTY);
    }

    public String dataSpace() {
        return splitName(0);
    }

    public String category() {
        return splitName(1);
    }

    public String schema() {
        return splitName(2);
    }

    public String toString() {
        return name;
    }
}
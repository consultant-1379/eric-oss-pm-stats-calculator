/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TabularParameterUtils {

    public static Collection<String> makeUniqueTableNamesForListOfTabularParameters(final Collection<String> tabularParameterNames, final UUID uuid) {
        final List<String> uniqueTableNames = new ArrayList<>();
        tabularParameterNames.forEach(tabularParameterName -> uniqueTableNames.add(makeUniqueTableNameForTabularParameters(tabularParameterName, uuid)));
        return uniqueTableNames;
    }

    public static String makeUniqueTableNameForTabularParameters(final String tabularParameterName, final UUID uuid) {
        return String.format("%s_%s", tabularParameterName, splitUuid(uuid));
    }

    public static Predicate<Parameter> aggregationElementPredicate(@NonNull final AggregationElement aggregationElement) {
        return parameter -> aggregationElement.getSourceTable().equals(parameter.tabularParameter().name()) && aggregationElement.getSourceColumn().equals(parameter.name());
    }

    private static String splitUuid(final UUID uuid) {
        return uuid.toString().split("-")[0];
    }
}

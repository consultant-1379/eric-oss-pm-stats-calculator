/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.helper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;

import kpi.model.ondemand.element.OnDemandAggregationElement;
import kpi.model.ondemand.element.OnDemandFilterElement;

@ApplicationScoped
public class ParameterDefinitionHelper {
    private static final Pattern PATTERN_PARAMETER_FINDER = Pattern.compile("'\\$\\{([a-zA-Z][a-zA-Z0-9_.]+)\\}'");

    public Set<String> collectParameterNames(final String parameterizedElement) {
        final Set<String> parameterNames = new HashSet<>();
        final Matcher matcher = PATTERN_PARAMETER_FINDER.matcher(parameterizedElement);

        while (matcher.find()) {
            final String paramName = matcher.group(1);
            parameterNames.add(paramName);
        }

        return parameterNames;
    }

    public Set<String> collectParameterNames(final OnDemandAggregationElement aggregationElement) {
        return collectParameterNames(aggregationElement.value());
    }

    public Set<String> collectParameterNames(final OnDemandFilterElement filterElement) {
        return collectParameterNames(filterElement.value());
    }

    public Set<String> collectParameterNames(final List<String> parameterizedElements) {
        return parameterizedElements.stream().flatMap(element -> collectParameterNames(element).stream()).collect(Collectors.toSet());
    }
}

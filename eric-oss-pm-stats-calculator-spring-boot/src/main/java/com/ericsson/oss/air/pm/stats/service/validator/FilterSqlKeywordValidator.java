/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.SQL_FROM;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.SQL_WHERE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.DefinitionNameErrorResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilterSqlKeywordValidator {

    /**
     * Validates definition filters for SQL keywords WHERE and FROM.
     *
     * @param payloadDefinitions the definitions to be validated
     * @return the {@link List} of {@link DefinitionNameErrorResponse} associated with the request.
     */
    public static List<DefinitionNameErrorResponse> validateFiltersForSqlKeywords(final Set<Definition> payloadDefinitions) {
        final List<DefinitionNameErrorResponse> result = new ArrayList<>();

        for (final Definition definition : payloadDefinitions) {
            for (final Filter filter : definition.getFilters()) {
                if (StringUtils.containsIgnoreCase(filter.getName(), SQL_FROM)
                        || StringUtils.containsIgnoreCase(filter.getName(), SQL_WHERE)) {
                    final String errorCause = String.format(
                            "Value '%s' provided for filter field is invalid. Filters must not contain SQL keywords 'WHERE' OR 'FROM'",
                            filter.getName());
                    log.warn(errorCause);
                    result.add(new DefinitionNameErrorResponse(List.of(errorCause), String.valueOf(definition.getAttributeByName("name"))));
                }
            }
        }

        return result;
    }
}

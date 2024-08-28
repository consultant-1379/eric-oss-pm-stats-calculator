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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.DefinitionNameErrorResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FilterSqlKeywordValidatorTest {

    private static Definition definitionNoKeyWord;
    private static Definition definitionWhere;
    private static Definition definitionLowerCaseWhere;
    private static Definition definitionFrom;
    private static String expectedFromMessage;
    private static String expectedWhereMessage;
    private static String expectedLowerCaseWhereMessage;

    @BeforeAll
    static void setUp() {
        definitionNoKeyWord = makeDefinitions("no_keyword", List.of("kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '2024-06-06'"));
        definitionFrom = makeDefinitions("from", List.of("kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '2024-06-06' "  + SQL_FROM));
        definitionWhere = makeDefinitions("where", List.of("kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '2024-06-06' "  + SQL_WHERE));
        definitionLowerCaseWhere = makeDefinitions("where", List.of("kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '2024-06-06' "  + SQL_WHERE.toLowerCase()));
        expectedFromMessage = "Value 'kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '2024-06-06' FROM' provided for filter field is invalid. " +
                "Filters must not contain SQL keywords 'WHERE' OR 'FROM'";
        expectedWhereMessage = "Value 'kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '2024-06-06' WHERE' provided for filter field is invalid. " +
                "Filters must not contain SQL keywords 'WHERE' OR 'FROM'";
        expectedLowerCaseWhereMessage = "Value 'kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) = '2024-06-06' where' provided for filter field is invalid. " +
                "Filters must not contain SQL keywords 'WHERE' OR 'FROM'";
    }

    @Test
    void shouldReturnEmpty_whenNoKeyWordsPassed () {
        List<DefinitionNameErrorResponse> actual = FilterSqlKeywordValidator.validateFiltersForSqlKeywords(Set.of(definitionNoKeyWord));

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldReturnErrorResponse_whenFromPassed () {

        List<DefinitionNameErrorResponse> actual = FilterSqlKeywordValidator.validateFiltersForSqlKeywords(Set.of(definitionNoKeyWord, definitionFrom));

        DefinitionNameErrorResponse expected = new DefinitionNameErrorResponse(List.of(expectedFromMessage), "from");

        assertThat(actual).containsExactly(expected);
    }

    @Test
    void shouldReturnErrorResponse_whenWherePassed () {

        List<DefinitionNameErrorResponse> actual = FilterSqlKeywordValidator.validateFiltersForSqlKeywords(Set.of(definitionNoKeyWord, definitionWhere));

        DefinitionNameErrorResponse expected = new DefinitionNameErrorResponse(List.of(expectedWhereMessage), "where");

        assertThat(actual).containsExactly(expected);
    }

    @Test
    void shouldReturnErrorResponse_whenLowercaseWherePassed () {

        List<DefinitionNameErrorResponse> actual = FilterSqlKeywordValidator.validateFiltersForSqlKeywords(Set.of(definitionNoKeyWord, definitionLowerCaseWhere));

        DefinitionNameErrorResponse expected = new DefinitionNameErrorResponse(List.of(expectedLowerCaseWhereMessage), "where");

        assertThat(actual).containsExactly(expected);
    }

    @Test
    void shouldReturnTwoErrorResponses_whenFromAndWherePassed () {

        List<DefinitionNameErrorResponse> actual = FilterSqlKeywordValidator.validateFiltersForSqlKeywords(Set.of(definitionNoKeyWord, definitionWhere, definitionFrom));

        DefinitionNameErrorResponse expectedWhere = new DefinitionNameErrorResponse(List.of(expectedWhereMessage), "where");
        DefinitionNameErrorResponse expectedFrom = new DefinitionNameErrorResponse(List.of(expectedFromMessage), "from");

        assertThat(actual).containsExactlyInAnyOrder(expectedWhere, expectedFrom);
    }

    private static Definition makeDefinitions(final String name, final List<String> filters) {
        Definition result = new Definition();
        result.setAttribute("filter", filters.stream()
                .map(Filter::new)
                .collect(Collectors.toList()));
        result.setAttribute("name", name);
        return result;
    }
}
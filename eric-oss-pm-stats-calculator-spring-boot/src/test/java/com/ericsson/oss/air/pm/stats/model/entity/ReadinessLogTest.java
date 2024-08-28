/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.entity;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReadinessLogTest {

    @MethodSource("provideHasSameDatasourceData")
    @ParameterizedTest(name = "[{index}] ''{0}'' && ''{1}'' is same ==> ''{2}''")
    void shouldDecideHasSameDatasource(final String datasource, final String dataSpace, final String schemaCategory, final String schemaName, final boolean expected) {
        final ReadinessLog readinessLog = ReadinessLog.builder().withDatasource(datasource).build();
        final KpiDefinitionEntity kpiDefinition = KpiDefinitionEntity.builder().withSchemaDataSpace(dataSpace).withSchemaCategory(schemaCategory).withSchemaName(schemaName).build();

        final boolean actual = readinessLog.hasSameDatasource(kpiDefinition);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> provideHasSameDatasourceData() {
        return Stream.of(
                Arguments.of("dataSpace|schemaCategory|schemaName", "dataSpace", "schemaCategory", "schemaName", true),
                Arguments.of("random", "schemaCategory", "schemaName", "identifier", false)
        );
    }
}
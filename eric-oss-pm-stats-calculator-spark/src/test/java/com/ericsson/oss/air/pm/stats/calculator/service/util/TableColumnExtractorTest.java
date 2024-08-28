/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import static com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory.sqlProcessorDelegator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator._test.TestObjectFactory;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition.KpiDefinitionBuilder;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.TableColumns;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;

import org.apache.spark.sql.execution.SparkSqlParser;
import org.junit.jupiter.api.Test;

class TableColumnExtractorTest {
    KpiDefinitionHelperImpl kpiDefinitionHelper = TestObjectFactory.kpiDefinitionHelper(mock(KpiDefinitionHierarchy.class));

    TableColumnExtractor objectUnderTest = new TableColumnExtractor(sqlProcessorDelegator(new SparkSqlParser()), kpiDefinitionHelper);

    @Test
    void shouldExtractAllTableColumns() {
        final KpiDefinition defDsInMemory = definition("in_memory_table.in_memory_column FROM kpi_inmemory://in_memory_table", "table2.column22");
        final KpiDefinition defDsExt2 = definition("ext2_table.ext2_column FROM external_ds_2://ext2_table", "as_table1.as_column1 AS columnAs", "table1.column12");
        final KpiDefinition defDsNull = definition("null_table.null_column FROM null_table", "table1.column12", "table1.column13");
        final KpiDefinition defDsExt1 = definition("ext1_table1.ext1_column1 FROM external_ds_1://ext1_table1", "table2.column21");
        final KpiDefinition defDsExt1_2 = definition("ext1_table1.ext1_column1 FROM external_ds_1://ext1_table1", "table3.column31");
        final KpiDefinition defDsExt1_3 = definition("ext1_table3.ext1_column3 FROM external_ds_1://ext1_table3", "table4.column41");
        final KpiDefinition defDsExt1_4 = definition("ext1_table3.ext1_column3 FROM external_ds_1://ext1_table3", "FDN_PARSE(table3.column333, \"Context\") AS fdn");


        final List<KpiDefinition> kpiDefinitions = List.of(defDsInMemory, defDsExt2, defDsNull, defDsExt1, defDsExt1_2, defDsExt1_3, defDsExt1_4);

        final TableColumns expected = TableColumns.of();
        expected.computeIfAbsent(new SourceColumn("ext2_table.ext2_column"));
        expected.computeIfAbsent(new SourceColumn("null_table.null_column"));
        expected.computeIfAbsent(new SourceColumn("ext1_table1.ext1_column1"));
        expected.computeIfAbsent(new SourceColumn("ext1_table3.ext1_column3"));
        expected.computeIfAbsent(new SourceColumn("as_table1.as_column1"));
        expected.computeIfAbsent(new SourceColumn("table1.column12"));
        expected.computeIfAbsent(new SourceColumn("table1.column13"));
        expected.computeIfAbsent(new SourceColumn("table2.column21"));
        expected.computeIfAbsent(new SourceColumn("table2.column22"));
        expected.computeIfAbsent(new SourceColumn("table3.column31"));
        expected.computeIfAbsent(new SourceColumn("table4.column41"));
        expected.computeIfAbsent(new SourceColumn("table3.column333"));

        final TableColumns actual = objectUnderTest.extractAllTableColumns(kpiDefinitions);

        assertThat(actual).isEqualTo(expected);
    }

    static KpiDefinition definition(final String expression, final String... aggregationElements) {
        final KpiDefinitionBuilder builder = KpiDefinition.builder();
        builder.withExpression(expression);
        builder.withAggregationElements(Arrays.asList(aggregationElements));
        return builder.build();
    }
}

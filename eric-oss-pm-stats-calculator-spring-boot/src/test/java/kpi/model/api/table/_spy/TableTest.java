/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.table._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import kpi.model.api.table.Table;
import kpi.model.simple.table.optional.SimpleTableAggregationPeriod;
import kpi.model.simple.table.required.SimpleTableAlias;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableTest {
    @Spy
    Table objectUnderTest;

    @Nested
    class ComputeTableName {
        @Test
        void forDefaultAggregationPeriod() {
            doReturn(SimpleTableAggregationPeriod.of(null)).when(objectUnderTest).aggregationPeriod();
            doReturn(SimpleTableAlias.of("alias")).when(objectUnderTest).alias();

            final String actual = objectUnderTest.tableName();

            verify(objectUnderTest).aggregationPeriod();
            verify(objectUnderTest).alias();

            Assertions.assertThat(actual).isEqualTo("kpi_alias_");
        }

        @Test
        void forNonDefaultAggregationPeriod() {
            doReturn(SimpleTableAggregationPeriod.of(60)).when(objectUnderTest).aggregationPeriod();
            doReturn(SimpleTableAlias.of("alias")).when(objectUnderTest).alias();

            final String actual = objectUnderTest.tableName();

            verify(objectUnderTest).aggregationPeriod();
            verify(objectUnderTest).alias();

            Assertions.assertThat(actual).isEqualTo("kpi_alias_60");
        }
    }
}
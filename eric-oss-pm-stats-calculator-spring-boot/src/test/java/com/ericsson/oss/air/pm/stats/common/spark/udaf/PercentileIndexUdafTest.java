/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.udaf;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.expressions.MutableAggregationBuffer;
import org.junit.jupiter.api.Test;
import scala.collection.JavaConverters;
import scala.collection.Seq;

/**
 * Unit tests for {@link PercentileIndexUdaf}.
 */
class PercentileIndexUdafTest {

    @Test
    void whenArrayIsValid_ThenValidBinReturned() {
        final PercentileIndexUdaf result = new PercentileIndexUdaf(80.0);
        result.initialize(buffer);
        assertThat(result.evaluate(buffer)).isEqualTo(5);
    }

    Seq<Double> convertListToSeq(final List<Double> inputList) {
        return JavaConverters.collectionAsScalaIterableConverter(inputList).asScala().toSeq();
    }

    final MutableAggregationBuffer buffer = new MutableAggregationBuffer() {

        private static final long serialVersionUID = 1036460019368213896L;

        @Override
        public int length() {
            return 1;
        }

        @Override
        public Object get(final int arg0) {
            final List<Double> list = new ArrayList<>();
            list.add(3.0);
            list.add(4.0);
            list.add(1.0);
            list.add(2.0);
            list.add(3.0);
            list.add(4.0);
            return convertListToSeq(list);
        }

        @Override
        public Row copy() {
            return null;
        }

        @Override
        public void update(final int arg0, final Object arg1) {

        }
    };
}
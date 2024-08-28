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
 * Unit tests for {@link ArrayIndexSumUdaf}.
 */
class ArrayIndexSumUdafTest {

    @Test
    void whenArrayIsValid_ThenValidBufferReturned() {
        final ArrayIndexSumUdaf result = new ArrayIndexSumUdaf();
        result.initialize(buffer);
        assertThat(result.evaluate(buffer).toString()).isEqualTo("[3, 4, 1, 2, 3, 4]");
    }

    Seq<Integer> convertListToSeq(final List<Integer> inputList) {
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
            final List<Integer> list = new ArrayList<Integer>();
            list.add(3);
            list.add(4);
            list.add(1);
            list.add(2);
            list.add(3);
            list.add(4);
            return convertListToSeq(list);
        }

        @Override
        public Row copy() {
            return null;
        }

        @Override
        public void update(final int arg0, final Object arg1) { }
    };
}
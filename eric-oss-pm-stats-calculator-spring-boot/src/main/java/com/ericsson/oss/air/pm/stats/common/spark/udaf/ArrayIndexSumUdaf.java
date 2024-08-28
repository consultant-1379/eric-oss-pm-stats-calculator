/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.udaf;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.expressions.UserDefinedAggregateFunction;
import org.apache.spark.sql.types.DataTypes;

/**
 * {@link UserDefinedAggregateFunction} class that aggregates the indices of an array for multiple rows.
 * <p>
 * <b>NOTE:</b> Any 'null' values in the array will be converted to '0' before the aggregation.
 */
public class ArrayIndexSumUdaf extends AbstractArrayUdaf {

    public static final String NAME = "ARRAY_INDEX_SUM_INTEGER";

    private static final long serialVersionUID = -7864863998947479657L;

    public ArrayIndexSumUdaf() {
        super(DataTypes.createArrayType(DataTypes.IntegerType));
    }

    @Override
    public Object evaluate(final Row buffer) {
        if (buffer.isNullAt(0)) {
            return null;
        }

        return buffer.getList(0);
    }
}

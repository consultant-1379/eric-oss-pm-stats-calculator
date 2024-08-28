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

import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;

public class LongArrayIndexSum extends AbstractLongArrayUdaf {

    public static final String NAME = "ARRAY_INDEX_SUM_LONG";

    private static final long serialVersionUID = -3L;

    public LongArrayIndexSum() {
        super(DataTypes.createArrayType(DataTypes.LongType));
    }

    @Override
    public Object evaluate(final Row buffer) {
        if (buffer.isNullAt(0)) {
            return null;
        }

        return buffer.getList(0);
    }
}
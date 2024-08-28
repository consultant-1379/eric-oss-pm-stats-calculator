/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark;

import static com.ericsson.oss.air.pm.stats.common.spark.udaf.PercentileIndexUdaf.PERCENTILE_INDEX_80_UDAF;
import static com.ericsson.oss.air.pm.stats.common.spark.udaf.PercentileIndexUdaf.PERCENTILE_INDEX_90_UDAF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.ericsson.oss.air.pm.stats.common.spark.udaf.ArrayIndexSumUdaf;
import com.ericsson.oss.air.pm.stats.common.spark.udaf.DoubleArrayIndexSum;
import com.ericsson.oss.air.pm.stats.common.spark.udaf.LongArrayIndexSum;
import com.ericsson.oss.air.pm.stats.common.spark.udaf.PercentileIndexUdaf;
import com.ericsson.oss.air.pm.stats.common.spark.udf.AddValueToArrayUdf;
import com.ericsson.oss.air.pm.stats.common.spark.udf.AppendArrayGivenCardinalityAndLimitUdf;
import com.ericsson.oss.air.pm.stats.common.spark.udf.AppendArrayUdf;
import com.ericsson.oss.air.pm.stats.common.spark.udf.CalculateMedianValue;
import com.ericsson.oss.air.pm.stats.common.spark.udf.CalculatePercentileBinValue;
import com.ericsson.oss.air.pm.stats.common.spark.udf.CalculatePercentileValue;
import com.ericsson.oss.air.pm.stats.common.spark.udf.CalculateWeightedAverageUdf;
import com.ericsson.oss.air.pm.stats.common.spark.udf.FdnParseUdf;
import com.ericsson.oss.air.pm.stats.common.spark.udf.UpdateNullTimeAdvancedKpisUdf;
import com.ericsson.oss.air.pm.stats.common.spark.udf.internal.TruncateToFifteenMinute;

import org.apache.spark.sql.UDFRegistration;
import org.apache.spark.sql.catalyst.analysis.CustomFunctionRegistryProviders;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.DataTypes;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CustomFunctionRegistryProviderTest {
    ArgumentCaptor<ArrayType> arrayTypeArgumentCaptor = ArgumentCaptor.forClass(ArrayType.class);

    @Test
    void shouldRegisterUserDefinedFunctions() {
        final UDFRegistration udfRegistrationMock = mock(UDFRegistration.class);

        CustomFunctionRegistryProviders.register(udfRegistrationMock);

        verify(udfRegistrationMock).register(eq("ARRAY_INDEX_SUM"), any(ArrayIndexSumUdaf.class));
        verify(udfRegistrationMock).register(eq(ArrayIndexSumUdaf.NAME), any(ArrayIndexSumUdaf.class));
        verify(udfRegistrationMock).register(eq(DoubleArrayIndexSum.NAME), any(DoubleArrayIndexSum.class));
        verify(udfRegistrationMock).register(eq(LongArrayIndexSum.NAME), any(LongArrayIndexSum.class));
        verify(udfRegistrationMock).register(eq(PERCENTILE_INDEX_80_UDAF), any(PercentileIndexUdaf.class));
        verify(udfRegistrationMock).register(eq(PERCENTILE_INDEX_90_UDAF), any(PercentileIndexUdaf.class));

        verify(udfRegistrationMock).register(
                eq(TruncateToFifteenMinute.TRUNCATE_TO_FIFTEEN_MINUTES),
                any(TruncateToFifteenMinute.class),
                eq(DataTypes.TimestampType)
        );

        verify(udfRegistrationMock).register(
                eq(CalculatePercentileBinValue.NAME),
                any(CalculatePercentileBinValue.class),
                eq(DataTypes.DoubleType)
        );

        verify(udfRegistrationMock).register(
                eq(CalculatePercentileValue.NAME),
                any(CalculatePercentileValue.class),
                eq(DataTypes.DoubleType)
        );

        verify(udfRegistrationMock).register(
                eq(CalculateMedianValue.NAME),
                any(CalculateMedianValue.class),
                eq(DataTypes.DoubleType)
        );

        verify(udfRegistrationMock).register(
            eq(AddValueToArrayUdf.LONG_NAME),
            any(AddValueToArrayUdf.class),
            arrayTypeArgumentCaptor.capture()
        );
        Assertions.assertThat(arrayTypeArgumentCaptor.getValue().elementType()).isEqualTo(DataTypes.LongType);


        verify(udfRegistrationMock).register(
                eq(AddValueToArrayUdf.DOUBLE_NAME),
                any(AddValueToArrayUdf.class),
                arrayTypeArgumentCaptor.capture()
        );
        Assertions.assertThat(arrayTypeArgumentCaptor.getValue().elementType()).isEqualTo(DataTypes.DoubleType);

        verify(udfRegistrationMock).register(
                eq(AddValueToArrayUdf.INTEGER_NAME),
                any(AddValueToArrayUdf.class),
                arrayTypeArgumentCaptor.capture()
        );
        Assertions.assertThat(arrayTypeArgumentCaptor.getValue().elementType()).isEqualTo(DataTypes.IntegerType);

        verify(udfRegistrationMock).register(
            eq(CalculateWeightedAverageUdf.NAME),
            any(CalculateWeightedAverageUdf.class),
            eq(DataTypes.DoubleType)
        );

        verify(udfRegistrationMock).register(
                eq(UpdateNullTimeAdvancedKpisUdf.NAME),
                any(UpdateNullTimeAdvancedKpisUdf.class),
                arrayTypeArgumentCaptor.capture()
        );
        Assertions.assertThat(arrayTypeArgumentCaptor.getValue().elementType()).isEqualTo(DataTypes.IntegerType);

        verify(udfRegistrationMock).register(
                eq(AppendArrayUdf.INTEGER_NAME),
                any(AppendArrayUdf.class),
                arrayTypeArgumentCaptor.capture()
        );
        Assertions.assertThat(arrayTypeArgumentCaptor.getValue().elementType()).isEqualTo(DataTypes.IntegerType);

        verify(udfRegistrationMock).register(
            eq(AppendArrayUdf.LONG_NAME),
            any(AppendArrayUdf.class),
            arrayTypeArgumentCaptor.capture()
        );
        Assertions.assertThat(arrayTypeArgumentCaptor.getValue().elementType()).isEqualTo(DataTypes.LongType);

        verify(udfRegistrationMock).register(
                eq(AppendArrayGivenCardinalityAndLimitUdf.DOUBLE_NAME),
                any(AppendArrayGivenCardinalityAndLimitUdf.class),
                arrayTypeArgumentCaptor.capture()
        );
        Assertions.assertThat(arrayTypeArgumentCaptor.getValue().elementType()).isEqualTo(DataTypes.DoubleType);

        verify(udfRegistrationMock).register(
                eq(AppendArrayGivenCardinalityAndLimitUdf.INT_NAME),
                any(AppendArrayGivenCardinalityAndLimitUdf.class),
                arrayTypeArgumentCaptor.capture()
        );
        Assertions.assertThat(arrayTypeArgumentCaptor.getValue().elementType()).isEqualTo(DataTypes.IntegerType);

        verify(udfRegistrationMock).register(
                eq(AppendArrayGivenCardinalityAndLimitUdf.LONG_NAME),
                any(AppendArrayGivenCardinalityAndLimitUdf.class),
                arrayTypeArgumentCaptor.capture()
        );
        Assertions.assertThat(arrayTypeArgumentCaptor.getValue().elementType()).isEqualTo(DataTypes.LongType);

        verify(udfRegistrationMock).register(
                eq(FdnParseUdf.NAME),
                any(FdnParseUdf.class),
                eq(DataTypes.StringType)
        );

        verifyNoMoreInteractions(udfRegistrationMock);
    }
}
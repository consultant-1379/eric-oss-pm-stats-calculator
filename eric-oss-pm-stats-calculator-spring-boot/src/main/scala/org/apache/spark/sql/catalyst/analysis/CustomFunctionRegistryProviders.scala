/*
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

package org.apache.spark.sql.catalyst.analysis

import com.ericsson.oss.air.pm.stats.common.spark.udaf.PercentileIndexUdaf.{PERCENTILE_INDEX_80_UDAF, PERCENTILE_INDEX_90_UDAF}
import com.ericsson.oss.air.pm.stats.common.spark.udaf.{ArrayIndexSumUdaf, DoubleArrayIndexSum, LongArrayIndexSum, PercentileIndexUdaf}
import com.ericsson.oss.air.pm.stats.common.spark.udf._
import com.ericsson.oss.air.pm.stats.common.spark.udf.internal.TruncateToFifteenMinute
import org.apache.spark.sql.UDFRegistration
import org.apache.spark.sql.types.DataTypes

object CustomFunctionRegistryProviders {

  def register(udfRegistration: UDFRegistration): Unit = {
    functionRegister("ARRAY_INDEX_SUM", (functionName: String) => udfRegistration.register( // NOSONAR
      functionName, new ArrayIndexSumUdaf))
    functionRegister(ArrayIndexSumUdaf.NAME, (functionName: String) => udfRegistration.register( // NOSONAR
      functionName, new ArrayIndexSumUdaf))
    functionRegister(DoubleArrayIndexSum.NAME, (functionName: String) => udfRegistration.register( // NOSONAR
      functionName, new DoubleArrayIndexSum))
    functionRegister(LongArrayIndexSum.NAME, (functionName: String) => udfRegistration.register( // NOSONAR
      functionName, new LongArrayIndexSum))
    functionRegister(PERCENTILE_INDEX_80_UDAF, (functionName: String) => udfRegistration.register( // NOSONAR
      functionName, new PercentileIndexUdaf(80.0)))
    functionRegister(PERCENTILE_INDEX_90_UDAF, (functionName: String) => udfRegistration.register( // NOSONAR
      functionName, new PercentileIndexUdaf(90.0)))

    functionRegister(TruncateToFifteenMinute.TRUNCATE_TO_FIFTEEN_MINUTES, (functionName: String) => udfRegistration.register(functionName, new TruncateToFifteenMinute, DataTypes.TimestampType))
    functionRegister(CalculatePercentileBinValue.NAME, (functionName: String) => udfRegistration.register(functionName, new CalculatePercentileBinValue, DataTypes.DoubleType))
    functionRegister(CalculatePercentileValue.NAME, (functionName: String) => udfRegistration.register(functionName, new CalculatePercentileValue, DataTypes.DoubleType))
    functionRegister(CalculateMedianValue.NAME, (functionName: String) => udfRegistration.register(functionName, new CalculateMedianValue, DataTypes.DoubleType))
    functionRegister(AddValueToArrayUdf.LONG_NAME, (functionName: String) => udfRegistration.register(functionName, new AddValueToArrayUdf[Number], DataTypes.createArrayType(DataTypes.LongType)))
    functionRegister(AddValueToArrayUdf.DOUBLE_NAME, (functionName: String) => udfRegistration.register(functionName, new AddValueToArrayUdf[Number], DataTypes.createArrayType(DataTypes.DoubleType)))
    functionRegister(AddValueToArrayUdf.INTEGER_NAME, (functionName: String) => udfRegistration.register(functionName, new AddValueToArrayUdf[Number], DataTypes.createArrayType(DataTypes.IntegerType)))
    functionRegister(UpdateNullTimeAdvancedKpisUdf.NAME, (functionName: String) => udfRegistration.register(functionName, new UpdateNullTimeAdvancedKpisUdf, DataTypes.createArrayType(DataTypes.IntegerType)))
    functionRegister(AppendArrayUdf.INTEGER_NAME, (functionName: String) => udfRegistration.register(functionName, new AppendArrayUdf, DataTypes.createArrayType(DataTypes.IntegerType)))
    functionRegister(AppendArrayUdf.LONG_NAME, (functionName: String) => udfRegistration.register(functionName, new AppendArrayUdf, DataTypes.createArrayType(DataTypes.LongType)))
    functionRegister(AppendArrayGivenCardinalityAndLimitUdf.DOUBLE_NAME, (functionName: String) => udfRegistration.register(functionName, new AppendArrayGivenCardinalityAndLimitUdf, DataTypes.createArrayType(DataTypes.DoubleType)))
    functionRegister(AppendArrayGivenCardinalityAndLimitUdf.INT_NAME, (functionName: String) => udfRegistration.register(functionName, new AppendArrayGivenCardinalityAndLimitUdf, DataTypes.createArrayType(DataTypes.IntegerType)))
    functionRegister(AppendArrayGivenCardinalityAndLimitUdf.LONG_NAME, (functionName: String) => udfRegistration.register(functionName, new AppendArrayGivenCardinalityAndLimitUdf, DataTypes.createArrayType(DataTypes.LongType)))
    functionRegister(FdnParseUdf.NAME, (functionName: String) => udfRegistration.register(functionName, new FdnParseUdf, DataTypes.StringType))
    functionRegister(CalculateWeightedAverageUdf.NAME, (functionName: String) => udfRegistration.register(functionName, new CalculateWeightedAverageUdf, DataTypes.DoubleType))
  }

  private def functionRegister(functionName: String, functionRegister: FunctionRegister): Unit = {
    functionRegister.register(functionName)
  }

  private trait FunctionRegister {
    def register(functionName: String): Unit
  }

}


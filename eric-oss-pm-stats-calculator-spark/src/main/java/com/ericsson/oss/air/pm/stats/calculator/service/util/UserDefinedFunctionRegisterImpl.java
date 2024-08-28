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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.analysis.CustomFunctionRegistryProviders;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserDefinedFunctionRegisterImpl {
    public void register(@NonNull final SparkSession sparkSession) {
        CustomFunctionRegistryProviders.register(sparkSession.sqlContext().udf());
    }
}

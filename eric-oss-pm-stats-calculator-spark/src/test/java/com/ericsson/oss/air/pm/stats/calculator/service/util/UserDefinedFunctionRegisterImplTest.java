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

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.analysis.CustomFunctionRegistryProviders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class UserDefinedFunctionRegisterImplTest {
    UserDefinedFunctionRegisterImpl objectUnderTest = new UserDefinedFunctionRegisterImpl();

    @Test
    void shouldRegisterUserDefinedFunctions(@Mock(answer = RETURNS_DEEP_STUBS) final SparkSession sparkSessionMock) {
        try (final MockedStatic<CustomFunctionRegistryProviders> functionRegistrarsMockedStatic = mockStatic(CustomFunctionRegistryProviders.class)) {
            objectUnderTest.register(sparkSessionMock);

            functionRegistrarsMockedStatic.verify(() -> CustomFunctionRegistryProviders.register(sparkSessionMock.sqlContext().udf()));
        }
    }
}
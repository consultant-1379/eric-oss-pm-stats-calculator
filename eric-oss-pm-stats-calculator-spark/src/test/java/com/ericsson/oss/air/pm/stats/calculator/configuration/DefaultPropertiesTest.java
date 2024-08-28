/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.SparkSession.Builder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultPropertiesTest {

    @CsvSource({"1, validate", "2, none"})
    @ParameterizedTest(name = "[{index}] value ''{0}'' configured to: ''{1}''")
    void shouldObtainDefaultProperties(final String value, final String expected) {
        try (final MockedStatic<SparkSession> sparkSessionMockedStatic = Mockito.mockStatic(SparkSession.class)) {
            final Builder builderMock = mock(Builder.class, RETURNS_DEEP_STUBS);
            final SparkConf sparkConfMock = mock(SparkConf.class);

            sparkSessionMockedStatic.when(SparkSession::builder).thenReturn(builderMock);

            when(builderMock.getOrCreate().sparkContext().getConf()).thenReturn(sparkConfMock);
            when(sparkConfMock.get("spark.calculation.counter")).thenReturn(value);

            final Properties actual = DefaultProperties.defaultProperties();
            assertThat(actual).containsEntry("spring.jpa.hibernate.ddl-auto", expected);
        }
    }

}
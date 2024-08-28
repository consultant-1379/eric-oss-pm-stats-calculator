/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration.property;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties.KafkaProperties;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.converter.StringToDurationConverter;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

class CalculationPropertiesTest {

    @Nested
    @ExtendWith(OutputCaptureExtension.class)
    class CaptureOutput {
        @Test
        void shouldVerifyStateLoggingAfterPropertiesSet(final CapturedOutput capturedOutput) {
            final CalculationProperties calculationProperties = new CalculationProperties(
                    Duration.ZERO,
                    Duration.ZERO,
                    Duration.ZERO,
                    "indexedNumericPartitionColumns",
                    false,
                    4,
                    "http://schemaregistry:8081",
                    new KafkaProperties(50, "messagebus-kf:9092")
            );

            calculationProperties.afterPropertiesSet();

            final String actual = capturedOutput.getOut();

            // As we have json loggers, whitespaces became somewhat unpredictable, hence only the content is important,
            // sometimes, this unit test fails on the pipeline, thus, changing the assertion.
            Assertions.assertThat(actual)
                    .contains("Calculation properties:")
                    .contains("scheduleIncrement = PT0S")
                    .contains("endOfExecutionOffset = PT0S")
                    .contains("maxLookBackPeriod = PT0S")
                    .contains("indexedNumericPartitionColumns = indexedNumericPartitionColumns")
                    .contains("partitionTableRead = false")
                    .contains("sparkParallelism = 4")
                    .contains("schemaRegistryUrl = http://schemaregistry:8081")
                    .contains("kafka")
                    .contains("bucketSize = 50")
                    .contains("bootstrapServers = messagebus-kf:9092");
        }
    }

    @MethodSource("provideIsAllMaxLookBackPeriodData")
    @ParameterizedTest(name = "[{index}] MAX_LOOK_BACK_PERIOD of ''{0}'' is 'ALL' => ''{1}''")
    void shouldVerifyIsAllMaxLookBackPeriod(final Duration maxLookBackPeriod, final boolean expected) {
        final CalculationProperties calculationProperties = new CalculationProperties(
                null,
                null,
                maxLookBackPeriod,
                null,
                false,
                4,
                null,
                null
        );

        final boolean actual = calculationProperties.isAllMaxLookBackPeriod();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @MethodSource("provideIsPartitionDatasetLoadData")
    @ParameterizedTest(name = "[{index}] partitionTableRead: ''{0}'' and indexedNumericPartitionColumns: ''{1}'' ==> isPartitionDatasetLoad: ''{2}''")
    void shouldVerifyIsPartitionDatasetLoad(final boolean partitionTableRead, final String indexedNumericPartitionColumns, final boolean expected) {
        final CalculationProperties calculationProperties = new CalculationProperties(
                null,
                null,
                null,
                indexedNumericPartitionColumns,
                partitionTableRead,
                null,
                null,
                null
        );
        final boolean actual = calculationProperties.isPartitionDatasetLoad();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @Import(StringToDurationConverter.class)
    @DisplayName("Spring Context related tests")
    @PropertySource("classpath:application.properties")
    @EnableConfigurationProperties(CalculationProperties.class)
    class SpringContext {

        @Nested
        @DisplayName("When environment variables are set")
        @TestPropertySource(properties = "SCHEDULE_INCREMENT = 50m")
        @TestPropertySource(properties = "END_OF_EXECUTION_OFFSET = 50m")
        @TestPropertySource(properties = "MAX_LOOK_BACK_PERIOD = 50m")
        @TestPropertySource(properties = "INDEXED_NUMERIC_PARTITION_COLUMNS = agg_column_0")
        @TestPropertySource(properties = "PARTITION_TABLE_READ = true")
        @TestPropertySource(properties = "SPARK_PARALLELISM = 10")
        @TestPropertySource(properties = "KAFKA_BUCKET_SIZE = 50")
        @TestPropertySource(properties = "SCHEMA_REGISTRY_URL = http://schemaregistry:8081")
        class WhenEnvironmentVariableSet {
            @Autowired CalculationProperties calculationProperties;

            @Test
            void scheduleIncrement() {
                Assertions.assertThat(calculationProperties.getScheduleIncrement()).isEqualTo(Duration.ofMinutes(50));
            }

            @Test
            void endOfExecutionOffset() {
                Assertions.assertThat(calculationProperties.getEndOfExecutionOffset()).isEqualTo(Duration.ofMinutes(50));
            }

            @Test
            void maxLookBackPeriod() {
                Assertions.assertThat(calculationProperties.getMaxLookBackPeriod()).isEqualTo(Duration.ofMinutes(50));
            }

            @Test
            void indexNumericPartitionColumns() {
                Assertions.assertThat(calculationProperties.isPartitionColumnProvided()).isTrue();
                Assertions.assertThat(calculationProperties.getIndexedNumericPartitionColumns()).isEqualTo("agg_column_0");
            }

            @Test
            void partitionTableRead() {
                Assertions.assertThat(calculationProperties.isPartitionTableRead()).isTrue();
            }

            @Test
            void sparkParallelism() {
                Assertions.assertThat(calculationProperties.getSparkParallelism()).isEqualTo(10);
            }

            @Test
            void bucketSize() {
                Assertions.assertThat(calculationProperties.getKafkaBucketSize()).isEqualTo(50);
            }

            @Test
            void schemaRegistryUrl() {
                Assertions.assertThat(calculationProperties.getSchemaRegistryUrl()).isEqualTo("http://schemaregistry:8081");
            }
        }

        @Nested
        @DisplayName("When environment variables are not set")
        class WhenEnvironmentVariableNotSet {
            @Autowired CalculationProperties calculationProperties;

            @Test
            void scheduleIncrement() {
                Assertions.assertThat(calculationProperties.getScheduleIncrement()).isEqualTo(Duration.ofMinutes(15));
            }

            @Test
            void endOfExecutionOffset() {
                Assertions.assertThat(calculationProperties.getEndOfExecutionOffset()).isEqualTo(Duration.ofMinutes(30));
            }

            @Test
            void maxLookBackPeriod() {
                Assertions.assertThat(calculationProperties.getMaxLookBackPeriod()).isEqualTo(Duration.ofDays(1));
            }

            @Test
            void indexNumericPartitionColumns() {
                Assertions.assertThat(calculationProperties.isPartitionColumnProvided()).isFalse();
                Assertions.assertThat(calculationProperties.getIndexedNumericPartitionColumns()).isEmpty();
            }

            @Test
            void partitionTableRead() {
                Assertions.assertThat(calculationProperties.isPartitionTableRead()).isFalse();
            }

            @Test
            void sparkParallelism() {
                Assertions.assertThat(calculationProperties.getSparkParallelism()).isEqualTo(4);
            }

            @Test
            void bucketSize() {
                Assertions.assertThat(calculationProperties.getKafkaBucketSize()).isEqualTo(100);
            }
        }

        @Nested
        @DisplayName("When special value is set for the Duration type")
        @TestPropertySource(properties = "MAX_LOOK_BACK_PERIOD=ALL")
        class StringDurationConverterApplied {
            @Autowired CalculationProperties calculationProperties;

            @Test
            void shouldApplyStringDurationConverter() {
                Assertions.assertThat(calculationProperties.getMaxLookBackPeriod()).isEqualTo(ChronoUnit.FOREVER.getDuration());
            }
        }
    }

    static Stream<Arguments> provideIsAllMaxLookBackPeriodData() {
        return Stream.of(
                Arguments.of(ChronoUnit.FOREVER.getDuration(), true),
                Arguments.of(Duration.ofSeconds(1), false)
        );
    }

    static Stream<Arguments> provideIsPartitionDatasetLoadData() {
        return Stream.of(
                Arguments.of(true, "partitionColumn", true),
                Arguments.of(true, null, false),
                Arguments.of(true, StringUtils.EMPTY, false),
                Arguments.of(false, "partitionColumn", false),
                Arguments.of(false, null, false),
                Arguments.of(false, StringUtils.EMPTY, false)
        );
    }
}
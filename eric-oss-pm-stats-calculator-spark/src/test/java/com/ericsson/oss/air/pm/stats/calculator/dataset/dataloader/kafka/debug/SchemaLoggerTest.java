/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.debug;

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.WARN;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.AvroSchemaHelper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.SchemaBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import za.co.absa.abris.config.FromAvroConfig;

@ExtendWith(MockitoExtension.class)
class SchemaLoggerTest {
    @Mock AvroSchemaHelper avroSchemaHelperMock;

    @InjectMocks SchemaLogger objectUnderTest;

    @Nested
    @ExtendWith(OutputCaptureExtension.class)
    class LogSchema {

        @AfterEach
        void tearDown() {
            setLogLevel(objectUnderTest.getClass(), INFO);
        }

        @Test
        void shouldNotLogSchema_whenLogLevelIsNotDebug(@NonNull final CapturedOutput capturedOutput, @Mock final FromAvroConfig fromAvroConfigMock) {
            setLogLevel(objectUnderTest.getClass(), WARN);

            objectUnderTest.logSchema(fromAvroConfigMock);

            verify(avroSchemaHelperMock, never()).parseSchema(fromAvroConfigMock);

            // Other threads might be running async adding additional log entries here thus cannot check simply for isEmpty()
            Assertions.assertThat(capturedOutput.getAll()).doesNotContain(
                    "Schema registry contained schema for name"
            );
        }

        @Test
        void shouldLogSchema_whenLogLevelIsDebug(@NonNull final CapturedOutput capturedOutput, @Mock final FromAvroConfig fromAvroConfigMock) {
            setLogLevel(objectUnderTest.getClass(), DEBUG);

            when(avroSchemaHelperMock.parseSchema(fromAvroConfigMock)).thenReturn(
                    SchemaBuilder.record("test_record").namespace("test_namespace")
                                 .fields()
                                 .name("utc_timestamp")
                                 .type(LogicalTypes.timestampMillis().addToSchema(Schema.create(Type.LONG)))
                                 .noDefault()
                                 .endRecord()
            );

            objectUnderTest.logSchema(fromAvroConfigMock);

            verify(avroSchemaHelperMock).parseSchema(fromAvroConfigMock);

            Assertions.assertThat(capturedOutput.getAll()).containsSubsequence(
                    "Schema registry contained schema for name 'test_record' and namespace 'test_namespace' is:",
                    "\"name\" : \"test_record\",",
                    "\"namespace\" : \"test_namespace\","
            );
        }

        void setLogLevel(final Class<?> clazz, final Level level) {
            final Logger logger = (Logger) LoggerFactory.getLogger(clazz);
            logger.setLevel(level);
        }
    }
}
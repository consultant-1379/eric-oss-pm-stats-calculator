/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import lombok.SneakyThrows;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import za.co.absa.abris.config.FromAvroConfig;

class AvroSchemaHelperTest {
    AvroSchemaHelper objectUnderTest = new AvroSchemaHelper();

    @Test
    void shouldParseSchema() {
        final FromAvroConfig fromAvroConfigMock = mock(FromAvroConfig.class);
        final String avroSchema = readSchema("/avro/avro-schema.json");

        when(fromAvroConfigMock.schemaString()).thenReturn(avroSchema);

        final Schema actual = objectUnderTest.parseSchema(fromAvroConfigMock);

        verify(fromAvroConfigMock).schemaString();

        Assertions.assertThat(actual.getType()).isEqualTo(Type.RECORD);
        Assertions.assertThat(actual.getNamespace()).isEqualTo("kpi-service");
        Assertions.assertThat(actual.getName()).isEqualTo("new_fact_table_0");
        Assertions.assertThat(actual.getFields()).satisfiesExactly(
                field -> Assertions.assertThat(field.name()).isEqualTo("agg_column_0"),
                field -> Assertions.assertThat(field.name()).isEqualTo("ropBeginTime"),
                field -> Assertions.assertThat(field.name()).isEqualTo("ropEndTime"),
                field -> Assertions.assertThat(field.name()).isEqualTo("pmCounters")
        );
    }

    @SneakyThrows
    private String readSchema(final String name) {
        return IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream(name), name),
                StandardCharsets.UTF_8
        );
    }
}
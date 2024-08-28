/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DataIdentifierTest {
    @Test
    void whenNewDataIdentifierIsCreated_thenServerSchemaTopicsCanBeExtracted() {
        final String dataSpace = "dataSpace";
        final String category = "category";
        final String schema = "schema";

        DataIdentifier objectUnderTest = DataIdentifier.of(dataSpace, category, schema);

        Assertions.assertThat(objectUnderTest.isEmpty()).isFalse();
        Assertions.assertThat(objectUnderTest.dataSpace()).isEqualTo(dataSpace);
        Assertions.assertThat(objectUnderTest.category()).isEqualTo(category);
        Assertions.assertThat(objectUnderTest.schema()).isEqualTo(schema);
    }

    @Test
    void whenNewDataIdentifierIsCreatedWithNull_thenServerSchemaTopicsReturnsNull() {
        DataIdentifier objectUnderTest = DataIdentifier.of(null);

        Assertions.assertThat(objectUnderTest.isEmpty()).isTrue();
        Assertions.assertThat(objectUnderTest.dataSpace()).isNull();
        Assertions.assertThat(objectUnderTest.category()).isNull();
        Assertions.assertThat(objectUnderTest.schema()).isNull();
    }

    @Test
    void whenInvalidStringIsPassed_thenComponentsReturnNull() {
        DataIdentifier objectUnderTest = DataIdentifier.of("invalid_data_identifier");

        Assertions.assertThat(objectUnderTest.dataSpace()).isNull();
        Assertions.assertThat(objectUnderTest.category()).isNull();
        Assertions.assertThat(objectUnderTest.schema()).isNull();
    }
}
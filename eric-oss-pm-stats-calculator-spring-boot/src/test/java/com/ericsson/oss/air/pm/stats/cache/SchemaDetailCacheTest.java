/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;

import kpi.model.api.table.definition.api.InpDataIdentifierAttribute;
import kpi.model.simple.table.definition.optional.SimpleDefinitionInpDataIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SchemaDetailCacheTest {

    static DataIdentifier dataIdentifier = DataIdentifier.of("identifier");
    static final InpDataIdentifierAttribute DATA_IDENTIFIER = SimpleDefinitionInpDataIdentifier.of("dataSpace|category|schema");
    SchemaDetail schemaDetail = SchemaDetail.builder().withTopic("topic").build();
    SchemaDetail schemaDetailObject = SchemaDetail.builder().withTopic("topic").build();
    SchemaDetailCache objectUnderTest = new SchemaDetailCache();

    @BeforeEach
    void setUp() {
        objectUnderTest.put(dataIdentifier, schemaDetail);
        objectUnderTest.put(DATA_IDENTIFIER, schemaDetailObject);
    }

    @Test
    void shouldPut() {
        DataIdentifier key = DataIdentifier.of("test");
        objectUnderTest.put(key, schemaDetail);

        assertThat(objectUnderTest.hasValue(key)).isTrue();
    }

    @Test
    void shouldGet() {
        SchemaDetail actual = objectUnderTest.get(dataIdentifier);

        assertThat(actual).isEqualTo(schemaDetail);
    }

    @ParameterizedTest(name = "[{index}] key ''{0}'' hasValue in the cache: ''{1}''")
    @MethodSource("provideHasValueData")
    void shouldDecideIfHasValue(DataIdentifier key, boolean expected) {
        boolean actual = objectUnderTest.hasValue(key);

        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideHasValueData() {
        return Stream.of(
                Arguments.of(dataIdentifier, true),
                Arguments.of(DataIdentifier.of("notPresent"), false)
        );
    }

    @Test
    void testPut() {
        InpDataIdentifierAttribute key = SimpleDefinitionInpDataIdentifier.of("testDataSpace|testCategory|testSchema");
        objectUnderTest.put(key, schemaDetailObject);

        assertThat(objectUnderTest.hasValue(key)).isTrue();
    }

    @Test
    void testGet() {
        SchemaDetail actual = objectUnderTest.get(DATA_IDENTIFIER);

        assertThat(actual).isEqualTo(schemaDetailObject);
    }

    @ParameterizedTest(name = "[{index}] key ''{0}'' hasValue in the cache: ''{1}''")
    @MethodSource("provideTestHasValueData")
    void testHasValue(final InpDataIdentifierAttribute key, final boolean expected) {
        boolean actual = objectUnderTest.hasValue(key);

        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideTestHasValueData() {
        return Stream.of(
                Arguments.of(DATA_IDENTIFIER, true),
                Arguments.of(SimpleDefinitionInpDataIdentifier.of("dummyDataSpace|dummyCategory|dummySchema"), false)
        );
    }
}
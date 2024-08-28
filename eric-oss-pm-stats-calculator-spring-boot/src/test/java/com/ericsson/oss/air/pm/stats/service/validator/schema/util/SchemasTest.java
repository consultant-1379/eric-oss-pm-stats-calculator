/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.schema.util;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Parser;
import org.apache.avro.SchemaBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SchemasTest {

    @ParameterizedTest
    @MethodSource("provideComputeFieldsData")
    void shouldVerifyComputeFields(final Schema schema, final List<Field> expected) {
        final List<Field> actual = Schemas.computeFields(schema);
        Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @ParameterizedTest
    @MethodSource("provideIsPathContainedData")
    void shouldVerifyIsPathContained(final Schema schema, final JsonPath jsonPath, final boolean expected) {
        final boolean actual = Schemas.isPathContained(schema, jsonPath);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideIsPathContainedData() {
        final Schema schema = new Parser().parse(
                "{ " +
                        "  \"namespace\": \"kpi-service\", \"type\": \"record\", \"name\": \"fact_table\", \"fields\": [ " +
                        "    {\"name\": \"nodeFDN\", \"type\": \"int\"}, " +
                        "    {\"name\": \"ropBeginTime\", \"type\": \"string\"}, " +
                        "    {\"name\": \"ropEndTime\", \"type\": \"string\"}, " +
                        "    {\"name\": \"pmCounters\", \"type\": { " +
                        "      \"name\":\"pmCounters\", \"type\": \"record\", \"fields\": [ " +
                        "        {\"name\": \"integerArrayColumn0\", \"type\": [\"null\", {\"type\": \"array\", \"items\": \"int\" }]}, " +
                        "        {\"name\": \"integerArrayColumn1\", \"type\": [\"null\", {\"type\": \"array\", \"items\": \"int\" }]}, " +
                        "        {\"name\": \"integerArrayColumn2\", \"type\": [\"null\", {\"type\": \"array\", \"items\": \"int\" }]}, " +
                        "        {\"name\": \"floatArrayColumn0\", \"type\": [ \"null\", {\"type\": \"array\", \"items\": \"double\" }]}, " +
                        "        {\"name\": \"floatArrayColumn1\", \"type\": [ \"null\", {\"type\": \"array\", \"items\": \"double\" }]}, " +
                        "        {\"name\": \"floatArrayColumn2\", \"type\": [ \"null\", {\"type\": \"array\", \"items\": \"double\" }]}, " +
                        "        {\"name\": \"integerColumn0\", \"type\": \"int\"}, " +
                        "        {\"name\": \"floatColumn0\", \"type\": \"double\"} " +
                        "     ]" +
                        "    }}," +
                        "    {\"name\": \"pmCounters0\", \"type\": [\"null\", " +
                        "       {\"name\": \"pmMetricsSchema\", \"type\": \"record\", \"fields\": [" +
                        "           {\"name\": \"create_sm_context_resp_succ\", \"type\": [\"int\"]}," +
                        "           {\"name\": \"create_sm_context_req\", \"type\": [\"int\"]}" +
                        "    ]}]}," +
                        "    {\"name\": \"pmCounters1\", \"type\": [ " +
                        "       {\"name\": \"pmMetricsSchema1\", \"type\": \"record\", \"fields\": [" +
                        "           {\"name\": \"create_sm_context_resp_succ\", \"type\": [\"int\"]}," +
                        "           {\"name\": \"create_sm_context_req\", \"type\": [\"int\"]}" +
                        "       ]}," +
                        "       {\"name\": \"pmMetricsSchema2\", \"type\": \"record\", \"fields\": [" +
                        "           {\"name\": \"field_in_double_record_union\", \"type\": [\"int\"]}" +
                        "       ]}" +
                        "   ]}" +
                        "  ]" +
                        "} "
        );

        return Stream.of(
                arguments(schema, jsonPath(), false),
                arguments(schema, jsonPath("unknown-schema"), false),
                arguments(schema, jsonPath("fact_table", "pmCounters"), false),
                arguments(schema, jsonPath("fact_table", "unknown-field"), false),
                arguments(schema, jsonPath("fact_table", "NodeFdn"), false),
                arguments(schema, jsonPath("fact_table", "nodeFDN"), true),
                arguments(schema, jsonPath("fact_table", "pmCounters", "floatColumn0"), true),
                arguments(schema, jsonPath("fact_table", "pmCounters", "floatArrayColumn0"), true),
                arguments(schema, jsonPath("fact_table", "pmCounters", "floatArrayColumn0", "unknown-field"), false),
                arguments(schema, jsonPath("fact_table", "pmCounters0", "create_sm_context_req"), true),
                arguments(schema, jsonPath("fact_table", "pmCounters0", "create_sm_context_resp_succ"), true),
                arguments(schema, jsonPath("fact_table", "pmCounters0", "create_sm_context_req", "unknown-field"), false),
                arguments(schema, jsonPath("fact_table", "pmCounters0", "unknown-field"), false),
                arguments(schema, jsonPath("fact_table", "pmCounters1", "unknown-field"), false),
                arguments(schema, jsonPath("fact_table", "pmCounters1", "create_sm_context_req"), true),
                arguments(schema, jsonPath("fact_table", "pmCounters1", "field_in_double_record_union"), true)
        );
    }

    static Stream<Arguments> provideComputeFieldsData() {
        return Stream.of(
                arguments(
                        new Parser().parse(
                                "{ " +
                                        "  \"type\" : \"record\", \"name\" : \"userInfo\", \"namespace\" : \"my.example\", " +
                                        "  \"fields\" : [" +
                                        "    { \"name\" : \"age\", \"type\" : \"int\" }" +
                                        "  ] " +
                                        "} "
                        ),
                        List.of(field("age", "int"))
                ),
                arguments(
                        new Parser().parse(
                                "{ " +
                                        "  \"type\" : \"record\", \"name\" : \"userInfo\", \"namespace\" : \"my.example\", " +
                                        "  \"fields\" : [" +
                                        "    {\"name\" : \"username\", \"type\" : \"string\" }, " +
                                        "    {\"name\" : \"age\", \"type\" : \"int\" }, " +
                                        "    {\"name\" : \"phone\", \"type\" : \"string\" }," +
                                        "    {\"name\" : \"housenum\", \"type\" : \"string\"}, " +
                                        "    {\"name\" : \"street\", \"type\" : \"string\" }, " +
                                        "    {\"name\" : \"city\", \"type\" : \"string\" }, " +
                                        "    {\"name\" : \"state_province\", \"type\" : \"string\" }, " +
                                        "    {\"name\" : \"country\", \"type\" : \"string\" }, " +
                                        "    {\"name\" : \"zip\", \"type\" : \"string\"} " +
                                        "  ] " +
                                        "} "
                        ),
                        List.of(
                                field("username", "string"),
                                field("age", "int"),
                                field("phone", "string"),
                                field("housenum", "string"),
                                field("street", "string"),
                                field("city", "string"),
                                field("state_province", "string"),
                                field("country", "string"),
                                field("zip", "string")
                        )
                ),
                arguments(
                        new Parser().parse(
                                "{ " +
                                        "  \"type\" : \"record\", \"name\" : \"userInfo\", \"namespace\" : \"my.example\", " +
                                        "  \"fields\" : [" +
                                        "    {\"name\" : \"username\", \"type\" : \"string\" }, " +
                                        "    {\"name\" : \"age\", \"type\" : \"int\" }, " +
                                        "    {\"name\" : \"phone\", \"type\" : \"string\" }, " +
                                        "    {\"name\" : \"housenum\",  \"type\" : \"string\" }, " +
                                        "    {\"name\" : \"address\", \"type\" : { " +
                                        "      \"type\" : \"record\", \"name\" : \"mailing_address\", \"fields\" : [ " +
                                        "        {\"name\" : \"street\", \"type\" : \"string\" }, " +
                                        "        {\"name\" : \"city\", \"type\" : \"string\" }, " +
                                        "        {\"name\" : \"state_prov\", \"type\" : \"string\" }, " +
                                        "        {\"name\" : \"country\", \"type\" : \"string\" }, " +
                                        "        {\"name\" : \"zip\", \"type\" : \"string\" } " +
                                        "      ]" +
                                        "    }} " +
                                        "  ] " +
                                        "} "
                        ),
                        List.of(
                                field("username", "string"),
                                field("age", "int"),
                                field("phone", "string"),
                                field("housenum", "string"),
                                field("street", "string"),
                                field("city", "string"),
                                field("state_prov", "string"),
                                field("country", "string"),
                                field("zip", "string")
                        )
                ),
                arguments(
                        new Parser().parse(
                                "{ " +
                                        "  \"namespace\": \"kpi-service\", \"type\": \"record\", \"name\": \"fact_table\", \"fields\": [" +
                                        "    {\"name\": \"nodeFDN\", \"type\": \"int\"}, " +
                                        "    {\"name\": \"ropBeginTime\", \"type\": \"string\"}, " +
                                        "    {\"name\": \"ropEndTime\", \"type\": \"string\"}, " +
                                        "    {\"name\": \"pmCounters\", \"type\": {" +
                                        "      \"name\":\"pmCounters\", \"type\": \"record\", \"fields\": [" +
                                        "        {\"name\": \"integerArrayColumn0\", \"type\": [\"null\", {\"type\": \"array\", \"items\": \"int\" }]}," +
                                        "        {\"name\": \"integerArrayColumn1\", \"type\": [\"null\", {\"type\": \"array\", \"items\": \"int\" }]}," +
                                        "        {\"name\": \"integerArrayColumn2\", \"type\": [\"null\", {\"type\": \"array\", \"items\": \"int\" }]}," +
                                        "        {\"name\": \"floatArrayColumn0\", \"type\": [ \"null\", {\"type\": \"array\", \"items\": \"double\" }]}," +
                                        "        {\"name\": \"floatArrayColumn1\", \"type\": [ \"null\", {\"type\": \"array\", \"items\": \"double\" }]}," +
                                        "        {\"name\": \"floatArrayColumn2\", \"type\": [ \"null\", {\"type\": \"array\", \"items\": \"double\" }]}," +
                                        "        {\"name\": \"integerColumn0\", \"type\": \"int\"}," +
                                        "        {\"name\": \"floatColumn0\", \"type\": \"double\"}" +
                                        "     ]" +
                                        "    }}" +
                                        "  ]" +
                                        "} "
                        ),
                        List.of(
                                field("nodeFDN", "int"),
                                field("ropBeginTime", "string"),
                                field("ropEndTime", "string"),
                                new Field("integerArrayColumn0", SchemaBuilder.builder().unionOf().nullType().and().array().items().intType().endUnion()),
                                new Field("integerArrayColumn1", SchemaBuilder.builder().unionOf().nullType().and().array().items().intType().endUnion()),
                                new Field("integerArrayColumn2", SchemaBuilder.builder().unionOf().nullType().and().array().items().intType().endUnion()),
                                new Field("floatArrayColumn0", SchemaBuilder.builder().unionOf().nullType().and().array().items().doubleType().endUnion()),
                                new Field("floatArrayColumn1", SchemaBuilder.builder().unionOf().nullType().and().array().items().doubleType().endUnion()),
                                new Field("floatArrayColumn2", SchemaBuilder.builder().unionOf().nullType().and().array().items().doubleType().endUnion()),
                                field("integerColumn0", "int"),
                                field("floatColumn0", "double")
                        )
                )
        );
    }

    static JsonPath jsonPath(final String... parts) {
        return JsonPath.of(List.of(parts));
    }

    static Field field(final String name, final String type) {
        return new Field(name, SchemaBuilder.builder().type(type));
    }
}
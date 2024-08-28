/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.util;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.reference;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static scala.collection.JavaConverters.asScalaBuffer;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;

import org.apache.spark.sql.catalyst.analysis.UnresolvedAttribute;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import scala.collection.Seq;

class AttributeParsersTest {

    @Test
    void shouldParseJsonPath() {
        final List<String> parts = parts("schemaName", "recordName", "kpiName");
        final Set<JsonPath> actual = AttributeParsers.parseJsonPath(asScalaBuffer(parts));
        Assertions.assertThat(actual).containsOnly(JsonPath.of(parts));
    }

    @Nested
    class ParseColumn {
        @Test
        void shouldParseColumn() {
            final UnresolvedAttribute unresolvedAttribute = new UnresolvedAttribute(asScalaBuffer(parts("column")));
            final Column actual = AttributeParsers.parseColumn(unresolvedAttribute);
            Assertions.assertThat(actual).isEqualTo(column("column"));
        }

        @Test
        void shouldFailColumnParsing_whenPartsEmpty() {
            final UnresolvedAttribute unresolvedAttribute = new UnresolvedAttribute(asScalaBuffer(parts()));
            Assertions.assertThatThrownBy(() -> AttributeParsers.parseColumn(unresolvedAttribute))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("Expected a single column but found invalid parts '[]'");
        }

        @Test
        void shouldFailColumnParsing_whenPartsHasMoreThanOneElement() {
            final UnresolvedAttribute unresolvedAttribute = new UnresolvedAttribute(asScalaBuffer(parts("table"," column")));
            Assertions.assertThatThrownBy(() -> AttributeParsers.parseColumn(unresolvedAttribute))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("Expected a single column but found invalid parts '[table,  column]'");
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    class ParseRelation {
        @Test
        void shouldFailRelationParsingOnInvalidParts() {
            Assertions.assertThatThrownBy(() -> AttributeParsers.parseRelation(parts("1", "2", "3"), null))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage("'Relation' contains invalid parts '[1, 2, 3]'");
        }

        @ParameterizedTest
        @MethodSource("provideParseRelationData")
        void shouldParseRelation(final List<String> parts, final String alias, final Relation expected) {
            final Relation actual = AttributeParsers.parseRelation(parts, alias);
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideParseRelationData() {
            return Stream.of(
                    arguments(parts("table"), null, relation(null, table("table"), null)),
                    arguments(parts("table"), "alias", relation(null, table("table"), alias("alias"))),
                    arguments(parts("kpi_db", "table"), null, relation(KPI_DB, table("table"), null)),
                    arguments(parts("kpi_db", "table"), "alias", relation(KPI_DB, table("table"), alias("alias")))
            );
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    class ParseReference {
        @Test
        void shouldFailOnInvalidReference() {
            Assertions.assertThatThrownBy(() -> AttributeParsers.parseReferences(asScalaBuffer(parts("datasource", "table", "column", "random")), null))
                      .isInstanceOf(IllegalArgumentException.class)
                      .hasMessage(
                              "'UnresolvedAttribute' can have at most three parts <column> or <table>.<column> or <datasource>.<table>.<column> " +
                              "but had '4': '[datasource, table, column, random]'"
                      );
        }

        @ParameterizedTest
        @MethodSource("provideValidaTableColumnsData")
        void shouldParseValidTableColumns(final Seq<String> nameParts, final String alias, final Reference expected) {
            final Set<Reference> actual = AttributeParsers.parseReferences(nameParts, alias);
            Assertions.assertThat(actual).containsOnly(expected);
        }

        Stream<Arguments> provideValidaTableColumnsData() {
            final Column column = Column.of("column");
            return Stream.of(
                    arguments(
                            asScalaBuffer(parts("column")),
                            null,
                            reference(null, null, column, null)
                    ),
                    arguments(
                            asScalaBuffer(parts("table", "column")),
                            null,
                            reference(null, table("table"), column("column"), null)
                    ),
                    arguments(
                            asScalaBuffer(parts("table", "column")),
                            "alias",
                            reference(null, table("table"), column("column"), alias("alias"))
                    )
            );
        }
    }

    static List<String> parts(final String... parts) {
        return List.of(parts);
    }
}
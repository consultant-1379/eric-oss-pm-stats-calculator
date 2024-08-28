/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository._util;

import java.util.Arrays;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RepositoryHelpers {

    public static EmbeddedDatabase database(final String script, final String... scripts) {
        final EmbeddedDatabaseBuilder embeddedDatabaseBuilder = new EmbeddedDatabaseBuilder();

        embeddedDatabaseBuilder.setType(EmbeddedDatabaseType.H2);
        embeddedDatabaseBuilder.setName(String.format("%s;MODE=PostgreSQL", "kpi_service_db"));

        embeddedDatabaseBuilder.addScript(script); /* At least one script must be added */
        Arrays.stream(scripts).forEach(embeddedDatabaseBuilder::addScript);

        return embeddedDatabaseBuilder.build();
    }
}

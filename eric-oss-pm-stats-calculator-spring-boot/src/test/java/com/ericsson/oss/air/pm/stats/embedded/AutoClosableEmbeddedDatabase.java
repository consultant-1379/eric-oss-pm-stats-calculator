/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.embedded;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

@RequiredArgsConstructor(staticName = "of")
public final class AutoClosableEmbeddedDatabase implements EmbeddedDatabase, AutoCloseable {
    @Delegate
    private final EmbeddedDatabase embeddedDatabaseDelegate;

    @Override
    public void close() {
        embeddedDatabaseDelegate.shutdown();
    }
}

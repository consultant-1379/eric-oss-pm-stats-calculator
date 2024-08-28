/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness.window;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DataSource;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DefinitionName;

import lombok.NonNull;
import org.slf4j.Logger;

@ApplicationScoped
public class ReadinessWindowPrinter {
    private static final String LS = System.lineSeparator();

    private static final Comparator<ReadinessWindow> READINESS_WINDOW_COMPARATOR = Comparator.comparing(
            ReadinessWindow::getDataSource,
            Comparator.comparing(DataSource::source)
    );
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ReadinessWindowPrinter.class);

    public void logReadinessWindows(final String executionGroup,
                                    @NonNull final Map<DefinitionName, ? extends List<ReadinessWindow>> definitionNameToReadinessWindows) {
        final StringBuilder message = new StringBuilder("Readiness windows: ").append(singleQuote(executionGroup)).append(LS);

        definitionNameToReadinessWindows.forEach((definitionName, readinessWindows) -> {
            message.append("Definition ").append(singleQuote(definitionName.name())).append(" depends on:").append(LS);

            readinessWindows.stream().sorted(READINESS_WINDOW_COMPARATOR).forEach(
                    readinessWindow -> message.append('\t')
                            .append("Datasource ")
                            .append(singleQuote(readinessWindow.getDataSource().source()))
                            .append(" has window between ")
                            .append('[')
                            .append(readinessWindow.getEarliestCollectedData())
                            .append(" - ")
                            .append(readinessWindow.getLatestCollectedData())
                            .append(']')
                            .append(LS));
        });

        log.info("{}", message);
    }

    private static String singleQuote(final String text) {
        return String.format("'%s'", text);
    }
}

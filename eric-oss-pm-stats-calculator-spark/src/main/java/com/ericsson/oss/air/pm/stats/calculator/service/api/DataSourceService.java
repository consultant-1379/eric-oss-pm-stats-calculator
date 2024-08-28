/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.api;

import java.sql.Timestamp;

import com.ericsson.oss.air.pm.stats.calculator.util.Timestamps;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

public interface DataSourceService {
    /**
     * Find the <strong>MAX utc_timestamp</strong> for the provided {@link Database} and {@link Table}.
     * <br>
     * <strong>NOTE</strong>: If there is no <strong>MAX</strong> or something goes wrong then returns the {@link Timestamps#initialTimeStamp()}.
     *
     * @param database
     *         {@link Database} to connect to
     * @param table
     *         {@link Table} to check
     * @return max utc_timestamp or {@link Timestamps#initialTimeStamp()} if something goes wrong
     */
    Timestamp findMaxUtcTimestamp(Database database, Table table);

    /**
     * Find the <strong>MIN utc_timestamp</strong> for the provided {@link Database} and {@link Table}.
     * <br>
     * <strong>NOTE</strong>: If there is no <strong>MIN</strong> or something goes wrong then returns the {@link Timestamps#initialTimeStamp()}.
     *
     * @param database
     *         {@link Database} to connect to
     * @param table
     *         {@link Table} to check
     * @return min utc_timestamp or {@link Timestamps#initialTimeStamp()} if something goes wrong
     */

    Timestamp findMinUtcTimestamp(Database database, Table table);
}

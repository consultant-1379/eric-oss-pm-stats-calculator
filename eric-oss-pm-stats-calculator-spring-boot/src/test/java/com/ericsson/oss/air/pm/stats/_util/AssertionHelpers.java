/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats._util;

import java.sql.SQLException;
import java.util.Properties;

import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.test_utils.DatabasePropertiesMock;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssertionHelpers {

    public static void assertUncheckedSqlException(final ThrowingCallable shouldRaiseThrowable) {
        DatabasePropertiesMock.prepare("INVALID_JDBC_URL", new Properties(), () -> {
            Assertions.assertThatThrownBy(shouldRaiseThrowable)
                    .hasRootCauseExactlyInstanceOf(SQLException.class)
                    .isInstanceOf(UncheckedSqlException.class);
        });
    }
}

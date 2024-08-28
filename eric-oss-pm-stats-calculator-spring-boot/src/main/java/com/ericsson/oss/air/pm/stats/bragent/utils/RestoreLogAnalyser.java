/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.bragent.utils;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.Locale;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestoreLogAnalyser {
    private static final Pattern BACKUP_FAIL_LOG_PATTERN = Pattern.compile("connection to database \"[^ ]*\" failed", CASE_INSENSITIVE);

    private static final String RESTORE_WARN_MESSAGE = "warning: errors ignored";
    private static final String RESTORE_DUPLICATION_ERROR_MESSAGE = "ERROR:  duplicate key value violates";

    public static boolean isLogContainsWarning(@NonNull final String resultLog) {
        if (resultLog.toLowerCase(Locale.getDefault()).contains(RESTORE_WARN_MESSAGE)) {
            log.info("Log include the WARNING, please take care of it");
            return true;
        }

        return false;
    }

    public static boolean isLogContainsDuplicateKeyValueError(@NonNull final String resultLog) {
        if (resultLog.contains(RESTORE_DUPLICATION_ERROR_MESSAGE)) {
            log.error("Fail to restore and the log include the message: '{}'!", RESTORE_DUPLICATION_ERROR_MESSAGE);
            return true;
        }

        return false;
    }

    public static boolean isLogContainsFailedConnectionError(@NonNull final String resultLog) {
        return BACKUP_FAIL_LOG_PATTERN.matcher(resultLog).find();
    }
}

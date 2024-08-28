/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.log;

import java.util.Optional;

import com.ericsson.orchestration.common.utilities.auth.context.AccessToken;
import com.ericsson.orchestration.common.utilities.auth.context.AuthorizationContext;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class LoggerHandler {
    private static final String CATEGORY_KEY = "category";
    private static final String FACILITY_KEY = "facility";
    private static final String SUBJECT_KEY = "subject";
    private static final String AUDIT_LOG = "log audit";
    private static final String CATEGORY_PRIVACY = "IAM-privacy-management";

    /**
     * logAudit
     *
     * @param logger - logger instance
     * @param msg    - the message to print
     */
    public void logAudit(final Logger logger, final String msg) {
        MDC.put(CATEGORY_KEY, CATEGORY_PRIVACY);
        MDC.put(FACILITY_KEY, AUDIT_LOG);
        final Optional<AccessToken> accessToken = AuthorizationContext.getAccessToken();
        accessToken.ifPresent(token -> MDC.put(SUBJECT_KEY, token.getUserName()));
        logger.info("{}", msg);
        MDC.remove(SUBJECT_KEY);
        MDC.remove(FACILITY_KEY);
        MDC.remove(CATEGORY_KEY);
    }
}

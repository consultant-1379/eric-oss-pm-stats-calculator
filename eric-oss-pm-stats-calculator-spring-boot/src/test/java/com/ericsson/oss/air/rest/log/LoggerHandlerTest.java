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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

@Slf4j
class LoggerHandlerTest {

    LoggerHandler objectUnderTest = new LoggerHandler();

    @Test
    void logAudit() {
        final Logger loggerMock = mock(Logger.class);
        final String msg = "Log message 'something'";

        objectUnderTest.logAudit(loggerMock, msg);

        verify(loggerMock).info("{}", msg);
    }
}
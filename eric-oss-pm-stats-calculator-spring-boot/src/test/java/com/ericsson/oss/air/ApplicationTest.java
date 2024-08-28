/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air;


import static org.apache.commons.lang.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

class ApplicationTest {
    @Test
    void main() {
        try (final MockedConstruction<SpringApplication> springApplicationMockedConstruction = mockConstruction(SpringApplication.class)) {

            Application.main(EMPTY_STRING_ARRAY);
            final SpringApplication springApplicationMock = Iterables.getOnlyElement(springApplicationMockedConstruction.constructed());

            verify(springApplicationMock).run(EMPTY_STRING_ARRAY);
        }
    }

    @Test
    void contextLoads() {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        final Application application = new Application();

        when(applicationContext.getBean(Application.class)).thenReturn(application);
        final Application loadedApplication = applicationContext.getBean(Application.class);
        assertNotNull(loadedApplication);
    }
}
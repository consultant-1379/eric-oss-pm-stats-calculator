/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.beans.common;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

class BeanConfigCommonTest {
    @Test
    void contextLoads() {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        final BeanConfigCommon beanConfigCommon = new BeanConfigCommon();

        when(applicationContext.getBean(BeanConfigCommon.class)).thenReturn(beanConfigCommon);

        final BeanConfigCommon loadedBeanConfigCommon = applicationContext.getBean(BeanConfigCommon.class);
        assertNotNull(loadedBeanConfigCommon);
    }
}
/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.beans.war;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class BeanConfigWarTest {
    @InjectMocks
    BeanConfigWar objectUnderTest;

    @Test
    void contextLoads() {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        final BeanConfigWar beanConfigWar = new BeanConfigWar();

        when(applicationContext.getBean(BeanConfigWar.class)).thenReturn(beanConfigWar);

        final BeanConfigWar loadedBeanConfigWar = applicationContext.getBean(BeanConfigWar.class);
        assertNotNull(loadedBeanConfigWar);
    }

    @Test
    void restTemplate() {
        final RestTemplate actual = objectUnderTest.restTemplate();

        Assertions.assertThat(actual).isNotNull();
    }
}
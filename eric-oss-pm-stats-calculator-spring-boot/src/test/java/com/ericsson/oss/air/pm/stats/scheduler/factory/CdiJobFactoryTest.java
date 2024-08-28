/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.factory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.scheduler.activity.KpiCalculatorActivity;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class CdiJobFactoryTest {

    @Mock
    ApplicationContext applicationContextMock;

    @InjectMocks
    CdiJobFactory objectUnderTest;


    @Test
    @SneakyThrows
    void newJobSuccess() {
        final TriggerFiredBundle bundleMock = mock(TriggerFiredBundle.class);
        final JobDetail jobDetailMock = mock(JobDetail.class);
        final Class<? extends Job> jobClass = KpiCalculatorActivity.class;
        final Scheduler schedulerMock = mock(Scheduler.class);
        final Job jobMock = mock(Job.class);


        when(bundleMock.getJobDetail()).thenReturn(jobDetailMock);
        doReturn(jobClass).when(jobDetailMock).getJobClass();
        doReturn(jobMock).when(applicationContextMock).getBean(jobClass);

        final Job result = objectUnderTest.newJob(bundleMock, schedulerMock);

        verify(bundleMock, times(1)).getJobDetail();
        verify(jobDetailMock, times(1)).getJobClass();
        verify(applicationContextMock, times(1)).getBean(jobClass);

        assertNotNull(result);

    }

    @Test
    @SneakyThrows
    void newJobThrowsException() {
        final TriggerFiredBundle bundleMock = mock(TriggerFiredBundle.class);
        final JobDetail jobDetailMock = mock(JobDetail.class);
        final Class<? extends Job> jobClass = KpiCalculatorActivity.class;
        final Scheduler schedulerMock = mock(Scheduler.class);


        when(bundleMock.getJobDetail()).thenReturn(jobDetailMock);
        doReturn(jobClass).when(jobDetailMock).getJobClass();
        doThrow(NoSuchBeanDefinitionException.class).when(applicationContextMock).getBean(jobClass);

        assertThatThrownBy(() -> objectUnderTest.newJob(bundleMock, schedulerMock))
                .isInstanceOf(SchedulerException.class)
                .hasMessage(String.format("No job bean of type %s found.", jobClass));

        verify(bundleMock, times(1)).getJobDetail();
        verify(jobDetailMock, times(1)).getJobClass();
        verify(applicationContextMock, times(1)).getBean(jobClass);

    }

}
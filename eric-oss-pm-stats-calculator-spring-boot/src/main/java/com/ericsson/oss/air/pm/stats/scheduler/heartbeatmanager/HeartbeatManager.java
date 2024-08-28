/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.heartbeatmanager;

import static lombok.AccessLevel.PUBLIC;

import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Implements heartbeat counting methods
 */
@Getter
@Setter
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class HeartbeatManager {

    @Inject
    private EnvironmentValue<Integer> maxHeartbeatToWaitToRecalculateSimples;

    private AtomicInteger heartbeatCounter = new AtomicInteger();

    public void incrementHeartbeatCounter() {
        heartbeatCounter.incrementAndGet();
    }

    public void resetHeartBeatCounter() {
        heartbeatCounter.set(0);
    }

    public boolean canSimplesWait() {
        return heartbeatCounter.intValue() < maxHeartbeatToWaitToRecalculateSimples.value();
    }
}

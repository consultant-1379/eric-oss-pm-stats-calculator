/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/


package com.ericsson.oss.air.pm.stats.calculator.metrics;

import com.codahale.metrics.Gauge;

// TODO: Consider using com.codahale.metrics.DefaultSettableGauge
public class SettableGauge<T> implements Gauge<T> {
    private final Object lock = new Object();

    private T value;

    public SettableGauge() {
        this(null);
    }

    public SettableGauge(final T defaultValue) {
        value = defaultValue;
    }

    @Override
    public T getValue() {
        synchronized (lock) {
            return value;
        }
    }

    public void setValue(final T value) {
        synchronized (lock) {
            this.value = value;
        }
    }
}

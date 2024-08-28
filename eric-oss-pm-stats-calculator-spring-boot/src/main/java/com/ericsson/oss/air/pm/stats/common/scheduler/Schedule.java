/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.scheduler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class to represent a schedule within the {@code ActivityScheduler} class. A {@link Schedule} should contain information on when to execute
 * an {@link Activity}. Schedule specific data can be given to the {@link Activity} by passing it in the context {@link Map} in the {@link Schedule}
 * constructor.
 */
public class Schedule {

    private final String name;
    private final Map<String, Object> context = new HashMap<>();

    /**
     * An instance of {@link Schedule} <b>must</b> be given a name ({@link String}) and a context ({@link Map}). The name must be unique and the
     * responsibility is on the client to ensure this.
     *
     * @param name
     *            a unique name for the {@link Schedule}
     * @param context
     *            a {@link Map} containing information required when the {@link Activity} is run for the given {@link Schedule}. This enables time
     *            specific configuration of an {@link Activity}. For example, an {@link Activity} may want a different configuration when running on
     *            weekdays versus weekends. Values can be accessed within the {@link Activity#run} method of the extending class. If the same
     *            {@link Map} key is used in both the {@link Activity} context and the {@link Schedule} context then the {@link Schedule} context
     *            takes precedence.
     */
    public Schedule(final String name, final Map<String, Object> context) {
        this.name = name;
        this.context.putAll(context == null ? Collections.emptyMap() : context);
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }

    @Override
    public String toString() {
        return String.format("%s:: {name: '%s', context: '%s'}", getClass().getSimpleName(), name, context);
    }
}

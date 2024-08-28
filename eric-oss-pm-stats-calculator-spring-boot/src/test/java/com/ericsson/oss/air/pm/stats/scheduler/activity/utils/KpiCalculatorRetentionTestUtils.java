/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.activity.utils;

import java.lang.reflect.Method;

public final class KpiCalculatorRetentionTestUtils {

    private KpiCalculatorRetentionTestUtils() {

    }

    public static Method getMethod(final Class methodClass, final String methodName, final Class... classParameter) throws NoSuchMethodException {
        final Method method = methodClass.getDeclaredMethod(methodName, classParameter);
        method.setAccessible(true);
        return method;
    }
}

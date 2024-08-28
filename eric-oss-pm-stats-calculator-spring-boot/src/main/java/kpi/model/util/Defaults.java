/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Defaults {
    public static final boolean EXPORTABLE = false;
    public static final boolean REEXPORT_LATE_DATA = false;
    public static final int AGGREGATION_PERIOD = -1;
    public static final int DATA_LOOK_BACK_LIMIT = 7_200;
    public static final int DATA_RELIABILITY_OFFSET = 15;
}

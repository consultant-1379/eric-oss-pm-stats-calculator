/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(fluent = true)
@Builder(setterPrefix = "with")
public class ExecutionGroup {
    public static final String COLUMN_EXECUTION_GROUP_ID = "id";
    public static final String COLUMN_EXECUTION_GROUP_NAME = "execution_group";
    private final Integer id;
    private final String name;
}

/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.entity;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Builder(setterPrefix = "with")
public class TabularParameter {
    public static final String COLUMN_TABULAR_PARAMETERS_ID = "id";
    public static final String COLUMN_TABULAR_PARAMETERS_NAME = "name";
    private final Integer id;
    private final String name;
    private final UUID collectionId;
}

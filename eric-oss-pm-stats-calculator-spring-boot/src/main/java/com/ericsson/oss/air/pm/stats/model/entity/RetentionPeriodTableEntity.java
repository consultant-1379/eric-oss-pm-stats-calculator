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

import com.ericsson.oss.air.pm.stats.model.entity.api.RetentionPeriodEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class RetentionPeriodTableEntity implements RetentionPeriodEntity {
    private Long id;
    private UUID kpiCollectionId;
    private String tableName;
    private Integer retentionPeriodInDays;
}

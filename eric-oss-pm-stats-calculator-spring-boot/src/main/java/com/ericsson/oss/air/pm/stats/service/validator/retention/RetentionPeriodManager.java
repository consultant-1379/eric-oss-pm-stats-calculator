/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.retention;

import static lombok.AccessLevel.PUBLIC;

import java.time.Duration;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.repository.api.CollectionRetentionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.TableRetentionRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class RetentionPeriodManager {
    @Inject
    private TableRetentionRepository tableRetentionRepository;
    @Inject
    private CollectionRetentionRepository collectionRetentionRepository;

    @Inject
    private EnvironmentValue<Duration> retentionPeriodDays;

    public RetentionPeriodMemoizer retentionPeriodMemoizer() {
        return new RetentionPeriodMemoizer(tableRetentionRepository, collectionRetentionRepository, retentionPeriodDays);
    }
}

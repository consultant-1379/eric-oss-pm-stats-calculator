/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.resourcemanager.model.request;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

@Data
@Builder
public class KafkaUserPermissionsDto {
    private List<Pair<String, TopicConfigurationsDto>> topics;

    @Data
    @Builder
    public static class TopicConfigurationsDto {
        private String permission;
    }
}

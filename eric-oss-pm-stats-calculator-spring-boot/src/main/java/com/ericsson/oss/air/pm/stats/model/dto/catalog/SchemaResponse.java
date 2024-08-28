/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.dto.catalog;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class SchemaResponse {
    private String mediumType;
    private long mediumId;
    private String schemaName;
    private boolean isExternal;
    private String consumedDataSpace;
    private String consumedDataCategory;
    private String consumedDataProvider;
    private String consumedSchemaName;
    private String consumedSchemaVersion;
    private MessageSchema messageSchema;

    @Data
    @Builder
    @Jacksonized
    public static class MessageBus {
        private Long id;
        private String name;
        private String clusterName;
        private String nameSpace;
        private List<String> accessEndpoints;
    }

    @Data
    @Builder
    @Jacksonized
    public static class MessageDataTopic {
        private Long id;
        private String name;
        private String encoding;
        private MessageBus messageBus;

        public List<String> getAccessEndpoints() {
            return messageBus.getAccessEndpoints();
        }
    }

    @Data
    @Builder
    @Jacksonized
    public static class MessageSchema {
        private Long id;
        private String specificationReference;
        private MessageDataTopic messageDataTopic;
    }
}
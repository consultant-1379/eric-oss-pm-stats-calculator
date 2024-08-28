/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.loader.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaObjects {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDataTopic {
        private String encoding;
        private Long messageBusId;
        private String name;

        public static MessageDataTopic build(final Long messageBusId, final String name) {
            return new MessageDataTopic("AVRO", messageBusId, name);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageStatusTopic {
        private String encoding;
        private String name;
        private Long messageBusId;
        private String specificationReference;

        public static MessageStatusTopic build(final Long messageBusId, final String name) {
            return new MessageStatusTopic("AVRO", name, messageBusId, "streamed");
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageSchema {
        private String specificationReference;
    }
}
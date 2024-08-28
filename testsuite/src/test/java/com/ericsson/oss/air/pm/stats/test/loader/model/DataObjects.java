/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.loader.model;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataObjects {

    /**
     * POJO class representing the DataSpace from the DataCatalog.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSpace {
        private String name;
    }

    /**
     * POJO class representing the DataService from the DataCatalog.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataService {
        private String dataServiceName;

        /**
         * Gives back the same DataService Instance, which will be needed for every MessageSchema registration.
         */
        public static DataService build() {
            return new DataService("irrelevant");
        }
    }

    /**
     * POJO class representing the DataServiceInstance from the DataCatalog.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataServiceInstance {
        private String dataServiceInstanceName;
        private String controlEndPoint;
        private String consumedDataSpace;
        private String consumedDataCategory;
        private String consumedDataProvider;
        private String consumedSchemaName;
        private int consumedSchemaVersion;

        /**
         * Gives back the same DataServiceInstance Instance, which will be needed for every MessageSchema registration.
         */
        public static DataServiceInstance build() {
            return builder()
                    .dataServiceInstanceName("irrelevant")
                    .controlEndPoint("http://irrelevant/")
                    .consumedDataSpace("irrelevant")
                    .consumedDataCategory("irrelevant")
                    .consumedDataProvider("irrelevant")
                    .consumedSchemaName("irrelevant")
                    .consumedSchemaVersion(1)
                    .build();
        }
    }

    /**
     * POJO class representing the DataCategory from the DataCatalog.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataCategory {
        private String dataCategoryName;

        /**
         * Gives back the same DataCategory Instance, which will be needed for every MessageSchema registration.
         */
        public static DataCategory build(final String category) {
            return new DataCategory(category);
        }
    }

    /**
     * POJO class representing the DataProviderType from the DataCatalog.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataProviderType {
        private String providerTypeId;
        private String providerVersion;

        /**
         * Gives back the same DataProviderType Instance, which will be needed for every MessageSchema registration.
         */
        public static DataProviderType build() {
            return new DataProviderType("PM STATS Calculation Handling", "v1");
        }
    }

    /**
     * POJO class representing the DataType from the DataCatalog.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataType {
        private String mediumType;
        private String schemaName;
        private int schemaVersion;
        private String consumedDataSpace;
        private String consumedDataCategory;
        private String consumedDataProvider;
        private String consumedSchemaName;
        private int consumedSchemaVersion;

        /**
         * Gives back the same DataType Instance, which will be needed for every MessageSchema registration.
         */
        public static DataType build(final String schemaName) {
            return builder()
                    .mediumType("stream")
                    .schemaName(schemaName)
                    .schemaVersion(UUID.nameUUIDFromBytes(schemaName.getBytes(StandardCharsets.UTF_8)).version())  //btw this one is also irrelevant
                    .consumedDataSpace("irrelevant")
                    .consumedDataCategory("irrelevant")
                    .consumedDataProvider("irrelevant")
                    .consumedSchemaName("irrelevant")
                    .consumedSchemaVersion(1)
                    .build();
        }
    }
}
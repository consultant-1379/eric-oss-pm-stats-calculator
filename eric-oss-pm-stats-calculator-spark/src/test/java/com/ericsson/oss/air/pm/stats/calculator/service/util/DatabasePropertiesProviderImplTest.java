/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.calculator.service.util.api.DatabasePropertiesProvider;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;

import org.apache.spark.SparkConf;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scala.Tuple2;

@ExtendWith(MockitoExtension.class)
class DatabasePropertiesProviderImplTest {
    DatabasePropertiesProvider objectUnderTest = new DatabasePropertiesProviderImpl();

    @Test
    void shouldReadDatabaseProperties(@Mock final SparkConf sparkConfMock) {
        @SuppressWarnings("unchecked")
        final Tuple2<String, String>[] configurations = new Tuple2[]{
                new Tuple2<>("spark.jdbc.ext.serviceName1.user", "user1"),
                new Tuple2<>("spark.jdbc.ext.serviceName1.password", "password1"),
                new Tuple2<>("spark.jdbc.ext.serviceName2.user", "user2"),
                new Tuple2<>("invalid.key", "user1"),
        };

        when(sparkConfMock.getAll()).thenReturn(configurations);

        final DatabaseProperties actual = objectUnderTest.readDatabaseProperties(sparkConfMock);

        Assertions.assertThat(actual.entrySet()).hasSize(2).satisfiesExactlyInAnyOrder(databasePropertiesEntry -> {
            final Properties properties = new Properties();
            properties.setProperty("user", "user1");
            properties.setProperty("password", "password1");

            Assertions.assertThat(databasePropertiesEntry.getKey()).isEqualTo(new Database("serviceName1"));
            Assertions.assertThat(databasePropertiesEntry.getValue()).containsExactlyInAnyOrderEntriesOf(properties);
        }, databasePropertiesEntry -> {
            final Properties properties = new Properties();
            properties.setProperty("user", "user2");

            Assertions.assertThat(databasePropertiesEntry.getKey()).isEqualTo(new Database("serviceName2"));
            Assertions.assertThat(databasePropertiesEntry.getValue()).containsExactlyInAnyOrderEntriesOf(properties);
        });

        verify(sparkConfMock).getAll();
    }
}
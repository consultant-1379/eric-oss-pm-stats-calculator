/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup.COLUMN_EXECUTION_GROUP_NAME;
import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.database;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

@ExtendWith(MockitoExtension.class)
public class ExecutionGroupRepositoryImplTest {
    private static final String DATABASE_NAME = "kpi_service_db";
    private static final String DATABASE_TABLE_NAME = DATABASE_NAME + ".kpi.kpi_execution_groups";

    @Mock
    ExecutionGroupGenerator executionGroupGeneratorMock;

    @InjectMocks
    ExecutionGroupRepositoryImpl objectUnderTest;

    @Nested
    @DisplayName("Given an available database")
    class GivenAnAvailableDatabase {
        EmbeddedDatabase embeddedDatabase;

        @BeforeEach
        void setUp() {
            embeddedDatabase = database("sql/initialize_execution_group.sql");
        }

        @AfterEach
        void tearDown() {
            embeddedDatabase.shutdown();
        }

        @Nested
        @DisplayName("When database is populated")
        class WhenDatabaseIsPopulated {

            @Test
            void shouldSave(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock) throws SQLException {
                when(executionGroupGeneratorMock.generateOrGetExecutionGroup(kpiDefinitionEntityMock)).thenReturn("execution_group");

                final Long id = objectUnderTest.getOrSave(embeddedDatabase.getConnection(), kpiDefinitionEntityMock);

                verify(executionGroupGeneratorMock).generateOrGetExecutionGroup(kpiDefinitionEntityMock);

                final List<String> result = findAll();
                assertThat(id).isNotNull();
                assertThat(result).containsExactly("execution_group");
            }

            @Test
            void shouldReturnAlreadySavedData(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock) throws SQLException {
                when(executionGroupGeneratorMock.generateOrGetExecutionGroup(kpiDefinitionEntityMock)).thenReturn("execution_group");

                final Long expected = objectUnderTest.getOrSave(embeddedDatabase.getConnection(), kpiDefinitionEntityMock);
                final Long actual = objectUnderTest.getOrSave(embeddedDatabase.getConnection(), kpiDefinitionEntityMock);

                verify(executionGroupGeneratorMock, times(2)).generateOrGetExecutionGroup(kpiDefinitionEntityMock);

                assertThat(expected).isEqualTo(actual);
            }

            @Test
            void shouldNotSaveAnything(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock) throws SQLException {
                when(executionGroupGeneratorMock.generateOrGetExecutionGroup(kpiDefinitionEntityMock)).thenReturn(null);

                final Long actual = objectUnderTest.getOrSave(embeddedDatabase.getConnection(), kpiDefinitionEntityMock);

                verify(executionGroupGeneratorMock).generateOrGetExecutionGroup(kpiDefinitionEntityMock);

                assertThat(actual).isNull();
            }
        }

        private List<String> findAll() throws SQLException {
            final String sql = "SELECT execution_group FROM " + DATABASE_TABLE_NAME;
            try (final Connection connection = embeddedDatabase.getConnection();
                 final Statement stmt = connection.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {
                final List<String> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rs.getString(COLUMN_EXECUTION_GROUP_NAME));
                }
                return result;
            }
        }
    }

}

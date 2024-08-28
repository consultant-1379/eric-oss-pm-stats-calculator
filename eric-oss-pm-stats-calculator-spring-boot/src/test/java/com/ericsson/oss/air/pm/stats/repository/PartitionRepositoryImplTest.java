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

import static org.mockito.Mockito.spy;

import java.sql.SQLException;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.util.table.SqlTableModifierImpl;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PartitionRepositoryImplTest {

    @InjectMocks
    SqlTableModifierImpl sqlTableModifierSpy = spy(new SqlTableModifierImpl());

    @InjectMocks
    PartitionRepositoryImpl objectUnderTest;

    @Test
    void shouldThrowUncheckedSqlException_whenSomethingGoesWrong_onGetUniqueIndexForTablePartitions() {
        DriverManagerMock.prepareThrow(SQLException.class, connectionMock -> {
            Assertions.assertThatThrownBy(() -> objectUnderTest.findAllPartitionUniqueIndexes(Table.of("table")))
                    .hasRootCauseInstanceOf(SQLException.class)
                    .isInstanceOf(UncheckedSqlException.class);
        });
    }
}
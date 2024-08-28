/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState.STARTED;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.DimensionTable;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test-containers")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DimensionTablesRepositoryTest {
    static final UUID CALCULATION_ID = UUID.fromString("b2531c89-8513-4a88-aa1a-b99484321628");
    static final String TABLE_NAME = "cell_configuration_test_b2531c89";

    @Autowired DimensionTablesRepository objectUnderTest;
    @Autowired CalculationRepository calculationRepository;

    @Test
    void shouldFindDimensionTableNamesByCalculationId() {
        final Calculation calculation = Calculation.builder()
                                                   .id(CALCULATION_ID)
                                                   .timeCreated(LocalDateTime.now())
                                                   .executionGroup("execution-group")
                                                   .state(STARTED)
                                                   .kpiType(KpiType.ON_DEMAND)
                                                   .build();

        final DimensionTable dimensionTable = DimensionTable.builder()
                                                            .calculation(calculation)
                                                            .tableName(TABLE_NAME)
                                                            .build();

        calculationRepository.save(calculation);
        objectUnderTest.save(dimensionTable);

        final List<String> actual = objectUnderTest.findTableNamesByCalculationId(CALCULATION_ID);

        assertThat(actual).first().isEqualTo(TABLE_NAME);
    }
}

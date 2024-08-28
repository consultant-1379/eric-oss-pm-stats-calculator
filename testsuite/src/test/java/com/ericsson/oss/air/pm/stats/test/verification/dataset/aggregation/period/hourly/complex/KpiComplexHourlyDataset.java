/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.hourly.complex;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.hourly.complex.KpiComplexHourly;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.JsonArrayUtils;
import com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils;

/**
 * {@link AbstractDataset} implementation for the {@link KpiComplexHourly} domain.
 */
public final class KpiComplexHourlyDataset extends AbstractDataset {
    public static final KpiComplexHourlyDataset INSTANCE = new KpiComplexHourlyDataset();

    public KpiComplexHourlyDataset() {
        super("kpi_complex_60");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("agg_column_0",
                             "agg_column_1",
                             "aggregation_begin_time",
                             "aggregation_end_time",
                             "sum_integer_60_complex",
                             "sum_float_60_complex",
                             "integer_array_complex",
                             "sum_float_count_60",
                             "sum_integer_float_complex");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(KpiComplexHourly.builder()
                                         .aggColumn0(1)
                                         .aggColumn1(18)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(1)
                                         .aggColumn1(18)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(2)
                                         .aggColumn1(25)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                         .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(2)
                                         .aggColumn1(25)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(3)
                                         .aggColumn1(28)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                         .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(3)
                                         .aggColumn1(28)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(4)
                                         .aggColumn1(19)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(5)
                                         .aggColumn1(17)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(6)
                                         .aggColumn1(18)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(7)
                                         .aggColumn1(18)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(3)
                                         .integerArrayComplex(JsonArrayUtils.create(3, 6, 9, 12, 15))
                                         .sumIntegerFloatComplex(3.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(8)
                                         .aggColumn1(54)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(8)
                                         .aggColumn1(54)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                         .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(9)
                                         .aggColumn1(33)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(9)
                                         .aggColumn1(33)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2,4,6,8,10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(10)
                                         .aggColumn1(66)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                         .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                         .sumInteger60Complex(3)
                                         .integerArrayComplex(JsonArrayUtils.create(3, 6, 9, 12, 15))
                                         .sumIntegerFloatComplex(3.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(10)
                                         .aggColumn1(66)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(3)
                                         .integerArrayComplex(JsonArrayUtils.create(3, 6, 9, 12, 15))
                                         .sumIntegerFloatComplex(3.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(11)
                                         .aggColumn1(32)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(3)
                                         .integerArrayComplex(JsonArrayUtils.create(3, 6, 9, 12, 15))
                                         .sumIntegerFloatComplex(3.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(12)
                                         .aggColumn1(12)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(13)
                                         .aggColumn1(67)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(14)
                                         .aggColumn1(42)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(15)
                                         .aggColumn1(99)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(16)
                                         .aggColumn1(76)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                         .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(16)
                                         .aggColumn1(76)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(4)
                                         .integerArrayComplex(JsonArrayUtils.create(4, 8, 12, 16, 20))
                                         .sumIntegerFloatComplex(4.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(17)
                                         .aggColumn1(45)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(18)
                                         .aggColumn1(18)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(18)
                                         .aggColumn1(18)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(5)
                                         .integerArrayComplex(JsonArrayUtils.create(5,10,15,20,25))
                                         .sumIntegerFloatComplex(5.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(19)
                                         .aggColumn1(18)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(20)
                                         .aggColumn1(11)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(21)
                                         .aggColumn1(58)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                         .sumInteger60Complex(3)
                                         .integerArrayComplex(JsonArrayUtils.create(3, 6, 9, 12, 15))
                                         .sumIntegerFloatComplex(3.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(22)
                                         .aggColumn1(86)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                         .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(22)
                                         .aggColumn1(86)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(6)
                                         .integerArrayComplex(JsonArrayUtils.create(6, 12, 18, 24, 30))
                                         .sumIntegerFloatComplex(6.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(23)
                                         .aggColumn1(33)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(24)
                                         .aggColumn1(14)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(25)
                                         .aggColumn1(44)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                         .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(26)
                                         .aggColumn1(70)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(27)
                                         .aggColumn1(34)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(3)
                                         .integerArrayComplex(JsonArrayUtils.create(3, 6, 9, 12, 15))
                                         .sumIntegerFloatComplex(3.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(27)
                                         .aggColumn1(34)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(7)
                                         .integerArrayComplex(JsonArrayUtils.create(7,14,21,28,35))
                                         .sumIntegerFloatComplex(7.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(28)
                                         .aggColumn1(55)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(29)
                                         .aggColumn1(18)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(30)
                                         .aggColumn1(23)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(32)
                                         .aggColumn1(76)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(31)
                                         .aggColumn1(38)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(33)
                                         .aggColumn1(54)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                         .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(33)
                                         .aggColumn1(54)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(8)
                                         .integerArrayComplex(JsonArrayUtils.create(8, 16, 24, 32, 40))
                                         .sumIntegerFloatComplex(8.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(34)
                                         .aggColumn1(19)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(34)
                                         .aggColumn1(19)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(9)
                                         .integerArrayComplex(JsonArrayUtils.create(9,18,27,36,45))
                                         .sumIntegerFloatComplex(9.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(35)
                                         .aggColumn1(72)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                         .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(35)
                                         .aggColumn1(10)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(35)
                                         .aggColumn1(72)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(10)
                                         .integerArrayComplex(JsonArrayUtils.create(10, 20, 30, 40, 50))
                                         .sumIntegerFloatComplex(10.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                        .aggColumn0(36)
                                        .aggColumn1(89)
                                        .aggregationBeginTime(TimeUtils.daysAgo(2, 23))
                                        .aggregationEndTime(TimeUtils.daysAgo(1, 0))
                                        .sumInteger60Complex(1)
                                        .sumIntegerFloatComplex(1.2F)
                                        .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(37)
                                         .aggColumn1(21)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(2)
                                         .integerArrayComplex(JsonArrayUtils.create(2, 4, 6, 8, 10))
                                         .sumIntegerFloatComplex(2.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(38)
                                         .aggColumn1(59)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 1))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 2))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(39)
                                         .aggColumn1(56)
                                         .aggregationBeginTime(TimeUtils.daysAgo(2, 22))
                                         .aggregationEndTime(TimeUtils.daysAgo(2, 23))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build(),
                         KpiComplexHourly.builder()
                                         .aggColumn0(40)
                                         .aggColumn1(13)
                                         .aggregationBeginTime(TimeUtils.daysAgo(1, 0))
                                         .aggregationEndTime(TimeUtils.daysAgo(1, 1))
                                         .sumInteger60Complex(1)
                                         .sumIntegerFloatComplex(1.2F)
                                         .build())
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}
/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily;

import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.A_DAY_AGO;
import static com.ericsson.oss.air.pm.stats.test.verification.util.TimeUtils.TODAY;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.domain.aggregation.period.daily.OnDemandKpiParameterTypesDaily;
import com.ericsson.oss.air.pm.stats.test.verification.domain.api.DatabaseRow;
import com.ericsson.oss.air.pm.stats.test.verification.util.JsonArrayUtils;

public final class OnDemandKpiParameterTypesDailyDataset extends AbstractDataset {
    public static final OnDemandKpiParameterTypesDailyDataset INSTANCE = new OnDemandKpiParameterTypesDailyDataset();

    public OnDemandKpiParameterTypesDailyDataset() {
        super("kpi_parameter_types_1440");
    }

    @Override
    public List<String> getExpectedColumns() {
        return Arrays.asList("agg_column_0",
                             "aggregation_begin_time",
                             "aggregation_end_time",
                             "udf_param",
                             "udf_tabular_param");
    }

    @Override
    public List<String> getDataset() {
        return Stream.of(OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(1)
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(1)
                                                       .aggregationBeginTime(A_DAY_AGO)
                                                       .aggregationEndTime(TODAY)
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(2)
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(2)
                                                       .aggregationBeginTime(A_DAY_AGO)
                                                       .aggregationEndTime(TODAY)
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(3)
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(3)
                                                       .aggregationBeginTime(A_DAY_AGO)
                                                       .aggregationEndTime(TODAY)
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(4)
                                                       .aggregationBeginTime(A_DAY_AGO)
                                                       .aggregationEndTime(TODAY)
                                                       .udfTabularParameter(JsonArrayUtils.create(1, 2, 3, 4, 5, 6))
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(5)
                                                       .udfTabularParameter(JsonArrayUtils.create(1, 2, 3, 4, 5, 7))
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(6)
                                                       .aggregationBeginTime(A_DAY_AGO)
                                                       .aggregationEndTime(TODAY)
                                                       .udfTabularParameter(JsonArrayUtils.create(1, 2, 3, 4, 5, 8))
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(7)
                                                       .udfTabularParameter(JsonArrayUtils.create(1, 2, 3, 4, 5, 9))
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(8)
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(8)
                                                       .aggregationBeginTime(A_DAY_AGO)
                                                       .aggregationEndTime(TODAY)
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(9)
                                                       .build(),
                         OnDemandKpiParameterTypesDaily.builder()
                                                       .aggColumn0(9)
                                                       .aggregationBeginTime(A_DAY_AGO)
                                                       .aggregationEndTime(TODAY)
                                                       .build()
                     )
                     .map(DatabaseRow::convertToRow)
                     .collect(Collectors.toList());
    }
}

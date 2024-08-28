--
-- COPYRIGHT Ericsson 2022
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

CREATE TABLE IF NOT EXISTS kpi_rolling_aggregation_1440
(
    agg_column_0
    INTEGER,
    aggregation_begin_time
    TIMESTAMP
    NOT
    NULL,
    aggregation_end_time
    TIMESTAMP,
    rolling_sum_integer_1440
    INTEGER,
    rolling_max_integer_1440
    INTEGER
) PARTITION BY RANGE
(
    aggregation_begin_time
);

CREATE TABLE IF NOT EXISTS kpi_rolling_aggregation_1440_p_2022_05_26 PARTITION OF kpi_rolling_aggregation_1440
    FOR
    VALUES
    FROM
(
    '2022-05-26 00:00:00'
) TO
(
    '2022-05-27 00:00:00'
);

CREATE TABLE IF NOT EXISTS kpi_rolling_aggregation_1440_p_2022_05_27 PARTITION OF kpi_rolling_aggregation_1440
    FOR
    VALUES
    FROM
(
    '2022-05-27 00:00:00'
) TO
(
    '2022-05-28 00:00:00'
);

CREATE UNIQUE INDEX IF NOT EXISTS kpi_rolling_aggregation_1440_p_2022_05_26_ui
    ON kpi_rolling_aggregation_1440_p_2022_05_26 (agg_column_0, aggregation_begin_time);

CREATE UNIQUE INDEX IF NOT EXISTS kpi_rolling_aggregation_1440_p_2022_05_27_ui
    ON kpi_rolling_aggregation_1440_p_2022_05_27 (agg_column_0, aggregation_begin_time);
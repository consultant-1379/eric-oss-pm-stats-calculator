--
-- COPYRIGHT Ericsson 2024
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

CREATE SCHEMA IF NOT EXISTS kpi;

CREATE TABLE IF NOT EXISTS kpi.kpi_rolling_aggregation_1440
(
    agg_column_0
    BIGINT,
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

CREATE TABLE IF NOT EXISTS kpi."kpi.kpi_rolling_aggregation_1440_p_2022_05_26" PARTITION OF kpi.kpi_rolling_aggregation_1440
    FOR
    VALUES
    FROM
(
    '2022-05-26 00:00:00'
) TO
(
    '2022-05-27 00:00:00'
);

CREATE UNIQUE INDEX IF NOT EXISTS "kpi.kpi_rolling_aggregation_1440_p_2022_05_26_ui"
    ON kpi."kpi.kpi_rolling_aggregation_1440_p_2022_05_26" (agg_column_0, aggregation_begin_time);

--

CREATE TABLE IF NOT EXISTS kpi.primary_key_table
(
    column1
    INT,
    column2
    INT,
    column3
    INT,
    column4
    INT,
    PRIMARY
    KEY
(
    column1,
    column3
)
    );

CREATE TABLE IF NOT EXISTS kpi.external_table
(
    id
    SERIAL,
    data
    INT
);

CREATE TABLE IF NOT EXISTS kpi.tabular_parameters_table
(
    name
    CHAR
(
    50
),
    data INT
    );
--
-- COPYRIGHT Ericsson 2022
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

CREATE SCHEMA IF NOT EXISTS kpi;

CREATE TABLE IF NOT EXISTS kpi.readiness_log
(
    id
    SERIAL
    PRIMARY
    KEY,
    datasource
    VARCHAR
(
    255
) NOT NULL,
    collected_rows_count BIGINT NOT NULL,
    earliest_collected_data TIMESTAMP NOT NULL,
    latest_collected_data TIMESTAMP NOT NULL,
    kpi_calculation_id UUID NOT NULL
    );

CREATE TABLE IF NOT EXISTS kpi.complex_readiness_log
(
    simple_readiness_log_id
    INT,
    complex_calculation_id
    UUID
);
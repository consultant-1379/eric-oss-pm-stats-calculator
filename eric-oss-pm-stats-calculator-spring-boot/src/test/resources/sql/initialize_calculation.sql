--
-- COPYRIGHT Ericsson 2023
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

CREATE SCHEMA IF NOT EXISTS kpi;

CREATE TABLE IF NOT EXISTS kpi.kpi_calculation
(
    calculation_id
    UUID
    NOT
    NULL
    PRIMARY
    KEY,
    time_created
    TIMESTAMP
    NOT
    NULL,
    time_completed
    TIMESTAMP,
    state
    VARCHAR
(
    32
) NOT NULL,
    parameters TEXT,
    execution_group VARCHAR
(
    100
) NOT NULL,
    kpi_type VARCHAR
(
    32
) NOT NULL,
    collection_id UUID NOT NULL
    );

CREATE TABLE IF NOT EXISTS kpi.calculation_reliability
(
    id
    SERIAL
    PRIMARY
    KEY,
    calculation_start_time
    TIMESTAMP
    NOT
    NULL,
    reliability_threshold
    TIMESTAMP
    NOT
    NULL,
    calculation_id
    UUID
    NOT
    NULL,
    kpi_definition_id
    INT
    NOT
    NULL
);
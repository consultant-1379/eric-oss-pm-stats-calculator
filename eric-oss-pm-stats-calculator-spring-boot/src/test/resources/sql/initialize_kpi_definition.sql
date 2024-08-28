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

CREATE TABLE IF NOT EXISTS kpi.kpi_definition
(
    id
    SERIAL
    NOT
    NULL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    56
) NOT NULL UNIQUE,
    alias VARCHAR
(
    61
) NOT NULL,
    expression VARCHAR NOT NULL,
    object_type VARCHAR
(
    255
) NOT NULL,
    aggregation_type VARCHAR
(
    255
) NOT NULL,
    aggregation_period INTEGER NOT NULL,
    aggregation_elements VARCHAR
(
    255
) ARRAY NOT NULL,
    exportable BOOLEAN NOT NULL,
    filters VARCHAR
(
    255
) ARRAY,
    schema_data_space VARCHAR,
    schema_category VARCHAR,
    schema_name VARCHAR,
    execution_group_id INT,
    data_reliability_offset INTEGER,
    data_lookback_limit INTEGER,
    reexport_late_data BOOLEAN,
    time_deleted TIMESTAMP,
    schema_detail_id INT,
    collection_id UUID NOT NULL
    );
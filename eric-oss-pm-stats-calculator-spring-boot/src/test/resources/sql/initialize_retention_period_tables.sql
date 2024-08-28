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

CREATE TABLE IF NOT EXISTS kpi.retention_configurations_table_level
(
    id
    SERIAL
    PRIMARY
    KEY,
    kpi_collection_id
    UUID
    NOT
    NULL,
    table_name
    VARCHAR
(
    255
) NOT NULL,
    retention_period_in_days INT NOT NULL
    );

CREATE TABLE IF NOT EXISTS kpi.retention_configurations_collection_level
(
    id
    SERIAL
    PRIMARY
    KEY,
    kpi_collection_id
    UUID
    NOT
    NULL,
    retention_period_in_days
    INT
    NOT
    NULL
);

ALTER TABLE kpi.retention_configurations_table_level
    ADD CONSTRAINT uc_kpi_collection_id_table_name UNIQUE (kpi_collection_id, table_name);


ALTER TABLE kpi.retention_configurations_collection_level
    ADD CONSTRAINT uc_kpi_collection_id UNIQUE (kpi_collection_id);

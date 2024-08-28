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

CREATE TABLE IF NOT EXISTS kpi.kpi_execution_groups
(
    id
    SERIAL
    NOT
    NULL
    PRIMARY
    KEY,
    execution_group
    VARCHAR
    NOT
    NULL
    UNIQUE
);
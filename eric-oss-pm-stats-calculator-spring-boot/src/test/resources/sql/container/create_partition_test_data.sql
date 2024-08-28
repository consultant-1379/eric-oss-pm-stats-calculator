--
-- COPYRIGHT Ericsson 2024
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

CREATE SCHEMA not_kpi;

CREATE TABLE not_kpi.kpi_1440
(
);
CREATE TABLE kpi.kpi_60
(
    time date
) PARTITION BY RANGE (time);
CREATE TABLE kpi.kpi_60_partition PARTITION OF kpi.kpi_60 DEFAULT;
CREATE TABLE kpi.kpi_calculation
(
);
CREATE TABLE kpi.kpi_execution_groups
(
);
CREATE TABLE kpi.kpi_definition
(
);

CREATE VIEW kpi.kpi_60_view AS
SELECT *
FROM kpi.kpi_60;
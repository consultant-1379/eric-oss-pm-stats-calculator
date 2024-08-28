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

CREATE TABLE IF NOT EXISTS kpi.on_demand_tabular_parameters
(
    id
    SERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    56
) NOT NULL,
    collection_id UUID NOT NULL
    );

INSERT INTO kpi.on_demand_tabular_parameters(name, collection_id)
VALUES ('cell_params', '29dc1bbf-7cdf-421b-8fc9-e363889ada79');
INSERT INTO kpi.on_demand_tabular_parameters(name, collection_id)
VALUES ('cell_configuration', '29dc1bbf-7cdf-421b-8fc9-e363889ada79');
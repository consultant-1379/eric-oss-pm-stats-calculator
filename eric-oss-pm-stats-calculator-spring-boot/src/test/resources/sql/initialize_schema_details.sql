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

CREATE TABLE IF NOT EXISTS kpi.schema_details
(
    id        SERIAL PRIMARY KEY,
    topic     VARCHAR NOT NULL,
    namespace VARCHAR NOT NULL,
    CONSTRAINT unique_detail UNIQUE (topic, namespace)
);
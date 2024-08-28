--
-- COPYRIGHT Ericsson 2024
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

CREATE TABLE IF NOT EXISTS kpi.metric
(
    id     UUID DEFAULT gen_random_uuid() NOT NULL,
    name   VARCHAR(255)                   NOT NULL,
    "value" BIGINT
);

ALTER TABLE kpi.metric
    ADD CONSTRAINT metric_pk
        PRIMARY KEY (id);

ALTER TABLE kpi.metric
    ADD CONSTRAINT metric_name
        UNIQUE (name);

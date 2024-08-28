--
-- COPYRIGHT Ericsson 2024
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

ALTER TABLE kpi.schema_details DROP CONSTRAINT unique_detail;
ALTER TABLE kpi.schema_details DROP COLUMN kafka_server;
ALTER TABLE kpi.schema_details ADD CONSTRAINT unique_detail UNIQUE (topic, namespace);
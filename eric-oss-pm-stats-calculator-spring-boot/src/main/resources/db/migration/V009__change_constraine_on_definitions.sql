--
-- COPYRIGHT Ericsson 2024
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

ALTER TABLE kpi_definition DROP CONSTRAINT kpi_definition_name_key;
ALTER TABLE kpi_definition ADD CONSTRAINT kpi_definition_name_collection_id_key UNIQUE(name, collection_id);
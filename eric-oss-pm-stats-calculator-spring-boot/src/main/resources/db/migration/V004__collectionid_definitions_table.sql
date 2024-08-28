--
-- COPYRIGHT Ericsson 2024
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

ALTER TABLE kpi.kpi_definition ADD COLUMN collection_id UUID NOT NULL DEFAULT '29dc1bbf-7cdf-421b-8fc9-e363889ada79';
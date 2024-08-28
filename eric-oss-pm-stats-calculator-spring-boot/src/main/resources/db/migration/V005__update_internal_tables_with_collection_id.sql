--
-- COPYRIGHT Ericsson 2024
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

ALTER TABLE kpi.latest_processed_offsets ADD COLUMN collection_id UUID NOT NULL DEFAULT '29dc1bbf-7cdf-421b-8fc9-e363889ada79';
ALTER TABLE kpi.kpi_calculation ADD COLUMN collection_id UUID NOT NULL DEFAULT '29dc1bbf-7cdf-421b-8fc9-e363889ada79';
ALTER TABLE kpi.latest_source_data ADD COLUMN collection_id UUID NOT NULL DEFAULT '29dc1bbf-7cdf-421b-8fc9-e363889ada79';
ALTER TABLE kpi.readiness_log ADD COLUMN collection_id UUID NOT NULL DEFAULT '29dc1bbf-7cdf-421b-8fc9-e363889ada79';
ALTER TABLE kpi.calculation_reliability ADD COLUMN collection_id UUID NOT NULL DEFAULT '29dc1bbf-7cdf-421b-8fc9-e363889ada79';
ALTER TABLE kpi.dimension_tables ADD COLUMN collection_id UUID NOT NULL DEFAULT '29dc1bbf-7cdf-421b-8fc9-e363889ada79';
ALTER TABLE kpi.on_demand_parameters ADD COLUMN collection_id UUID NOT NULL DEFAULT '29dc1bbf-7cdf-421b-8fc9-e363889ada79';
ALTER TABLE kpi.on_demand_tabular_parameters ADD COLUMN collection_id UUID NOT NULL DEFAULT '29dc1bbf-7cdf-421b-8fc9-e363889ada79';
ALTER TABLE kpi.on_demand_definitions_per_calculation ADD COLUMN collection_id UUID NOT NULL DEFAULT '29dc1bbf-7cdf-421b-8fc9-e363889ada79';
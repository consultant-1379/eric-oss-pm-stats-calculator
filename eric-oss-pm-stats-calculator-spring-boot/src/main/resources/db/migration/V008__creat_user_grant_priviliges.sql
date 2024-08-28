--
-- COPYRIGHT Ericsson 2024
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--


-- Create read only user
CREATE ROLE kpi_service_user_ro WITH LOGIN;
GRANT USAGE ON SCHEMA kpi TO kpi_service_user_ro;
GRANT SELECT ON ALL TABLES IN SCHEMA kpi TO kpi_service_user_ro;
ALTER DEFAULT PRIVILEGES FOR ROLE kpi_service_user GRANT SELECT ON TABLES TO kpi_service_user_ro;
ALTER ROLE kpi_service_user_ro SET search_path to kpi;

-- Give ownership to kpi_service_user
ALTER TABLE kpi.kpi_definition OWNER TO kpi_service_user;
ALTER TABLE kpi.latest_source_data OWNER TO kpi_service_user;
ALTER TABLE kpi.kpi_calculation OWNER TO kpi_service_user;
ALTER TABLE kpi.kpi_execution_groups OWNER TO kpi_service_user;
ALTER TABLE kpi.latest_processed_offsets OWNER TO kpi_service_user;
ALTER TABLE kpi.readiness_log OWNER TO kpi_service_user;
ALTER TABLE kpi.calculation_reliability OWNER TO kpi_service_user;
ALTER TABLE kpi.on_demand_definitions_per_calculation OWNER TO kpi_service_user;
ALTER TABLE kpi.complex_readiness_log OWNER TO kpi_service_user;
ALTER TABLE kpi.schema_details OWNER TO kpi_service_user;
ALTER TABLE kpi.on_demand_parameters OWNER TO kpi_service_user;
ALTER TABLE kpi.on_demand_tabular_parameters OWNER TO kpi_service_user;
ALTER TABLE kpi.dimension_tables OWNER TO kpi_service_user;
ALTER TABLE kpi.retention_configurations_table_level OWNER TO kpi_service_user;
ALTER TABLE kpi.retention_configurations_collection_level OWNER TO kpi_service_user;
ALTER TABLE kpi.metric OWNER TO kpi_service_user;
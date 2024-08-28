--
-- COPYRIGHT Ericsson 2024
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--


-- Populating kpi definition
INSERT INTO kpi.kpi_definition (name, alias, expression, object_type, aggregation_type, aggregation_period,
                                aggregation_elements, exportable, filters, collection_id)
VALUES ('kpiDefinition3', 'alias2', 'expression1', 'objectType1', 'aggregationType1', 60, ARRAY['aggregationElement1',
        'aggregationElement2'], true, ARRAY['filter1', 'filter2'], '29dc1bbf-7cdf-421b-8fc9-e363889ada79');
INSERT INTO kpi.kpi_definition (name, alias, expression, object_type, aggregation_type, aggregation_period,
                                aggregation_elements, exportable, filters, collection_id)
VALUES ('kpiDefinition4', 'alias2', 'expression1', 'objectType1', 'aggregationType1', 60, ARRAY['aggregationElement1',
        'aggregationElement2'], true, ARRAY['filter1', 'filter2'], '29dc1bbf-7cdf-421b-8fc9-e363889ada79');

-- Populating kpi calculation
INSERT INTO kpi.kpi_calculation (calculation_id, time_created, state, execution_group, kpi_type, collection_id)
VALUES ('56e4365a-4567-4afb-b1e8-103ee4acbeb9', to_timestamp('03 Aug 2022 17:00:00', 'DD MON YYYY HH24:MI:SS'),
        'FINISHED', 'ON_DEMAND', 'ON_DEMAND', '29dc1bbf-7cdf-421b-8fc9-e363889ada79');

-- Populating ON_DEMAND definitions per calculations table
INSERT INTO kpi.on_demand_definitions_per_calculation (kpi_definition_id, calculation_id, collection_id)
VALUES (3, '56e4365a-4567-4afb-b1e8-103ee4acbeb9', '29dc1bbf-7cdf-421b-8fc9-e363889ada79');
INSERT INTO kpi.on_demand_definitions_per_calculation (kpi_definition_id, calculation_id, collection_id)
VALUES (4, '56e4365a-4567-4afb-b1e8-103ee4acbeb9', '29dc1bbf-7cdf-421b-8fc9-e363889ada79');
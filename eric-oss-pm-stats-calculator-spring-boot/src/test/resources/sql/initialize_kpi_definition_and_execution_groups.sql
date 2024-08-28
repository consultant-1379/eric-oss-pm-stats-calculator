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

CREATE TABLE IF NOT EXISTS kpi.kpi_execution_groups
(
    id
    SERIAL
    NOT
    NULL
    PRIMARY
    KEY,
    execution_group
    VARCHAR
    NOT
    NULL
    UNIQUE
);

INSERT INTO kpi.kpi_execution_groups (id, execution_group)
VALUES (1, 'executionGroup1');
INSERT INTO kpi.kpi_execution_groups (id, execution_group)
VALUES (2, 'executionGroup2');
INSERT INTO kpi.kpi_execution_groups (id, execution_group)
VALUES (3, 'executionGroup3');
INSERT INTO kpi.kpi_execution_groups (id, execution_group)
VALUES (4, 'COMPLEX');

INSERT INTO kpi.kpi_definition (name, alias, expression, object_type, aggregation_type, aggregation_period,
                                aggregation_elements, exportable, filters, schema_data_space, schema_category,
                                schema_name, execution_group_id, reexport_late_data, time_deleted, collection_id)
VALUES ('kpiDefinition1', 'alias1', 'expression1', 'objectType1', 'aggregationType1', 60, ARRAY['aggregationElement1',
        'aggregationElement2'], true, ARRAY['filter1', 'filter2'], 'dataSpace', 'category', 'schema', 1, false, NULL,
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       ('kpiDefinition2', 'alias1', 'expression1', 'objectType1', 'aggregationType1', 60, ARRAY['aggregationElement1',
        'aggregationElement2'], true, ARRAY['filter1', 'filter2'], 'dataSpace', 'category', 'schema', 1, false, NULL,
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       ('kpiDefinition3', 'alias1', 'expression1', 'objectType1', 'aggregationType1', 60, ARRAY['aggregationElement1',
        'aggregationElement2'], true, ARRAY['filter1', 'filter2'], 'dataSpace', 'category', 'schema', 1, false, NULL,
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       ('kpiDefinition4', 'alias1', 'expression1', 'objectType1', 'aggregationType1', 60, ARRAY['aggregationElement1',
        'aggregationElement2'], true, ARRAY['filter1', 'filter2'], 'dataSpace', 'category', 'schema', 2, false, NULL,
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       ('kpiDefinition5', 'alias1', 'expression1', 'objectType1', 'aggregationType1', 60, ARRAY['aggregationElement1',
        'aggregationElement2'], true, ARRAY['filter1', 'filter2'], 'dataSpace', 'category', 'schema', 3, false, NULL,
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       ('kpiDefinition6', 'onDemandAlias1', 'expression1', 'objectType1', 'aggregationType1', 60,
        ARRAY['aggregationElement1', 'aggregationElement2'], true, ARRAY['filter1', 'filter2'], null, null, null, null,
        false, NULL, '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       ('kpiDefinitionComplex1', 'complex', 'expression1', 'objectType1', 'aggregationType1', 60,
        ARRAY['aggregationElement1', 'aggregationElement2'], true, ARRAY['filter1', 'filter2'], null, null, null, 4,
        false, NULL, '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       ('kpiDefinition7', 'complex', 'expression1', 'objectType1', 'aggregationType1', 60, ARRAY['aggregationElement1',
        'aggregationElement2'], true, ARRAY['filter1', 'filter2'], null, null, null, 3, false, NULL,
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       ('kpiDefinition8', 'complex', 'expression1', 'objectType1', 'aggregationType1', 60, ARRAY['aggregationElement1',
        'aggregationElement2'], true, ARRAY['filter1', 'filter2'], null, null, null, 3, false, NULL,
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       ('kpiDefinition9', 'onDemandAlias1', 'expression1', 'objectType1', 'aggregationType1', 1440,
        ARRAY['aggregationElement1', 'aggregationElement2'], true, ARRAY['filter1', 'filter2'], null, null, null, null,
        false, NULL, '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       ('kpiDefinition10', 'onDemandAlias2', 'expression1', 'objectType1', 'aggregationType1', 60,
        ARRAY['aggregationElement1', 'aggregationElement2'], true, ARRAY['filter1', 'filter2'], null, null, null, null,
        false, NULL, '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       ('kpiDefinitionDeleted', 'aliasDeleted', 'expressionDeleted', 'objectTypeDeleted', 'aggregationTypeDeleted', 60,
        ARRAY['aggregationElementDeleted'], true, ARRAY['filterDeleted'], NULL, NULL, NULL, 1, false, now(),
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79');

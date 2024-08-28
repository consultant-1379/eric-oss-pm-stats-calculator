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

CREATE TABLE IF NOT EXISTS kpi.calculation_reliability
(
    id
    SERIAL
    PRIMARY
    KEY,
    calculation_start_time
    TIMESTAMP
    NOT
    NULL,
    reliability_threshold
    TIMESTAMP
    NOT
    NULL,
    calculation_id
    UUID
    NOT
    NULL,
    kpi_definition_id
    INT
    NOT
    NULL
    REFERENCES
    kpi
    .
    kpi_definition
(
    id
)
    );

-- Populating kpi_definition table
INSERT INTO kpi.kpi_definition (name, alias, expression, object_type, aggregation_type, aggregation_period,
                                aggregation_elements, exportable, filters, execution_group_id, collection_id)
VALUES ('kpiDefinition1', 'alias2', 'expression1', 'objectType1', 'aggregationType1', 60,
        ('aggregationElement1', 'aggregationElement2'), true, ('filter1', 'filter2'), null,
        'd98700c9-713a-429a-80c6-e82be272a87c'),
       ('kpiDefinition2', 'alias2', 'expression1', 'objectType1', 'aggregationType1', 60,
        ('aggregationElement1', 'aggregationElement2'), true, ('filter1', 'filter2'), null,
        'd98700c9-713a-429a-80c6-e82be272a87c'),
       ('kpiDefinition3', 'alias2', 'expression1', 'objectType1', 'aggregationType1', 60,
        ('aggregationElement1', 'aggregationElement2'), true, ('filter1', 'filter2'), 1,
        'd98700c9-713a-429a-80c6-e82be272a87c');

-- Populating calculation_reliability table
INSERT INTO kpi.calculation_reliability (calculation_start_time, reliability_threshold, calculation_id,
                                         kpi_definition_id)
VALUES (to_timestamp('03 Aug 2022 00:00:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('04 Aug 2022 00:00:00', 'DD MON YYYY HH24:MI:SS'), 'b10de8fb-2417-44cd-802b-19e0b13fd3a5', 1),
       (to_timestamp('03 Aug 2022 02:00:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('03 Aug 2022 05:00:00', 'DD MON YYYY HH24:MI:SS'), 'b10de8fb-2417-44cd-802b-19e0b13fd3a5', 2),
       (to_timestamp('03 Aug 2022 03:00:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('03 Aug 2022 06:00:00', 'DD MON YYYY HH24:MI:SS'), '991be859-1487-4fe2-bbea-fcbc7e06e7ca', 2);
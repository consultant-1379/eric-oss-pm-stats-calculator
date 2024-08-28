--
-- COPYRIGHT Ericsson 2022
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

CREATE SCHEMA IF NOT EXISTS kpi;

CREATE TABLE IF NOT EXISTS kpi.readiness_log
(
    id
    SERIAL
    PRIMARY
    KEY,
    datasource
    VARCHAR
(
    255
) NOT NULL,
    collected_rows_count BIGINT NOT NULL,
    earliest_collected_data TIMESTAMP NOT NULL,
    latest_collected_data TIMESTAMP NOT NULL,
    kpi_calculation_id UUID NOT NULL,
    collection_id UUID NOT NULL
    );

-- Populating table
INSERT INTO kpi.readiness_log (id, datasource, collected_rows_count, earliest_collected_data, latest_collected_data,
                               kpi_calculation_id, collection_id)
VALUES (1, 'datasource1', 10, to_timestamp('03 Aug 2022 17:00:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('04 Aug 2022 17:00:00', 'DD MON YYYY HH24:MI:SS'), '84edfb50-95d5-4afb-b1e8-103ee4acbeb9',
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       (2, 'datasource2', 15, to_timestamp('03 Aug 2022 18:00:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('04 Aug 2022 18:00:00', 'DD MON YYYY HH24:MI:SS'), '84edfb50-95d5-4afb-b1e8-103ee4acbeb9',
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       (3, 'datasource3', 20, to_timestamp('03 Aug 2022 19:00:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('04 Aug 2022 19:00:00', 'DD MON YYYY HH24:MI:SS'), 'c5c46e48-32cf-488e-bd31-803d8078efbe',
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       (4, 'datasource4', 25, to_timestamp('03 Aug 2022 17:00:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('04 Aug 2022 17:20:00', 'DD MON YYYY HH24:MI:SS'), '08673863-2573-4631-9d88-2a87db1b7887',
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       (5, 'datasource5', 15, to_timestamp('03 Aug 2022 17:30:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('04 Aug 2022 18:00:00', 'DD MON YYYY HH24:MI:SS'), '131a2534-2c26-4f60-9169-4d27a872454b',
        '29dc1bbf-7cdf-421b-8fc9-e363889ada79');
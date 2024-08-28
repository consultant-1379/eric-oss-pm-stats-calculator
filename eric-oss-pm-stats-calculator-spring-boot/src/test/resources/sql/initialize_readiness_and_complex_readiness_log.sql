--
-- COPYRIGHT Ericsson 2022
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--


-- Populating table
INSERT INTO kpi.readiness_log (id, datasource, collected_rows_count, earliest_collected_data, latest_collected_data,
                               kpi_calculation_id)
VALUES (1, 'datasource1', 10, to_timestamp('03 Aug 2022 17:00:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('04 Aug 2022 17:00:00', 'DD MON YYYY HH24:MI:SS'), '84edfb50-95d5-4afb-b1e8-103ee4acbeb9'),
       (2, 'datasource2', 15, to_timestamp('03 Aug 2022 18:00:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('04 Aug 2022 18:00:00', 'DD MON YYYY HH24:MI:SS'), '84edfb50-95d5-4afb-b1e8-103ee4acbeb9'),
       (3, 'datasource3', 20, to_timestamp('03 Aug 2022 19:00:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('04 Aug 2022 19:00:00', 'DD MON YYYY HH24:MI:SS'), 'c5c46e48-32cf-488e-bd31-803d8078efbe'),
       (4, 'datasource4', 25, to_timestamp('03 Aug 2022 17:00:00', 'DD MON YYYY HH24:MI:SS'),
        to_timestamp('04 Aug 2022 17:20:00', 'DD MON YYYY HH24:MI:SS'), '08673863-2573-4631-9d88-2a87db1b7887');

INSERT INTO kpi.complex_readiness_log (simple_readiness_log_id, complex_calculation_id)
VALUES (2, 'b77a2a48-92d9-41be-a669-5e9a27c2c1af'),
       (2, '87719ee6-934b-4ce1-a818-10b6ac1238cc'),
       (4, 'b77a2a48-92d9-41be-a669-5e9a27c2c1af');

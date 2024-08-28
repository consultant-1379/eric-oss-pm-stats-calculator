--
-- COPYRIGHT Ericsson 2022
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

DROP TABLE IF EXISTS external_datasource;

CREATE TABLE IF NOT EXISTS external_datasource
(
    id BIGINT PRIMARY KEY,
    aggregation_begin_time TIMESTAMP
);

INSERT INTO external_datasource(id, aggregation_begin_time)
VALUES (1, '2022-07-14 08:00:00'),
       (2, '2022-07-14 08:01:00'),
       (3, '2022-07-14 08:02:00');


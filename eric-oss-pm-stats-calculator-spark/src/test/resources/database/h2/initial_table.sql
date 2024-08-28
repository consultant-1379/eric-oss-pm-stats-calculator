--
-- COPYRIGHT Ericsson 2022
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

CREATE TABLE IF NOT EXISTS dummy_table
(
    aggregation_begin_time TIMESTAMP
);

INSERT INTO dummy_table(aggregation_begin_time)
VALUES (to_timestamp('03 Aug 2022 15:00:00', 'DD MON YYYY HH24:MI:SS')),
       (to_timestamp('03 Aug 2022 16:00:00', 'DD MON YYYY HH24:MI:SS')),
       (to_timestamp('03 Aug 2022 17:00:00', 'DD MON YYYY HH24:MI:SS'))
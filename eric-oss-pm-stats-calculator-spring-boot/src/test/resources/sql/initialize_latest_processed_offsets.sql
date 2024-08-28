--
-- COPYRIGHT Ericsson 2023
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
    PRIMARY
    KEY,
    execution_group
    TEXT
    NOT
    NULL
    UNIQUE
);

CREATE TABLE IF NOT EXISTS kpi.latest_processed_offsets
(
    id
    SERIAL
    PRIMARY
    KEY,
    topic_name
    VARCHAR
(
    255
) NOT NULL,
    topic_partition INT NOT NULL,
    topic_partition_offset BIGINT NOT NULL,
    from_kafka BOOLEAN NOT NULL,
    execution_group_id INT REFERENCES kpi.kpi_execution_groups
(
    id
),
    collection_id UUID NOT NULL,
    CONSTRAINT unique_offset UNIQUE
(
    topic_name,
    topic_partition,
    execution_group_id
)
    );

INSERT INTO kpi.kpi_execution_groups(id, execution_group)
VALUES (1, 'exe_group1'),
       (2, 'exe_group2');

INSERT INTO kpi.latest_processed_offsets(id, topic_name, topic_partition, topic_partition_offset, from_kafka,
                                         execution_group_id, collection_id)
VALUES (1, 'topic1', 0, 10, false, 1, '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       (2, 'topic1', 1, 10, false, 1, '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       (3, 'topic2', 0, 10, false, 2, '29dc1bbf-7cdf-421b-8fc9-e363889ada79'),
       (4, 'topic2', 1, 10, false, 2, '29dc1bbf-7cdf-421b-8fc9-e363889ada79');
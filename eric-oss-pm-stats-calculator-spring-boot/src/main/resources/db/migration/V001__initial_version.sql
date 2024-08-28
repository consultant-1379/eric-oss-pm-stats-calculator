--
-- COPYRIGHT Ericsson 2024
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

-- Create private schema for service
CREATE SCHEMA IF NOT EXISTS kpi;

CREATE OR REPLACE PROCEDURE rename_column_if_exists(
    target_schema TEXT, target_table TEXT, old_column TEXT,new_column TEXT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF EXISTS(SELECT NULL
              FROM information_schema.columns
              WHERE table_schema = target_schema
                AND table_name = target_table
                AND column_name = old_column)
    THEN
        EXECUTE FORMAT('ALTER TABLE %I.%I RENAME COLUMN %I TO %I', target_schema, target_table, old_column, new_column);
    END IF;
END;
$$;

-- Create tables
CREATE TABLE IF NOT EXISTS kpi.kpi_execution_groups
(
    id              SERIAL PRIMARY KEY,
    execution_group TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS kpi.schema_details
(
    id           SERIAL PRIMARY KEY,
    kafka_server VARCHAR NOT NULL,
    topic        VARCHAR NOT NULL,
    namespace    VARCHAR NOT NULL,
    CONSTRAINT unique_detail UNIQUE (kafka_server, topic, namespace)
);

CREATE TABLE IF NOT EXISTS kpi.kpi_definition
(
    id                      SERIAL PRIMARY KEY,
    name                    VARCHAR(56)    NOT NULL UNIQUE,
    alias                   VARCHAR(61)    NOT NULL,
    expression              VARCHAR        NOT NULL,
    object_type             VARCHAR(255)   NOT NULL,
    aggregation_type        VARCHAR(255)   NOT NULL,
    aggregation_period      INT            NOT NULL,
    aggregation_elements    VARCHAR(255)[] NOT NULL,
    is_visible              BOOLEAN        NOT NULL,
    schema_data_space       VARCHAR,
    schema_category         VARCHAR,
    schema_name             VARCHAR,
    execution_group_id      INT REFERENCES kpi.kpi_execution_groups (id),
    data_reliability_offset INT,
    data_lookback_limit     INT,
    reexport_late_data      BOOLEAN,
    time_deleted            TIMESTAMP,
    -- TODO: later on change this to a junction table instead of saving it with the definition
    schema_detail_id        INT REFERENCES kpi.schema_details (id)
);

CALL rename_column_if_exists('kpi', 'kpi_definition', 'is_visible', 'exportable');

CREATE TABLE IF NOT EXISTS kpi.latest_source_data
(
    source                     VARCHAR(255),
    latest_time_collected      TIMESTAMP NOT NULL,
    aggregation_period_minutes int4      NOT NULL,
    execution_group            VARCHAR(100),
    PRIMARY KEY (source, aggregation_period_minutes, execution_group)
);

CREATE TABLE IF NOT EXISTS kpi.kpi_calculation
(
    calculation_id  UUID,
    time_created    TIMESTAMP    NOT NULL,
    time_completed  TIMESTAMP,
    state           VARCHAR(32)  NOT NULL,
    parameters      TEXT,
    execution_group VARCHAR(100) NOT NULL,
    kpi_type        VARCHAR(32)  NOT NULL,
    PRIMARY KEY (calculation_id)
);

CREATE TABLE IF NOT EXISTS kpi.readiness_log
(
    id                      SERIAL PRIMARY KEY,
    datasource              VARCHAR(255) NOT NULL,
    collected_rows_count    BIGINT       NOT NULL,
    earliest_collected_data TIMESTAMP    NOT NULL,
    latest_collected_data   TIMESTAMP    NOT NULL,
    kpi_calculation_id      UUID         NOT NULL,
    CONSTRAINT fk_kpi_calculation FOREIGN KEY (kpi_calculation_id) REFERENCES kpi.kpi_calculation (calculation_id)
);

CREATE TABLE IF NOT EXISTS kpi.complex_readiness_log
(
    simple_readiness_log_id INT REFERENCES kpi.readiness_log (id) ON DELETE CASCADE,
    complex_calculation_id  UUID REFERENCES kpi.kpi_calculation (calculation_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS kpi.on_demand_bulk_parameters
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(56) NOT NULL
);

CREATE TABLE IF NOT EXISTS kpi.on_demand_parameters
(
    id                SERIAL PRIMARY KEY,
    name              VARCHAR(56) NOT NULL,
    type              VARCHAR(32) NOT NULL,
    bulk_parameter_id INT REFERENCES kpi.on_demand_bulk_parameters (id),
    CONSTRAINT unique_parameter UNIQUE (name, bulk_parameter_id)
);

DO
$$
    BEGIN
        BEGIN
            ALTER TABLE IF EXISTS kpi.on_demand_bulk_parameters
                RENAME TO on_demand_tabular_parameters;
        EXCEPTION
            WHEN SQLSTATE '42P07' THEN
                DROP TABLE kpi.on_demand_bulk_parameters;
                RAISE NOTICE 'relation "on_demand_tabular_parameters" already exists';
            WHEN SQLSTATE '42501' THEN
                RAISE NOTICE 'relation "on_demand_tabular_parameters" already exists and in ownership of another user';
        END;
    END;
$$;

CALL rename_column_if_exists('kpi','on_demand_parameters', 'bulk_parameter_id', 'tabular_parameter_id');

CREATE TABLE IF NOT EXISTS kpi.dimension_tables
(
    id             SERIAL PRIMARY KEY,
    calculation_id UUID REFERENCES kpi.kpi_calculation (calculation_id) ON DELETE CASCADE,
    table_name     VARCHAR(62) NOT NULL
);

CREATE TABLE IF NOT EXISTS kpi.latest_processed_offsets
(
    id                     SERIAL PRIMARY KEY,
    topic_name             VARCHAR(255) NOT NULL,
    topic_partition        INT          NOT NULL,
    topic_partition_offset BIGINT       NOT NULL,
    from_kafka             BOOLEAN      NOT NULL,
    execution_group_id     INT REFERENCES kpi.kpi_execution_groups (id),
    CONSTRAINT unique_offset UNIQUE (topic_name, topic_partition, execution_group_id)
);

CREATE TABLE IF NOT EXISTS kpi.calculation_reliability
(
    id                      SERIAL PRIMARY KEY,
    earliest_collected_data TIMESTAMP NOT NULL,
    latest_collected_data   TIMESTAMP NOT NULL,
    calculation_id          UUID      NOT NULL REFERENCES kpi.kpi_calculation (calculation_id),
    kpi_definition_id       INT       NOT NULL REFERENCES kpi.kpi_definition (id)
);

CALL rename_column_if_exists('kpi', 'calculation_reliability', 'earliest_collected_data', 'calculation_start_time');
CALL rename_column_if_exists('kpi', 'calculation_reliability', 'latest_collected_data', 'reliability_threshold');

CREATE TABLE IF NOT EXISTS kpi.on_demand_definitions_per_calculation
(
    kpi_definition_id INT REFERENCES kpi.kpi_definition (id) ON DELETE CASCADE,
    calculation_id    UUID REFERENCES kpi.kpi_calculation (calculation_id) ON DELETE CASCADE
);

ALTER TABLE kpi.kpi_definition
    ADD COLUMN IF NOT EXISTS filters VARCHAR(255)[];
ALTER TABLE kpi.kpi_definition
    ADD COLUMN IF NOT EXISTS schema_data_space VARCHAR;
ALTER TABLE kpi.kpi_definition
    ADD COLUMN IF NOT EXISTS reexport_late_data BOOLEAN;
ALTER TABLE kpi.kpi_definition
    ADD COLUMN IF NOT EXISTS time_deleted TIMESTAMP;
ALTER TABLE kpi.kpi_calculation
    ADD COLUMN IF NOT EXISTS time_completed TIMESTAMP;
ALTER TABLE kpi.latest_processed_offsets
    ADD COLUMN IF NOT EXISTS from_kafka BOOLEAN;

-- hard to check if it does NOT exist
ALTER TABLE kpi.on_demand_parameters
    DROP CONSTRAINT IF EXISTS unique_parameter;
ALTER TABLE kpi.on_demand_parameters
    ADD CONSTRAINT unique_parameter UNIQUE (name, tabular_parameter_id);

UPDATE kpi.kpi_calculation
SET state = 'FINALIZING'
WHERE state = 'SENDING_REPORT_TO_EXPORTER';

CREATE UNIQUE INDEX IF NOT EXISTS unique_parameter_index_1 ON kpi.on_demand_parameters (name, tabular_parameter_id) WHERE tabular_parameter_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS unique_parameter_index_2 ON kpi.on_demand_parameters (name) WHERE tabular_parameter_id IS NULL;

DO
$$
    BEGIN
        CREATE ROLE kpi_service_user WITH LOGIN;
    EXCEPTION
        WHEN SQLSTATE '42710' THEN
            RAISE NOTICE 'user "kpi_service_user" already exists';
    END;
$$;

GRANT ALL PRIVILEGES ON SCHEMA public TO kpi_service_user WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO kpi_service_user WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON SCHEMA kpi TO kpi_service_user WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA kpi TO kpi_service_user WITH GRANT OPTION;
ALTER ROLE kpi_service_user SET search_path to kpi,public;

DROP PROCEDURE IF EXISTS rename_column_if_exists(target_schema TEXT, target_table TEXT, old_column TEXT, new_column TEXT);
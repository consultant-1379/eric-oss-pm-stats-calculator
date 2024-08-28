--
-- COPYRIGHT Ericsson 2022
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

create TABLE IF NOT EXISTS kpi.kpi_rolling_aggregation_1440("agg_column_0" INTEGER, "aggregation_begin_time" TIMESTAMP NOT NULL, "aggregation_end_time" TIMESTAMP, "rolling_sum_integer_1440" INTEGER, "rolling_max_integer_1440" INTEGER) PARTITION BY RANGE ( aggregation_begin_time ) with ( OIDS = FALSE );
alter table kpi.kpi_rolling_aggregation_1440 OWNER TO kpi_service_user;

DO $$
declare
    _partition_name TEXT;
    _partition_query TEXT;
    _partition_index TEXT;
    _partition_unique TEXT;
    _unique_idx_name TEXT;
	_date_txt TEXT;
	_date TEXT;
	_day_before_date TEXT;
	_alter_table_txt TEXT;
begin
    for counter in 1..2 loop
	    _date_txt:= to_char(now() - interval '1 day' * counter , 'YYYY_MM_DD');
		_date:=to_char(now() - interval '1 day' * counter , 'YYYY-MM-DD');
		_day_before_date:=to_char(now() - interval '1 day' * (counter-1),'YYYY-MM-DD');
		_partition_name := 'kpi_rolling_aggregation_1440_p_' || _date_txt;
		_unique_idx_name := _partition_name || '_ui';
		_partition_query := format('CREATE TABLE IF NOT EXISTS %I PARTITION OF kpi_rolling_aggregation_1440 FOR VALUES FROM ( ''%I'' ) TO ( ''%I'' );', _partition_name, _date, _day_before_date);
		_partition_unique := format('CREATE UNIQUE INDEX IF NOT EXISTS %I ON %I ("agg_column_0", "aggregation_begin_time");', _unique_idx_name, _partition_name);
        _alter_table_txt := format('ALTER TABLE %I OWNER TO kpi_service_user;', _partition_name);
		execute _partition_query;
		execute _partition_unique;
		execute _alter_table_txt;
    end loop;
end $$;

-- rolling aggregation IT
-- Insert rolling aggregate test data
insert into kpi.kpi_rolling_aggregation_1440 ("agg_column_0", "aggregation_begin_time", "aggregation_end_time" ,"rolling_sum_integer_1440", "rolling_max_integer_1440") values
(1, (date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(2, (date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(3, (date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(4, (date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(5, (date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(6, (date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(7, (date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(8, (date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(9, (date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(10,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(11,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(12,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(13,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(14,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(15,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(16,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(17,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(18,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(19,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(20,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(21,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(22,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(23,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(24,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(25,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(26,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(27,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(28,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(29,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(30,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(31,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(32,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(33,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(34,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(35,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(36,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(37,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(38,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(39,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(40,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(41,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(42,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(43,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(44,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(45,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(46,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(47,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(48,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(49,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(50,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(51,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(52,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(53,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(54,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(55,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(56,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(57,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(58,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(59,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(60,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(61,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(62,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(63,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(64,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '0 days'),1,2),
(65,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(66,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(67,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(68,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(69,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2),
(70,(date_trunc('day', current_timestamp at time zone 'utc') - interval '2 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),1,2);

-- insert test data for IT test in complex KPI sum_integer_float_complex
-- to test param.start_date_time and param.end_date_time
CREATE TABLE IF NOT EXISTS kpi.kpi_simple_60 (
	agg_column_0 int4 NOT NULL,
	agg_column_1 int4 NOT NULL,
	integer_simple int4 NULL,
	integer_array_simple _int4 NULL,
	float_array_simple _float8 NULL,
	float_simple float8 NULL,
	aggregation_begin_time timestamp NOT NULL,
	aggregation_end_time timestamp NULL
)
PARTITION BY RANGE (aggregation_begin_time);
alter table kpi.kpi_simple_60 OWNER TO kpi_service_user;

DO $$
declare
    _partition_name TEXT;
    _partition_query TEXT;
    _partition_index TEXT;
    _partition_unique TEXT;
    _unique_idx_name TEXT;
	_date_txt TEXT;
	_date TEXT;
	_day_before_date TEXT;
	_alter_table_txt TEXT;
begin
    for counter in 1..2 loop
	    _date_txt:= to_char(now() - interval '1 day' * counter , 'YYYY_MM_DD');
		_date:=to_char(now() - interval '1 day' * counter , 'YYYY-MM-DD');
		_day_before_date:=to_char(now() - interval '1 day' * (counter-1),'YYYY-MM-DD');
		_partition_name := 'kpi_simple_60_p_' || _date_txt;
		_unique_idx_name := _partition_name || '_ui';
		_partition_query := format('CREATE TABLE IF NOT EXISTS %I PARTITION OF kpi_simple_60 FOR VALUES FROM ( ''%I'' ) TO ( ''%I'' );', _partition_name, _date, _day_before_date);
		_partition_unique := format('CREATE UNIQUE INDEX IF NOT EXISTS %I ON %I ("agg_column_0", "aggregation_begin_time");', _unique_idx_name, _partition_name);
        _alter_table_txt := format('ALTER TABLE %I OWNER TO kpi_service_user;', _partition_name);
		execute _partition_query;
		execute _partition_unique;
		execute _alter_table_txt;
    end loop;
end $$;

INSERT INTO kpi.kpi_simple_60 (agg_column_0,agg_column_1,integer_simple,integer_array_simple,float_array_simple,float_simple,aggregation_begin_time,aggregation_end_time) VALUES
(8,54,1,'{1,2,3,4,5}','{1.0,2.0,3.0,4.0,1.0}',0.2,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '12 hours')),
(9,33,2,'{1,2,3,4,5}','{1.0,2.0,3.0,4.0,1.0}',0.2,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '12 hours')),
(10,66,3,'{1,2,3,4,5}','{1.0,2.0,3.0,4.0,1.0}',0.2,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '12 hours')),
(16,76,4,'{1,2,3,4,5}','{1.0,2.0,3.0,4.0,1.0}',0.2,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '12 hours')),
(18,18,5,'{1,2,3,4,5}','{1.0,2.0,3.0,4.0,1.0}',0.2,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '12 hours')),
(22,86,6,'{1,2,3,4,5}','{1.0,2.0,3.0,4.0,1.0}',0.2,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '12 hours')),
(27,34,7,'{1,2,3,4,5}','{1.0,2.0,3.0,4.0,1.0}',0.2,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '12 hours')),
(33,54,8,'{1,2,3,4,5}','{1.0,2.0,3.0,4.0,1.0}',0.2,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '12 hours')),
(34,19,9,'{1,2,3,4,5}','{1.0,2.0,3.0,4.0,1.0}',0.2,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '12 hours')),
(35,72,10,'{1,2,3,4,5}','{1.0,2.0,3.0,4.0,1.0}',0.2,(date_trunc('day', current_timestamp at time zone 'utc') - interval '1 days'),(date_trunc('day', current_timestamp at time zone 'utc') - interval '12 hours'));

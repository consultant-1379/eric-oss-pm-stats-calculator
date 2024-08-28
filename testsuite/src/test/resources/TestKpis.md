## KPI JSONs

There are 7 different file of KPI definitions, all has a different purpose.
> Total of 88 Kpi definition

The 7 files is listed below:

* TestVerySimpleKpis
* TestSimpleKpis
* TestComplexKpis
* TestOnDemandKpis
* TestInvalidSimpleKpis
* SDKKpis
* SDKKpis_notCalculated

> **_NOTE:_** `SDKKpis` and `SDKKpis_notCalculated` contain the examples presented in the SDK. The KPIs in the `SDKKpis_notCalculated` are validated then 
> deleted without calculation to avoid increasing the length of the IT. This is a temporary solution till refactoring the IT.

> **_NOTE:_** To be able to test everything properly, the 15 minutes aggregation has an unusual source data with 5 minute rop rather than the expected 
> 15 minute rop

## Writing new KPIs
* Before adding new KPIs, always check if there is an existing one that is covers your test case
* Use the existing schemas if it is applicable, do not add new schemas unnecessarily
* If you still need to add a new schema, make sure that the new schema uses the same time range (ropBeginTime and ropEndTime) as the existing ones.

## Very Simple KPIs

> Total of 6 KPI definition

No testing for these, the only purpose is to have dummy data for the other KPI types.
The calculations will only move data from kafka to the Postgres database.

| KPI name                | Output table             | Description/Purpose                                                                          |
|-------------------------|--------------------------|----------------------------------------------------------------------------------------------|
| integer_simple          | `kpi_simple_60`          | Dummy integer data from kafka to PG                                                          |
| float_array_simple      | `kpi_simple_60`          | Dummy float array data from kafka to PG                                                      |
| integer_array_simple    | `kpi_simple_60`          | Dummy integer array data from kafka to PG                                                    |
| float_simple            | `kpi_simple_60`          | Dummy float data from kafka to PG                                                            |
| integer_simple_same_day | `kpi_same_day_simple_60` | Dummy integer data from kafka to PG for testing the non-triggered COMPLEX3 group calculation |
| float_simple_same_day   | `kpi_same_day_simple_60` | Dummy float data from kafka to PG for testing the triggered COMPLEX4 group calculation       |


## Simple KPIs

> Total of 9 KPI definitions

Basic calculation testings for simple scheduled calculations. Filtering, different schema same table writing, rolling calculations.

| KPI name                           | Output table                                    | Description/Purpose                                               |
|------------------------------------|-------------------------------------------------|-------------------------------------------------------------------|
| sum_integer_1440_simple            | `kpi_cell_guid_simple_1440`                     | Testing easy simple calculation from kafka with schema#1, table#1 |
| sum_integer_arrayindex_1440_simple | `kpi_cell_guid_simple_1440`                     | Testing easy simple calculation from kafka with schema#2, table#1 |
| sum_integer_simple                 | `kpi_rel_guid_s_guid_t_guid_simple_`            | Testing easy simple calculation from kafka with schema#1, table#1 |
| sum_integer_1440_simple_filter     | `kpi_rel_guid_s_guid_t_guid_simple_filter_1440` | Testing filtering for simple calculations                         |
| sum_integer_15                     | `kpi_limited_15`                                | Testing 15 minutes aggregation period for simple                  |
| count_integer_15                   | `kpi_limited_15`                                | Testing 15 minutes aggregation period for simple                  |
| transform_array_15                 | `kpi_limited_15`                                | Testing 15 minutes aggregation period for simple                  |
| not_calculated_simple_15           | `kpi_limited_15`                                | Base for not calculated complex                                   |
| sum_long_single_counter            | `kpi_relation_simple_60`                        | Base for on-demand FDN examples                                   |

## Complex KPIs

> Total of 12 KPI definition

Basic calculations testing the Complex Scheduled flow.

| KPI name                               | Output table                       | Description/Purpose                                                                |
|----------------------------------------|------------------------------------|------------------------------------------------------------------------------------|
| sum_integer_60_complex                 | `kpi_complex_60`                   | Testing `SUM` depending on a very simple KPI, type integer                         |
| sum_float_60_complex                   | `kpi_complex_60`                   | Testing `SUM` depending on a very simple KPI, type float                           |
| integer_array_complex                  | `kpi_complex_60`                   | Testing `FIRST` and `TRANSFORM` depending on a very simple KPI, type integer array |
| sum_float_count_60                     | `kpi_complex_60`                   | Testing `COUNT` depending on a very simple KPI                                     |
| sum_integer_float_complex              | `kpi_complex_60`                   | Testing `SUM` depending on 2 very simple KPI                                       |
| sum_integer_complex                    | `kpi_complex_`                     | Testing a complex kpi without aggregation period                                   |
| sum_integer_1440_complex_nontriggered  | `no output table`                  | Testing that we do not triggering this                                             |
| sum_integer_integer_arrayindex_complex | `kpi_complex2_60`                  | Testing depending on 2 datasource                                                  |
| sum_float_1440_complex                 | `kpi_complex_1440`                 | Testing triggered complex group, while the kpi itself is not reliable              |
| first_integer_complex_15               | `kpi_limited_complex_15`           | Testing 15 minutes aggregation period for complex                                  |
| transform_complex_15                   | `kpi_limited_complex_dependent_15` | Testing 15 minutes aggregation period for complex, depending on another complex    |
| copy_array_15                          | `kpi_limited_complex_15`           | Copy of simple KPI for reference                                                   |

## On demand KPIs

> Total of 18 KPI definitions

| KPI name                                   | Output table                                   | Description/Purpose                                        |
|--------------------------------------------|------------------------------------------------|------------------------------------------------------------|
| rolling_sum_integer_1440                   | `kpi_rolling_aggregation_1440`                 | Testing rolling aggregation of `SUM`                       |
| rolling_max_integer_1440                   | `kpi_rolling_aggregation_1440`                 | Testing rolling aggregation of `MAX`                       |
| first_float_operator_1440_post_aggregation | `kpi_rolling_aggregation_1440`                 | Testing post aggregation                                   |
| executionid_sum_integer_1440               | `kpi_execution_id_1440`                        | Testing parameterization of aggregation element            |
| first_integer_aggregate_slice_1440         | `kpi_cell_guid_1440`                           | Testing aggregate, slice Spark functions                   |
| first_integer_operator_60_stage2           | `kpi_cell_guid_60`                             | Testing spark staging                                      |
| first_integer_operator_60_stage3           | `kpi_cell_guid_60`                             | Testing spark staging                                      |
| first_integer_operator_60_stage4           | `kpi_cell_guid_60`                             | Testing spark staging                                      |
| first_float_divideby0_60                   | `kpi_relation_guid_source_guid_target_guid_60` | Testing operation tokenization                             |
| first_integer_dim_enrich_1440              | `kpi_cell_sector_1440`                         | Testing tabular parameter in aggregation elements          |
| first_float_dim_enrich_1440                | `kpi_cell_sector_1440`                         | Testing tabular parameter in expression                    |
| max_integer_1440_kpidb                     | `kpi_sector_1440`                              | Testing daily aggregation on an hourly aggregated KPI      |
| sum_integer_60_join_kpidb                  | `kpi_sector_60`                                | Testing filter parameterization                            |
| udf_param                                  | `kpi_parameter_types_1440`                     | Testing single parameter with UDF                          |
| udf_tabular_param                          | `kpi_parameter_types_1440`                     | Testing tabular parameter with UDF                         |
| aggregate_array_15                         | `kpi_limited_ondemand_15`                      | Testing 15 minutes aggregation period for On Demand        |
| fdn_concat_edge                            | `kpi_ondemand_fdn_edge_1440`                   | Testing the concatenation of two FDN_PARSE UDF expressions |
| fdn_sum_agg                                | `kpi_ondemand_fdn_agg_1440`                    | Testing the FDN_PARSE UDF in aggregation element           |


## SDK KPIs

### Unsynchronized SDK KPIs

> Total of 6 KPI definition (to be refactored): 5 simple, 1 complex


### Synchronized SDK KPIs

> Total of 9 KPI definition: 4 simple, 4 complex, 1 on demand

| KPI name                         | Output table                 | Description/Purpose                                                      |
|----------------------------------|------------------------------|--------------------------------------------------------------------------|
| simple_fdn_expr                  | `kpi_simple_fdn_cell_60`     | Identifying source cell of a relation with FDN Parse Predefined Function |
| array_sum_simple                 | `kpi_predefined_simple_60`   | Median of Values Predefined Function: simple definition                  |
| relation_level_agg               | `kpi_predefined_simple_60`   | Aggregating relation counters to source cell: simple definition          |
| sum_array_simple                 | `kpi_simple_example_60`      | Sum of the values in an array                                            |
| max_1440_complex                 | `kpi_complex_example_1440`   | Max value calculated from max_60_simple                                  |
| median_complex_hourly            | `kpi_predefined_complex_60`  | Median of Values Predefined Function: complex definition                 |
| weighted_average_complex_hourly  | `kpi_predefined_complex_60`  | Weighted Average of Values Predefined Function: complex definition       |
| cell_level_agg                   | `kpi_complex_fdn_60`         | Aggregating relation counters to source cell: complex definition         |
| on_demand_fdn_filter             | `kpi_on_demand_fdn_60`       | Filtering On-Demand KPI input with FDN Parse Predefined Function         |


### Not calculated SDK KPIs

> Total of 28 KPI definition: 15 simple, 9 complex, 4 on demand
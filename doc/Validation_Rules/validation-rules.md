# PM Stats Calculator Kpi Definition Validation Rules

- [PM Stats Calculator Validation Rules](#pm-stats-calculator-validation-rules)
  - [Introduction](#introduction)
  - [Kpi table and definition schemas](#kpi-table-and-definition-schemas)
  - [Payload-Based validations](#payload-based-validations)
    - [Global validations](#global-validations)
    - [Specific-attribute-validations](#specific-attribute-validations)
  - [Service-Level validations](#service-level-validations)
  - [Parameterization validations](#parameterization-validations)

## Introduction

This document provides information on all existing kpi definition validation rules separately to **payload based** and **service-level** validation.
Kpi Definition Type can be the following 3 types: Scheduled Simple, Scheduled Complex, On Demand Kpi

## Kpi table and definition schemas

```json
{
  "on_demand": {
    "parameters": ["OPTIONAL"],
    "tabular_parameters": ["OPTIONAL"],
    "kpi_output_tables": [
      {
        "aggregation_period": "REQUIRED",
        "alias": "REQUIRED",
        "aggregation_elements": ["REQUIRED"],
        "exportable": "OPTIONAL",
        "kpi_definitions": [
          {
            "name": "REQUIRED",
            "expression": "REQUIRED",
            "object_type": "REQUIRED",
            "aggregation_type": "REQUIRED",
            "aggregation_elements": ["OPTIONAL"],
            "exportable": "OPTIONAL",
            "filters": ["OPTIONAL"]
          }
        ]
      }
    ]
  },
  "scheduled_complex": {
    "kpi_output_tables": [
      {
        "aggregation_period": "OPTIONAL",
        "alias": "REQUIRED",
        "aggregation_elements": ["REQUIRED"],
        "exportable": "OPTIONAL",
        "data_reliability_offset": "OPTIONAL",
        "data_lookback_limit": "OPTIONAL",
        "reexport_late_data": "OPTIONAL",
        "kpi_definitions": [
          {
            "name": "REQUIRED",
            "expression": "REQUIRED",
            "object_type": "REQUIRED",
            "aggregation_type": "REQUIRED",
            "execution_group": "REQUIRED",
            "aggregation_elements": ["OPTIONAL"],
            "exportable": "OPTIONAL",
            "filters": ["OPTIONAL"],
            "data_reliability_offset": "OPTIONAL",
            "data_lookback_limit": "OPTIONAL",
            "reexport_late_data": "OPTIONAL"
          }
        ]
      }
    ]
  },
  "scheduled_simple": {
    "kpi_output_tables": [
      {
        "aggregation_period": "OPTIONAL",
        "alias": "REQUIRED",
        "aggregation_elements": ["REQUIRED"],
        "exportable": "OPTIONAL",
        "inp_data_identifier": "REQUIRED",
        "data_reliability_offset": "OPTIONAL",
        "data_lookback_limit": "OPTIONAL",
        "reexport_late_data": "OPTIONAL",
        "kpi_definitions": [
          {
            "name": "REQUIRED",
            "expression": "REQUIRED",
            "object_type": "REQUIRED",
            "aggregation_type": "REQUIRED",
            "aggregation_elements": ["OPTIONAL"],
            "exportable": "OPTIONAL",
            "filters": ["OPTIONAL"],
            "data_reliability_offset": "OPTIONAL",
            "data_lookback_limit": "OPTIONAL",
            "reexport_late_data": "OPTIONAL",
            "inp_data_identifier": "OPTIONAL"
          }
        ]
      }
    ]
  }
}
```

## Payload-Based validations

Payload level validation happens during unwrapping the JSON formatted request body into the definition Java object.
For the PATCH endpoint, the potential updates are applied, and then all the following payload rules are applied
to the resulting KpiDefinition object.

### Global validations

| Title                                                     | Description                                                                                                       | Error message                                                                                                                            |
|-----------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| Examination of Kpi Definition Type existence              | Validates the existence of the Kpi Definition Type                                                                | ***"At least one kpi definition type is required"***                                                                                     |
| Examination of output tables                              | Validates if there is at least one output table for each Kpi Definition Type present                              | ***"At least one kpi table is required"***                                                                                               |
| Alias uniqueness validation                               | Validates the uniqueness of the alias + aggregation period paris in all Kpi Definition Types                      | ***"< alias > and < aggregation period > values must be unique for a KPI type. Received values: < alias > and < aggregation period >"*** |
| Validation against merging scheduled and on-demand output | Validates the uniqueness of the output tables between Scheduled(simple and complex) and OnDemand kpi definitions. | ***"The following tables should only contain ON_DEMAND or SCHEDULED KPI types: < output-table-name >"***                                 |
| Validation for required attributes                        | If an attribute is **required** then it should not have null value.                                               | ***"< attribute-name > has null value, but this attribute is "required", must not be null"***                                            |
| Validation for empty/blank values                         | If an attribute is **required** then it should not have empty value.                                              | ***"< attribute-name > value < attribute-value > is blank, but this attribute is "required", must not be empty"***                       |
| Validation for not parameterizable attributes             | Not parameterizable attributes should NOT contain parameter token.                                                | ***"The attribute < attribute-name > does not support parameters"***                                                                     |
| Validation for scalar type parameters                     | Scalar type parameters (numbers and booleans) should not be string values                                         | ***"Expected error code 400 when validating KPIs"***                                                                                     |

### Specific attribute validations

| Title                                                                                    | Description                                                                                                                                                                                                                                                     | Error message                                                                                                                                                                  |
|------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Validation for ***expression*** attribute in Simple definitions                          | Validates the 'expression' attribute of Simple Kpi Definition. - it **can NOT** contains 'FROM' and 'kpi_post_agg://'                                                                                                                                           | ***"< expression-attribute > value < actual-value > cannot contain < forbidden-value >"***.                                                                                    |
| Validation for ***expression*** attribute: exactly one FROM                              | Validates the 'expression' attribute of Complex and OnDemand Kpi Definition. The expression attribute **can NOT contain more than one FROM clause**.                                                                                                            | ***" < expression-attribute > value < actual-value > cannot contain more than one 'FROM'"***                                                                                   |
| Validation for ***expression*** attribute in post aggregation definitions                | KPI expression containing kpi_post_agg datasource should not contain other datasources.                                                                                                                                                                         | ***"'< attribute-name >' value '< expression-value >' contains 'kpi_post_agg://' datasource cannot contain < other datasource >:// datasource"***                              | POST, PATCH |
| Validation for ***expression*** attribute in in-memory definitions                       | KPI expression containing in_memory datasource should not contain kpi_db and tabular_parameters datasources.                                                                                                                                                    | ***"'< attribute-name >' value '< expression-value >' contains 'kpi_inmemory://' datasource cannot contain < other datasource >:// datasource"***                              | POST, PATCH |
| Validation for ***expression*** attribute: no tabular parameters datasource in Complexes | Complex KPI expressions should not contain tabular_parameters datasource.                                                                                                                                                                                       | ***"'< attribute-name >' value ' expression-value ' cannot contain 'tabular_parameters'"***                                                                                    | POST, PATCH |
| Validation for ***alias' and 'name*** attribute                                          | Validates the 'alias' and 'name' attributes using a predefined pattern. (e.g.: definition names should follow the "^[a-z][a-zA-Z0-9_]{0,55}$" pattern, it must start with lowercase letter, followed by (lowercase or uppercase) letters, digits or underscore) | ***"< attribute-name > value < actual-value > has invalid format. Format must follow the "< pattern >" pattern"***                                                             |
| Validation for ***dataLookBackLimit*** attribute                                         | Validates the dataLookBackLimit, it must be greater than 0                                                                                                                                                                                                      | ***"< attribute-name > value < actual-value > must be greater than 0"***                                                                                                       |
| Validation for ***filters*** attribute: empty in case of post-aggregation                | Validates the filters attribute, it should be empty in case the 'expression' attribute contains 'kpi_post_agg://' or 'kpi_inmemory://'                                                                                                                          | ***"'filters' attribute must be empty when 'expression' attribute contains 'kpi_post_agg://'"***                                                                               |
| Validation for ***filters*** attribute: can NOT contain selected SQL keywords            | Validates the filters attribute, it should not contain keywords 'FROM', 'WHERE' (case insensitive)                                                                                                                                                              | ***"'filter' value cannot contain 'WHERE'"***; ***"'filter' value cannot contain 'FROM'"***                                                                                    |
| Validation for ***filters*** attribute: can NOT contain more than one data source        | Validates the filters attribute, it can NOT contain more than one data source                                                                                                                                                                                   | ***"'filter' value '< filter-value >' has invalid format. Format must follow the < ^(?!.\*://.\*://)\*$ > pattern"***                                                          |
| Validation for ***data_reliability_offset*** attribute                                   | Validates the data_reliability_offset (If the aggregation period is ***NON*** default the data_reliability_offset should be equal to or smaller than the value of aggregation_period.                                                                           | ***"< data-reliability-offset-attribute > value < data-reliability-offset-value > must be smaller than < aggregation-period-attribute > value < aggregation-period-value >"*** |
| ***Schema*** existence Validation                                                        | Validates the schema against the schema registry                                                                                                                                                                                                                | ***"Schema < schema > is not registered in schema registry."***                                                                                                                |
| Validate references in ***expression***                                                  | Checks each entry in a set of KPI proposals for invalid expressions. Expressions containing aliases or KPIs that are not present in the current payload or database are invalid.                                                                                | ***"Expression contains an alias/kpi that doesn't exist in db or payload"***                                                                                                   |
| Validation for ***aggregation_elements*** attribute                                      | Validates the aggregation_elements attribute, this attribute **can NOT** be empty.                                                                                                                                                                              | ***"Table attribute aggregation_elements is empty, but this attribute is "required", must not be empty"***                                                                     |

## Service-Level validations

Service-level validation validates based on the created Java object.

| Title                                                  | Description                                                                                                                                                                                                          | Error message                                                                                                                                                                  | Applicable  |
|--------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------|
| Alias and aggregation period DB validation             | Validates the alias-aggregation period pairs of all Kpi Definition types against db.                                                                                                                                 | ***"Kpi with an alias < alias > and aggregation period < aggregation period > already exists in database for a conflicting KPI type"***                                        | POST        |
| DataIdentifier Validation for Simple Kpi Definition    | Validates the DataIdentifier of Scheduled Simple Kpi Definition. It checks whether it is in the schema details cache, if not then put it in                                                                          | no error message                                                                                                                                                               | POST        |
| Name Uniqueness Validation  1                          | Validates the Kpi definition name uniqueness against the payload                                                                                                                                                     | ***"KPI name must be unique but < definition name > is duplicated in the payload"***                                                                                           | POST        |
| Name Uniqueness Validation  2                          | Validates the Kpi definition name uniqueness against the database.                                                                                                                                                   | ***"KPI name must be unique but < definition-name > is already defined in the database"***                                                                                     | POST        |
| Validate referred fields in Complex and On-Demand KPIs | Complex and On-Demand KPI definitions where an attribute is referring to a non-existing table or column (additionally, in case of Complexes, if it is referring to an On-Demand KPI) must be rejected.               | ***"KPI Definition < definition-name > has reference < resolution-reference >"***                                                                                              | POST        |
| Unknown datasource                                     | If a datasource is not defined as an external one and not the built in datasources then the payload is rejected                                                                                                      | ***"Datasource < datasource-name >  has no connection. Use sources < built-in-datasources >"***                                                                                | POST        |
| Complex KPI in-memory datasource usage                 | Validate the following condition for Complexes: in case the kpi_inmemory and kpi_post_agg data sources are used in a KPI definition, the KPI can only depend on KPIs which are in the same execution group as itself | ***"KPI Definition < definition-name > with reference < resolution-reference > points outside the execution group of < execution-group > but uses in-memory datasource"***     | POST        |
| Mandatory usage of the **ON** keyword                  | In an expression after a JOIN keyword ON keyword must be used                                                                                                                                                        | ***"In expression < expression-text > 'JOIN' with the following tables: < table-list > does not contain 'ON' condition"***                                                     | POST        |
| Execution graph Validation                             | Validates the execution graph, it checks circular definitions                                                                                                                                                        | ***"Following execution groups have circular definition: < group-loop-in-definitions>"***                                                                                      | POST, PATCH |
| KPI Definition graph Validation                        | Validates the KPI Definition graph, it checks circular definitions                                                                                                                                                   | ***"Following KPIs in the same group have circular definition: < list-of-circular-loops >"***                                                                                  | POST, PATCH |
| Referenced Schema Validation                           | Validates the Kpi definition's aggregation_element, expression and filter attributes for valid references to schema                                                                                                  | ***"KPI Definition with name < definition-name > for schema < schem-name > the following references not found: < list of invalid references >"***                              | POST, PATCH |
| No dangling dependency after delete                    | Validates, that if we delete all the KPIs in the request, no KPI would remain in the database depending on either of the deleted ones.                                                                               | ***"The Following KPIs have dependencies that would be deleted: < dangg dependency list >"***                                                                                  | DELETE      |
| KPI expression spark sql idiom whitelist validation    | Validates the spark sql in the expression against a whitelist of allowed spark sql idiom.                                                                                                                            | ***"KPI Definition < Kpi name > contains the following prohibited sql elements: '< idioms >'"***                                                                               | POST, PATCH |
| On-Demand Parameter and Tabular Parameter validation   | Validates whether a parameter or tabular parameter has been declared before being used in a KPI definition.                                                                                                          | ***"Missing declaration for following parameters: '< parameter-name >'"***                                                                                                     | POST        |
| Validation for parameters' type match                  | Type of parameters must match the declared types                                                                                                                                                                     | ***"Parameter(s): < parameter-name> do(es) not match with the declared type(s)."***                                                                                            | POST        |
| Tabular parameter validation 1                         | Validates if requested tabular parameter exists in database                                                                                                                                                          | ***"The following tabular parameters are not present in the KPI definition database: < tabular-parameter-name >"***                                                            | POST        |
| Tabular parameter validation 2                         | Validates if the requested calculation provides the required tabular parameter for the calculation.                                                                                                                  | ***"The following tabular parameters are not present for the triggered KPIs: < tabular-parameter-name >"***                                                                    | POST        |
| Tabular parameter validation 3                         | Validates if columns are provided in the header (if header presents)                                                                                                                                                 | ***"Not all of the required columns are present in the header of following tabular parameters: < tabular-parameter-name >"***                                                  | POST        |
| Tabular parameter datasource not alone                 | Validates the 'expression' attribute of On Demand Kpi Definition. 'tabular_parameters://' data source can't be used with 'FROM' clause alone, only with 'INNER JOIN'.                                                | ***"'< definition name >' definition has an invalid expression: '< expression-value >'! The tabular parameters:// data source can only be used with INNER JOIN, not alone."*** | POST, PATCH |

## Parameterization validations

| Title                       | Description                                                                                                                                                    | Error message                                                                                                                                   |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| Parameters should be unique | Validates the parameter which is declared in a KPI Definition request is not already declared and persisted in the database, or not duplicated in the request. | ***"ERROR: duplicate key value violates unique constraint \"unique_parameter_index_2\ Detail: Key (name)=(<parameter_name>) already exists."*** |

![NON](error.png) - Non parameterizable

![Param](check.png) - Parameterizable

| Definition type | Attribute           | Table level         | Definition level    |
|-----------------|---------------------|---------------------|---------------------|
| COMPLEX         | expression          | N/A                 | ![NON](error.png)   |
| COMPLEX         | aggregation_element | ![NON](error.png)   | ![NON](error.png)   |
| COMPLEX         | filters             | N/A                 | ![NON](error.png)   |
| SIMPLE          | expression          | N/A                 | ![NON](error.png)   |
| SIMPLE          | aggregation_element | ![NON](error.png)   | ![NON](error.png)   |
| SIMPLE          | filters             | N/A                 | ![NON](error.png)   |
| ON_DEMAND       | expression          | N/A                 | ![Param](check.png) |
| ON_DEMAND       | aggregation_element | ![Param](check.png) | ![Param](check.png) |
| ON_DEMAND       | filters             | N/A                 | ![Param](check.png) |

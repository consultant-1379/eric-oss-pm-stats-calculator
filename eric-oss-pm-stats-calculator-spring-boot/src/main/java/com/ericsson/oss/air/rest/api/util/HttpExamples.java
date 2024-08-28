/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.api.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpExamples {

    public static final String POST_DEFINITION_REQUEST =
            "{\"scheduled_simple\": " +
                    "{ \"kpi_output_tables\": " +
                        "[{\"aggregation_period\": 60, " +
                        "\"alias\": \"simple\", " +
                        "\"aggregation_elements\": [\"example_schema.nodeFDN\"], " +
                        "\"exportable\": true, " +
                        "\"inp_data_identifier\": \"5G|PM_COUNTERS|example_schema\", " +
                        "\"data_reliability_offset\": 0, " +
                        "\"data_lookback_limit\": 240, " +
                        "\"retention_period_in_days\" : 3, " +
                        "\"kpi_definitions\": " +
                            "[{\"name\": \"resp_time\", " +
                            "\"expression\": \"SUM(example_schema.pmCounters.sm_context_resp_time)\", " +
                            "\"object_type\": \"FLOAT\", " +
                            "\"aggregation_type\": \"SUM\"}," +
                            "{\"name\": \"resp_req\", " +
                            "\"expression\": \"SUM(example_schema.pmCounters.sm_context_req))\", " +
                            "\"object_type\": \"INTEGER\", " +
                            "\"aggregation_type\": \"SUM\"}]}]}," +
            "\"scheduled_complex\": " +
                    "{\"kpi_output_tables\": " +
                    "[{\"aggregation_period\": 60, " +
                    "\"alias\": \"complex\", " +
                    "\"aggregation_elements\": [\"kpi_simple_60.nodeFDN\"]," +
                    "\"exportable\": true, " +
                    "\"data_reliability_offset\": 10, " +
                    "\"reexport_late_data\": true, " +
                    "\"retention_period_in_days\" : 3, " +
                    "\"kpi_definitions\": " +
                        "[ {\"name\": \"avg_resp_time\", " +
                        "\"expression\": \"SUM(kpi_simple_60.resp_time) / SUM(kpi_simple_60.resp_req) FROM kpi_db://kpi_simple_60\", " +
                        "\"object_type\": \"FLOAT\", " +
                        "\"aggregation_type\": \"FIRST\", " +
                        "\"execution_group\": \"complex_arbitrary_group\" }]}]}," +
            "\"on_demand\": " +
                    "{\"parameters\": " +
                        "[ { \"name\": \"date_for_filter\", " +
                        "\"type\": \"STRING\"} ], " +
                    "\"tabular_parameters\": " +
                        "[{ \"name\": \"tabular_example\", " +
                        "\"columns\": " +
                            "[{ \"name\": \"nodeFDN\", \"type\": \"STRING\" }, " +
                            "{\"name\": \"moFdn\", \"type\": \"STRING\"}, " +
                            "{\"name\": \"integer_property\", \"type\": \"INTEGER\"}]}]," +
                    "\"kpi_output_tables\":" +
                        "[ {\"aggregation_period\": 1440, " +
                        "\"alias\": \"on_demand\", " +
                        "\"aggregation_elements\": [\"kpi_complex_60.nodeFDN\"], " +
                        "\"exportable\": true, " +
                        "\"retention_period_in_days\" : 3, " +
                        "\"kpi_definitions\": " +
                            "[ {\"name\": \"avg_resp_time_for_day\", " +
                            "\"expression\": \"SUM(kpi_complex_60.avg_resp_time) / COUNT(kpi_complex_60.avg_resp_time) FROM kpi_db://kpi_complex_60\", " +
                            "\"object_type\": \"FLOAT\", " +
                            "\"aggregation_type\": \"FIRST\", " +
                            "\"filters\": [ \"kpi_db://kpi_complex_60.TO_DATE(aggregation_begin_time) > '${param.date_for_filter}'\"]}]}]}}";
    public static final String POST_DEFINITION_SUMMARY = "Add KPI Definitions";
    public static final String POST_DEFINITION_DESCRIPTION = "Add KPI Definitions. Validate that all proposed KPI Definitions are compatible with the expected schema " +
            "and add them to the database. Response indicates the KPI Output Tables in which the KPI can be found.";
    public static final String POST_DEFINITION_201_DESCRIPTION = "The proposed KPI Definitions are compatible with the expected schema and have been added.";
    public static final String POST_DEFINITION_400_DESCRIPTION = "KPI Definition provided has failed validation due to the issue described in the message.";
    public static final String POST_DEFINITION_409_DESCRIPTION = "Conflicting KPI Definitions exist.";
    public static final String POST_DEFINITION_500_DESCRIPTION = "Submitted KPI Definition could not be processed due to a server-side error.";
    public static final String DELETE_DEFINITION = "[\"avg_resp_time\", \"avg_resp_time_for_day\"]";
    public static final String DELETE_DEFINITION_SUMMARY = "Delete KPI Definitions";
    public static final String DELETE_DEFINITION_DESCRIPTION = "Deletes KPI Definitions from the database. The request contains a list of KPI Definition names. Deleting " +
            "already deleted or non existent KPI Definition gives back the same response as successful delete. Deleting " +
            "an already deleted KPI Definition does not change its retention period. If there is a KPI Definition " +
            "in the list you want to delete, that is dependent on another KPI Definition which is not on the list, " +
            "then the deletion of all the KPI Definitions is refused.";
    public static final String POST_DEFINITION_201 = "{\"successMessage\": \"All KPIs proposed are validated and persisted to database.\"," +
            "\"submittedKpiDefinitions\":  {\"resp_time\": \"kpi_simple_60\", \"resp_req\": \"kpi_simple_60\", \"avg_resp_time\": \"kpi_complex_60\"," +
            "\"avg_resp_time_for_day\": \"kpi_on_demand_1440\"}}";
    public static final String POST_DEFINITION_400 = "{\"timestamp\": \"2023-04-27T07:29:39.936\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"The following KPIs have failed schema verification: [resp_req_60_complex]\"}";
    public static final String POST_DEFINITION_409 = "{\"timestamp\": \"2023-04-27T07:29:39.936\", \"status\": 409, \"error\": \"Conflict\", \"message\": \"KPI name must be unique but 'resp_req' is already defined in the database\"}";
    public static final String POST_DEFINITION_500 = "{\"timestamp\": \"2023-04-27T07:29:39.936\", \"status\": 500, \"error\": \"Internal Server Error\", " +
            "\"message\": \"KPIs have been successfully validated and persisted but KPI Output Table creation has failed.\"}";
    public static final String GET_DEFINITION_SUMMARY = "Retrieve KPI Definitions";
    public static final String GET_DEFINITION_DESCRIPTION = "Retrieve KPI Definition list from the database. This endpoint has an optional query parameter: showDeleted." +
            "Its default value is false. If this parameter is true, then the GET endpoint retrieves not only all the usable" +
            "KPI Definitions, but also the deleted ones until the retention period is over.";
    public static final String GET_DEFINITION_200 = "{ \"scheduled_simple\": " +
            "{ \"kpi_output_tables\": " +
                "[{ \"alias\": \"simple\", " +
                "\"aggregation_period\": 60, " +
                "\"retention_period_in_days\" : 3, " +
                "\"kpi_definitions\": " +
                    "[{ \"name\": \"resp_time\", " +
                    "\"expression\": \"SUM(example_schema.pmCounters.sm_context_resp_time)\", " +
                    "\"object_type\": \"FLOAT\", " +
                    "\"aggregation_type\": \"SUM\"," +
                    "\"aggregation_elements\": [\"example_schema.nodeFDN\"], " +
                    "\"exportable\": true, " +
                    "\"data_reliability_offset\": 0, " +
                    "\"data_lookback_limit\": 240, " +
                    "\"inp_data_identifier\": \"5G|PM_COUNTERS|example_schema\" " +
                "}," +
                    "{\"name\": \"resp_req\"," +
                    "\"expression\": \"SUM(example_schema.pmCounters.sm_context_req)\", " +
                    "\"object_type\": \"INTEGER\", " +
                    "\"aggregation_type\": \"SUM\", " +
                    "\"aggregation_elements\": [\"example_schema.nodeFDN\"], " +
                    "\"exportable\": true, \"data_reliability_offset\": 0, " +
                    "\"data_lookback_limit\": 240, " +
                    "\"inp_data_identifier\": \"5G|PM_COUNTERS|example_schema\"}]}]}," +
        "\"scheduled_complex\": " +
            "{\"kpi_output_tables\": " +
                "[{\"alias\": \"complex\", " +
                "\"aggregation_period\": 60, " +
                "\"retention_period_in_days\" : 3, " +
                "\"kpi_definitions\": " +
                    "[{\"name\": \"avg_resp_time\"," +
                    "\"expression\": \"SUM(kpi_simple_60.resp_time) / SUM(kpi_simple_60.resp_req) FROM kpi_db://kpi_simple_60\", " +
                    "\"object_type\": \"FLOAT\", " +
                    "\"aggregation_type\": \"FIRST\", " +
                    "\"aggregation_elements\": [\"kpi_simple_60.nodeFDN\"], " +
                    "\"exportable\": true, " +
                    "\"reexport_late_data\": true, " +
                    "\"data_reliability_offset\": 10, " +
                    "\"execution_group\": \"complex_arbitrary_group\"}]}]}, " +
        "\"on_demand\": " +
            "{ \"kpi_output_tables\": " +
                "[{\"alias\": \"on_demand\", " +
                "\"aggregation_period\": 1440, " +
                "\"retention_period_in_days\" : 3, " +
                "\"kpi_definitions\": " +
                    "[{\"name\": \"avg_resp_time_for_day\"," +
                    "\"expression\": \"SUM(kpi_complex_60.avg_resp_time) / COUNT(kpi_complex_60.avg_resp_time) FROM kpi_db://kpi_complex_60\", " +
                    "\"object_type\": \"FLOAT\", " +
                    "\"aggregation_type\": \"FIRST\", " +
                    "\"aggregation_elements\": [\"kpi_complex_60.nodeFDN\"], " +
                    "\"exportable\": true, " +
                    "\"filters\": [ \"kpi_db://kpi_complex_60.TO_DATE(aggregation_begin_time) > '${param.date_for_filter}'\"]}]}]}}";
    public static final String GET_DEFINITION_200_DESCRIPTION = "Successfully retrieved KPI Definitions.";
    public static final String GET_DEFINITION_500 = "{\"timestamp\": \"2023-04-27T07:29:39.936\", \"status\": 500, \"error\": \"Internal Server Error\"," +
            "\"message\": \"Connection to eric-pm-kpi-data-v2:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.\"}";
    public static final String GET_DEFINITION_500_DESCRIPTION = "Failed to retrieve KPI Definitions.";
    public static final String DELETE_DEFINITION_400 = "{\"timestamp\": \"2023-04-27T07:29:39.936\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"The Following KPIs have dependencies that would be deleted: [kpi2: [kpi1]]\"}";
    public static final String DELETE_DEFINITION_400_DESCRIPTION = "Failed to delete requested KPI Definition due to the issue described in the message.";
    public static final String DELETE_DEFINITION_500 = "{\"timestamp\": \"2023-04-27T07:29:39.936\", \"status\": 500, \"error\": \"Internal Server Error\", \"message\": " +
            "\"Connection to eric-pm-kpi-data-v2:5432 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.\"}";
    public static final String DELETE_DEFINITION_500_DESCRIPTION = "Failed to delete the requested KPI Definitions due to server-side error. Check the message for more information.";
    public static final String DELETE_DEFINITION_200_DESCRIPTION = "The KPI Definitions have been successfully deleted.";
    public static final String PATCH_DEFINITION_REQUEST = "{\"expression\": \"1000 * SUM(kpi_complex_60.avg_resp_time) / COUNT(kpi_complex_60.avg_resp_time) FROM kpi_db://kpi_complex_60\", " +
            "\"exportable\": true}";
    public static final String PATCH_DEFINITION_REQUEST_SUMMARY = "Update a single KPI Definition";
    public static final String PATCH_DEFINITION_REQUEST_DESCRIPTION = "Update a single KPI Definition in the database.";
    public static final String PATCH_DEFINITION_200 = "{\"aggregation_period\": 1440, \"alias\": \"on_demand\", \"aggregation_elements\": [\"kpi_complex_60.nodeFDN\"], " +
            "\"exportable\": true, \"name\": \"avg_resp_time_for_day\", \"expression\": \"1000 * SUM(kpi_complex_60.avg_resp_time) / COUNT(kpi_complex_60.avg_resp_time) FROM kpi_db://kpi_complex_60\", " +
            "\"object_type\": \"FLOAT\", \"aggregation_type\": \"FIRST\", \"filters\": [\"kpi_db://kpi_complex_60.TO_DATE(aggregation_begin_time) > '${param.date_for_filter}'\"]}";
    public static final String PATCH_DEFINITION_200_DESCRIPTION = "The KPI Definition update request is compatible with the expected schema and has been updated.";
    public static final String PATCH_DEFINITION_400 = "{\"timestamp\": \"2023-04-27T07:29:39.936\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"Property 'aggregation_type' cannot be modified in a PATCH request\"}";
    public static final String PATCH_DEFINITION_400_DESCRIPTION = "Provided KPI Definition has failed validation due to the issue described in the message.";
    public static final String PATCH_DEFINITION_500 = "{\"timestamp\": \"2023-04-27T07:29:39.936\", \"status\": 500, \"error\": \"Internal Server Error\", \"message\": \"Connection to eric-pm-kpi-data-v2:5432 refused. Check that the hostname and port are correct " +
            "and that the postmaster is accepting TCP/IP connections.\"}";
    public static final String PATCH_DEFINITION_500_DESCRIPTION = "Failed to process the incoming KPI Definition update request";

    public static final String POST_CALCULATION_REQUEST =
            "{\"kpi_names\": [\"avg_resp_time_for_day\"]," +
             "\"parameters\": [" +
                     "{\"name\":\"param.date_for_filter\", " +
                      "\"value\":\"2023-04-29\"}]," +
             "\"tabular_parameters\": " +
                    "[{\"name\":\"tabular_example_csv\", " +
                      "\"format\":\"CSV\", " +
                      "\"header\": \"nodeFDN,moFdn,execution_id\", " +
                      "\"value\": \"1,1,TEST_1\\n2,2,TEST_1\\n3,3,TEST_2\" }, " +
                     "{\"name\": \"tabular_example_json\"," +
                      "\"format\": \"JSON\", " +
                      "\"value\": \"{\\\"tabular_example_json\\\": [" +
                        "{\\\"nodeFDN\\\": node1, \\\"moFdn\\\": mo1, \\\"execution_id\\\": \\\"TEST_1\\\"}, " +
                        "{\\\"nodeFDN\\\": node2, \\\"moFdn\\\": mo2, \\\"execution_id\\\": \\\"TEST_1\\\"}, " +
                        "{\\\"nodeFDN\\\": node3, \\\"moFdn\\\": mo3, \\\"execution_id\\\": \\\"TEST_2\\\"}]}\"}]}";
    public static final String POST_CALCULATION_SUMMARY = "Trigger On-Demand KPI calculation(s)";
    public static final String POST_CALCULATION_DESCRIPTION =
            "Submit an On-Demand KPI calculation request for validation and calculation. " +
            "You can also assign parameters and tabular parameters for On-Demand KPI calculations. " +
            "The response includes a uuid (calculation_id) for each calculation.";
    public static final String POST_CALCULATION_201_DESCRIPTION =
            "KPI calculation request has been validated and calculation has been launched.";
    public static final String POST_CALCULATION_201 =
            "{ \"successMessage\": \"Requested KPIs are valid and calculation has been launched\", " +
              "\"calculationId\": \"3ae1e711-fadd-4b92-9435-f47dc87199f2\", " +
              "\"kpiOutputLocations\": { \"avg_resp_time_for_day\" : \"kpi_on_demand_1440\" } }";
    public static final String POST_CALCULATION_400_DESCRIPTION =
            "KPI calculation request has failed validation due to the issue described in the message";
    public static final String POST_CALCULATION_400 =
            "{ \"timestamp\": \"2023-04-26T07:27:54\", " +
              "\"status\": 400, " +
              "\"error\": \"Bad Request\", " +
              "\"message\": \"The KPIs requested for calculation have unresolved parameters - Error data: avg_resp_time_for_day\"}";
    public static final String POST_CALCULATION_429_DESCRIPTION =
            "KPI calculation request has failed, currently the maximum number of On-Demand KPI calculations is being handled";
    public static final String POST_CALCULATION_429 =
            "{ \"timestamp\": \"2023-04-26T07:27:54\", " +
              "\"status\": 429, " +
              "\"error\": \"Too Many Requests\", " +
              "\"message\": " +
              "\"PM Stats Calculator is currently handling the maximum number of calculations - Error data: avg_resp_time_for_day\"}";
    public static final String POST_CALCULATION_500_DESCRIPTION =
            "KPI calculation request has failed as an internal server error has occurred.";
    public static final String POST_CALCULATION_500 =
            "{ \"timestamp\": \"2023-04-26T07:27:54\", " +
              "\"status\": 500, " +
              "\"error\": \"Internal Server Error\", " +
              "\"message\": \"Unable to calculate the requested KPIs\"}";

    public static final String GET_CALCULATION_ID_SUMMARY = "Retrieve info about a specific KPI calculation";
    public static final String GET_CALCULATION_ID_DESCRIPTION =
            "Retrieve the status, execution group and readiness logs of a KPI calculation, identified by a calculation id. " +
            "Readiness log contains an entry for each datasource of the calculation, containing the datasource name, " +
            "number of rows collected, time for earliest and the latest data collected.";
    public static final String GET_CALCULATION_ID_200_DESCRIPTION =
            "Requested calculation has been found, kpi calculation info is returned";
    public static final String GET_CALCULATION_ID_200 =
            "{ \"calculationId\": \"74324e06-f902-4302-9c61-d6db71b284fd\", " +
              "\"status\": \"FINISHED\", " +
              "\"executionGroup\": \"complex_arbitrary_group\", " +
              "\"readinessLogs\": [{\"collectedRowCount\": 42 , " +
              "\"datasource\": \"5G|PM_COUNTERS|example_schema\", " +
              "\"earliestCollectedData\": \"2023-04-22T14:00:00\", " +
              "\"latestCollectedData\": \"2023-04-22T19:00:00\"}]}";
    public static final String GET_CALCULATION_ID_400_DESCRIPTION = "Provided KPI calculation ID is invalid";
    public static final String GET_CALCULATION_ID_400 =
            "{ \"timestamp\": \"2023-04-27T07:29:39.936\", " +
              "\"status\": 400, " +
              "\"error\": \"Bad Request\", " +
              "\"message\": \"The provided parameter is not an UUID\"}";
    public static final String GET_CALCULATION_ID_404_DESCRIPTION = "Calculation with the provided id is not found";
    public static final String GET_CALCULATION_ID_404 =
            "{ \"timestamp\": \"2023-04-27T07:29:39.936\", " +
              "\"status\": 404, " +
              "\"error\": \"Not Found\", " +
              "\"message\": \"Calculation state with id '74324e06-f902-4302-9c61-d6db71b284fa' is not found\"}";
    public static final String GET_CALCULATION_ID_500_DESCRIPTION = "Failed to retrieve KPI state from database";
    public static final String GET_CALCULATION_ID_500 =
            "{ \"timestamp\": \"2023-04-27T07:29:39.936\", " +
              "\"status\": 500, " +
              "\"error\": \"Internal Server Error\", " +
              "\"message\": \"\"}";
    public static final String CALCULATION_ID_DESCRIPTION =
            "The calculation id of a calculation to be listed. The calculation ids can be obtained from the response of " +
            "the GET request to the /calc/v1/calculations endpoint. For On-Demand calculations, they can also be obtained " +
            "from the response of the POST request to the /calc/v1/calculations endpoint";

    public static final String GET_CALCULATION_SUMMARY = "Retrieve a list of calculations started in the previous elapsed minutes.";
    public static final String GET_CALCULATION_DESCRIPTION =
            "Returns the status of each recently started calculation. Calculation in this context refers to a process " +
            "of calculating all KPIs within an execution group. Calculations can be identified in the response by the " +
            "execution group name, and by the type of the included KPIs. The response includes a uuid (calculation_id) " +
            "for each calculation. The calculations in the response are ordered by their start time from latest to earliest.";
    public static final String GET_CALCULATION_200_RESPONSE = "Calculations have been found, status and calculation IDs are returned for each.";
    public static final String GET_CALCULATION_200 =
            "[{\"executionGroup\": \"COMPLEX2\", " +
              "\"kpiType\": \"SCHEDULED_COMPLEX\", " +
              "\"status\": \"FINISHED\", " +
              "\"calculationId\": \"070bba4a-c3d8-4de0-98c1-7caa6c427d57\" }," +
             "{\"executionGroup\": \"COMPLEX1\", " +
              "\"kpiType\": \"SCHEDULED_COMPLEX\", " +
              "\"status\": \"FINISHED\", " +
              "\"calculationId\": \"fec86f08-d04e-4a91-9815-45155aacc484\" }]";

    public static final String ELAPSED_MINUTES_DESCRIPTION =
            "The duration that specifies how many minutes back the started calculations should be listed.";
    public static final String INCLUDE_NOTHING_CALCULATED_DESCRIPTION =
            "Determines whether the received list should include all started calculations or exclude those calculations " +
            "in which no KPIs were calculated.";
}

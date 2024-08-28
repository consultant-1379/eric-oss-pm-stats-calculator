/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.utils;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.pm.stats.model.exception.TabularParameterValidationException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonToCsvConverter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Converts JSON data to CSV. The expected format for JSON is:
     * <pre>{@code
     *  {
     *      "json-array-key": [
     *          {
     *              "field-1": "value",
     *              "field-2": "value"
     *          },
     *          {
     *              "field-1": "value",
     *              "field-2": "value"
     *          }
     *      ]
     *  }
     * }</pre>
     *
     * @param jsonValue the JSON object to be transformed
     * @return {@link TabularParameters} with the converted value, and the order of the fields joined together as the header
     */
    public static TabularParameters convertJsonToCsv(final String jsonValue) {
        try {
            final JsonNode root = OBJECT_MAPPER.readTree(jsonValue);
            final String jsonArrayKey = root.fields().next().getKey();
            final JsonNode jsonArrayData = root.get(jsonArrayKey);
            final JsonNode firstObject = jsonArrayData.get(0);

            final List<String> fieldNames = new ArrayList<>();
            final Builder csvBuilder = CsvSchema.builder();
            csvBuilder.setUseHeader(false);

            firstObject.fieldNames().forEachRemaining(fieldName -> {
                log.info("For '{}' JSON key '{}' column added to the CSV representation", jsonArrayKey, fieldName);
                csvBuilder.addColumn(fieldName);
                fieldNames.add(fieldName);
            });
            final CsvSchema csvSchema = csvBuilder.build();
            final String value = new CsvMapper().writerFor(JsonNode.class)
                    .with(csvSchema)
                    .writeValueAsString(jsonArrayData);

            return TabularParameters.builder().value(value).header(String.join(",", fieldNames)).build();
        } catch (final JsonProcessingException e) {
            throw new TabularParameterValidationException("Error occurred while processing json. Unable to save tabular parameters", e);
        }
    }
}

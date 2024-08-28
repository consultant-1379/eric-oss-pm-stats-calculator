/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.udf;

import org.apache.spark.sql.api.java.UDF2;

/**
 * This {@link UDF2} parses an FDN string to detect and expose the hierarchical topology information contained within.
 * The first argument of the function is the full FDN string to be parsed. An FDN string contains concatenated key-value
 * pairs with alphanumeric characters as follows:
 * key1=value1,key2=value2,...,keyN=valueN
 * In the targeted use-case this input is represented by a specific database column identifier when the query is
 * constructed.
 * The second argument is generally a literal, a hierarchy identifier that is used to determine where to truncate the
 * FDN. This identifier is expected to be one of the keys in the FDN.
 * In case of a successful parse and search the returned value is the truncated part of the FDN that contains the
 * located key and its value including any previous key-value pairs that are present. Otherwise, error response is returned.
 */
public class FdnParseUdf implements UDF2<String, String, String> {
    public static final String NAME = "FDN_PARSE";

    private static final long serialVersionUID = 1L;
    private static final int INDEX_NOT_FOUND = -1;
    private static final char EQUALS = '=';
    private static final char SEPARATOR = ',';
    private static final String ERROR_RESPONSE = "FDN_PARSE_ERROR";

    @Override
    public String call(final String fdn, final String filterKey) {
        if (filterKey == null || fdn == null || filterKey.isBlank() || fdn.isBlank() || filterKey.length() >= fdn.length()) {
            return ERROR_RESPONSE;
        }

        int index = 0;
        int separatorIndex = INDEX_NOT_FOUND;
        int actualKeyLength;

        while (index < fdn.length()) {
            index = fdn.indexOf(EQUALS, index);
            if (index == INDEX_NOT_FOUND) {
                return ERROR_RESPONSE;
            }

            actualKeyLength = index - separatorIndex - 1; /* To avoid partial match 'SubNetwork' actual key with filter key of 'Sub' */
            if (actualKeyLength == filterKey.length() && fdn.regionMatches(separatorIndex + 1, filterKey, 0, filterKey.length())) {
                separatorIndex = fdn.indexOf(SEPARATOR, index);
                return separatorIndex == INDEX_NOT_FOUND
                        ? fdn /* filterKey matches to the last key of the FDN */
                        : fdn.substring(0, separatorIndex);
            }

            separatorIndex = fdn.indexOf(SEPARATOR, index);
            if (separatorIndex == INDEX_NOT_FOUND) {
                return ERROR_RESPONSE;
            }

            index = separatorIndex + 1; /* Move to next <key>=<value> pair */
        }

        return ERROR_RESPONSE;
    }
}

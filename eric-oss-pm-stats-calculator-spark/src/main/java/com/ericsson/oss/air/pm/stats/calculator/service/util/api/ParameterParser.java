/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.api;

import java.util.Map;

public interface ParameterParser {
    /**
     * Parse provided parameters.
     *
     * @param parameters
     *         to parse.
     * @return {@link Map} containing the parsed parameters.
     */
    Map<String, String> parseParameters(String parameters);
}

/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for {@link KpiSchemaValidator} class.
 */
class KpiSchemaValidatorTest {

    @Test
    void givenStringContainingParameterToken_whenLookingForTokens_thenAllAreFound() {
        final List<String> listType = new ArrayList<>();
        listType.add("test_entry");
        listType.add("${parameter}");

        assertTrue(KpiSchemaValidator.valueContainsParameterToken(listType));
        assertTrue(KpiSchemaValidator.valueContainsParameterToken("test${parameter}"));
        assertTrue(KpiSchemaValidator.valueContainsParameterToken("${parameter}"));
        assertFalse(KpiSchemaValidator.valueContainsParameterToken(100));
    }
}
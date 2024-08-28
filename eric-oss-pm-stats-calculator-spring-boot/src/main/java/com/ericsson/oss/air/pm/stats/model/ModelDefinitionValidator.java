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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class that validates the KPI model definition payload.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModelDefinitionValidator {

    /**
     * Verifies each entry in a {@link Set} representing a payload does not contradict itself in that it holds conflicting definitions for the same
     * primary key entry.
     *
     * @param definitions a {@link Set} of key value pair that specify proposals to be validated against one another
     * @param primaryKeys a {@link List} a list of Strings that represent the primary keys, i.e. the unique identifier for a Set of entries
     * @return {@link Set} of contradicting entries
     */
    public static Set<Definition> findContradictingDefinitions(final Set<Definition> definitions, final List<String> primaryKeys) {
        final Set<Definition> contradictingDefinitions = new HashSet<>();
        for (final Definition definition : definitions) {
            if (anyPrimaryKeysMissing(definition, primaryKeys)) {
                continue;
            }

            for (final Definition otherDefinition : definitions) {
                if (checkAttributes(definition, otherDefinition, primaryKeys)) {
                    contradictingDefinitions.add(definition);
                    contradictingDefinitions.add(otherDefinition);
                }
            }
        }
        return contradictingDefinitions;
    }

    private static boolean checkAttributes(final Definition definition, final Definition otherDefinition, final List<String> primaryKeys) {
        return checkPrimaryKeyValuesMatch(otherDefinition, definition, primaryKeys) && (!definition.equals(otherDefinition));
    }

    private static boolean anyPrimaryKeysMissing(final Definition definition, final List<String> primaryKeys) {
        for (final String key : primaryKeys) {
            if (!definition.doesAttributeExist(key)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkPrimaryKeyValuesMatch(final Definition definition, final Definition verifyDefinition,
                                                      final List<String> primaryKeys) {
        for (final String key : primaryKeys) {
            if (definition.doesAttributeExist(key) && verifyDefinition.doesAttributeExist(key)) {
                if (!definition.getAttributeByName(key).equals(verifyDefinition.getAttributeByName(key))) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}

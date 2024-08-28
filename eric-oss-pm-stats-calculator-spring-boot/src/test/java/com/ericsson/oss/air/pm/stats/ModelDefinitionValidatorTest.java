/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DAILY_AGGREGATION_PERIOD_IN_MINUTES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.model.ModelDefinitionValidator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ModelDefinitionValidator}.
 */
class ModelDefinitionValidatorTest {
    static final List<String> simplePrimaryKey = new ArrayList<>();
    static final List<String> compoundPrimaryKey = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        simplePrimaryKey.add("name");

        compoundPrimaryKey.add("name");
        compoundPrimaryKey.add("element");
        compoundPrimaryKey.add("aggregation_period");
    }

    @Test
    void whenCheckingForContradictingDefinitions_andInputHasNoDefinitions_thenEmptySetIsReturned() {
        final Set<Definition> input = Collections.emptySet();
        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, simplePrimaryKey);
        Assertions.assertThat(output).isEmpty();
    }

    @Test
    void whenCheckingPayloadForContradictingDefinitions_andInputHasNoDefinitions_thenEmptySetIsReturned() {
        final Set<Definition> input = Collections.emptySet();
        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, compoundPrimaryKey);
        Assertions.assertThat(output).isEmpty();
    }

    @Test
    void whenCheckingForContradictingDefinitions_andInputHasNoDuplicateDefinitions_thenEmptySetIsReturned() {
        final Set<Definition> input = new HashSet<>(1);
        input.add(new Definition(Collections.singletonMap("name", "definition1")));

        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, simplePrimaryKey);

        Assertions.assertThat(output).isEmpty();
    }

    @Test
    void whenCheckingPayloadForContradictingDefinitions_andInputHasNoDuplicateDefinitions_thenEmptySetIsReturned() {
        final Set<Definition> input = new HashSet<>(1);
        input.add(new Definition(Collections.singletonMap("name", "definition1")));

        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, compoundPrimaryKey);

        Assertions.assertThat(output).isEmpty();
    }

    @Test
    void whenCheckingForContradictingDefinitions_andInputHasDefinitionWithSameNameDuplicated_thenDuplicateDefinitionIsReturned() {
        final Map<String, Object> definition1 = new HashMap<>(2);
        definition1.put("name", "definition1");
        definition1.put("attribute", "value1");

        final Map<String, Object> definition2 = new HashMap<>(2);
        definition2.put("name", "definition1");
        definition2.put("attribute", "value2");

        final Set<Definition> input = new HashSet<>(2);
        input.add(new Definition(definition1));
        input.add(new Definition(definition2));

        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, simplePrimaryKey);

        Assertions.assertThat(output).hasSize(2);
        Assertions.assertThat(output).first().extracting(definition -> definition.getAttributeByName("name")).isEqualTo("definition1");
    }

    @Test
    void whenCheckingPayloadForContradictingDefinitions_andInputHasDefinitionWithSameNameDuplicated_thenDuplicateDefinitionsAreReturned() {
        final Map<String, Object> definition1 = new HashMap<>(4);
        definition1.put("name", "definition1");
        definition1.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition1.put("element", "cell");
        definition1.put("attribute", "VALUE_A");

        final Map<String, Object> definition2 = new HashMap<>(4);
        definition2.put("name", "definition1");
        definition2.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition2.put("element", "cell");
        definition2.put("attribute", "VALUE_B");

        final Map<String, Object> definition3 = new HashMap<>(4);
        definition3.put("name", "definition3");
        definition3.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition3.put("element", "cell");
        definition3.put("attribute", "VALUE_B");

        final Set<Definition> input = new HashSet<>(2);
        input.add(new Definition(definition1));
        input.add(new Definition(definition2));
        input.add(new Definition(definition3));

        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, compoundPrimaryKey);

        Assertions.assertThat(output).hasSize(2);
        Assertions.assertThat(output).first().extracting(definition -> definition.getAttributeByName("name")).isEqualTo("definition1");
    }

    @Test
    void whenCheckingForContradictingDefinitions_andInputHasDefinitionWithSameNameDuplicated_andOneValidDefinition_thenDuplicateDefinitionIsReturned() {
        final Map<String, Object> definition1 = new HashMap<>(2);
        definition1.put("name", "definition1");
        definition1.put("attribute", "value1");

        final Map<String, Object> definition2 = new HashMap<>(2);
        definition2.put("name", "definition1");
        definition2.put("attribute", "value2");

        final Map<String, Object> definition3 = Collections.singletonMap("name", "definition2");

        final Set<Definition> input = new HashSet<>(3);
        input.add(new Definition(definition1));
        input.add(new Definition(definition2));
        input.add(new Definition(definition3));

        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, simplePrimaryKey);

        Assertions.assertThat(output).hasSize(2);
        Assertions.assertThat(output).first().extracting(definition -> definition.getAttributeByName("name")).isEqualTo("definition1");
    }

    @Test
    void whenCheckingPayloadForContradictingDefinitions_andInputHasDefinitionWithSameNameDuplicated_andOneValidDefinition_thenDuplicateDefinitionsAreReturned() {
        final Map<String, Object> definition1 = new HashMap<>(4);
        definition1.put("name", "definition1");
        definition1.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition1.put("element", "cell");
        definition1.put("attribute", "VALUE_A");

        final Map<String, Object> definition2 = new HashMap<>(4);
        definition2.put("name", "definition1");
        definition2.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition2.put("element", "cell");
        definition2.put("attribute", "VALUE_B");

        final Map<String, Object> definition3 = new HashMap<>(4);
        definition3.put("name", "definition2");
        definition3.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition3.put("element", "cell");
        definition3.put("attribute", "VALUE_A");

        final Set<Definition> input = new HashSet<>(3);
        input.add(new Definition(definition1));
        input.add(new Definition(definition2));
        input.add(new Definition(definition3));

        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, compoundPrimaryKey);

        Assertions.assertThat(output).hasSize(2);
        Assertions.assertThat(output).first().extracting(definition -> definition.getAttributeByName("name")).isEqualTo("definition1");
    }

    @Test
    void whenCheckingForContradictingDefinitions_andInputHasDefinitionWithNullName_thenNullIsIgnored_andDuplicateDefinitionIsReturned() {
        final Map<String, Object> definition1 = new HashMap<>(2);
        definition1.put("name", "definition1");
        definition1.put("attribute", "value1");

        final Map<String, Object> definition2 = new HashMap<>(2);
        definition2.put("name", "definition1");
        definition2.put("attribute", "value2");

        final Map<String, Object> definitionWithoutName = Collections.singletonMap("invalid", "definition2");

        final Set<Definition> input = new HashSet<>(3);
        input.add(new Definition(definition1));
        input.add(new Definition(definition2));
        input.add(new Definition(definitionWithoutName));

        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, simplePrimaryKey);

        Assertions.assertThat(output).hasSize(2);
        Assertions.assertThat(output).first().extracting(definition -> definition.getAttributeByName("name")).isEqualTo("definition1");
    }

    @Test
    void whenCheckingPayloadForContradictingDefinitions_andInputHasDefinitionWithNullName_thenNullIsIgnored_andDuplicateDefinitionIsReturned() {
        final Map<String, Object> definition1 = new HashMap<>(4);
        definition1.put("name", "definition1");
        definition1.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition1.put("element", "cell");
        definition1.put("attribute", "VALUE_A");

        final Map<String, Object> definition2 = new HashMap<>(4);
        definition2.put("name", "definition1");
        definition2.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition2.put("element", "cell");
        definition2.put("attribute", "VALUE_B");

        final Map<String, Object> definitionWithoutName = Collections.singletonMap("invalid", "definition2");

        final Set<Definition> input = new HashSet<>(3);
        input.add(new Definition(definition1));
        input.add(new Definition(definition2));
        input.add(new Definition(definitionWithoutName));

        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, compoundPrimaryKey);

        Assertions.assertThat(output).hasSize(2);
        Assertions.assertThat(output).first().extracting(definition -> definition.getAttributeByName("name")).isEqualTo("definition1");
    }

    @Test
    void whenCheckingForContradictingDefinitions_andInputHasMultipleDefinitionsWithoutNullName_thenNullsAreIgnored_andDuplicateDefinitionIsReturned() {
        final Map<String, Object> definition1 = new HashMap<>(2);
        definition1.put("name", "definition1");
        definition1.put("attribute", "value1");

        final Map<String, Object> definition2 = new HashMap<>(2);
        definition2.put("name", "definition1");
        definition2.put("attribute", "value2");

        final Map<String, Object> definitionWithoutName1 = Collections.singletonMap("invalid", "definition2");
        final Map<String, Object> definitionWithoutName2 = Collections.singletonMap("invalid", "definition3");

        final Set<Definition> input = new HashSet<>(4);
        input.add(new Definition(definition1));
        input.add(new Definition(definition2));
        input.add(new Definition(definitionWithoutName1));
        input.add(new Definition(definitionWithoutName2));

        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, simplePrimaryKey);

        Assertions.assertThat(output).hasSize(2);
        Assertions.assertThat(output).first().extracting(definition -> definition.getAttributeByName("name")).isEqualTo("definition1");
    }

    @Test
    void whenCheckingPayloadForContradictingDefinitions_andInputHasMultipleDefinitionsWithoutNullName_thenNullsAreIgnored_andDuplicateDefinitionIsReturned() {
        final Map<String, Object> definition1 = new HashMap<>(4);
        definition1.put("name", "definition1");
        definition1.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition1.put("element", "cell");
        definition1.put("attribute", "VALUE_A");

        final Map<String, Object> definition2 = new HashMap<>(4);
        definition2.put("name", "definition1");
        definition2.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition2.put("element", "cell");
        definition2.put("attribute", "VALUE_B");

        final Map<String, Object> definitionWithoutName1 = Collections.singletonMap("invalid", "definition2");
        final Map<String, Object> definitionWithoutName2 = Collections.singletonMap("invalid", "definition3");

        final Set<Definition> input = new HashSet<>(4);
        input.add(new Definition(definition1));
        input.add(new Definition(definition2));
        input.add(new Definition(definitionWithoutName1));
        input.add(new Definition(definitionWithoutName2));

        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, compoundPrimaryKey);

        Assertions.assertThat(output).hasSize(2);
        Assertions.assertThat(output).first().extracting(definition -> definition.getAttributeByName("name")).isEqualTo("definition1");
    }

    @Test
    void whenCheckingPayloadForContradictingDefinitions_andInputHasMatchingDefinitions_thenNoDuplicatesAreFound() {
        final Map<String, Object> definition1 = new HashMap<>(4);
        definition1.put("name", "definition1");
        definition1.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition1.put("element", "cell");
        definition1.put("attribute", "VALUE_A");

        final Map<String, Object> definition2 = new HashMap<>(4);
        definition2.put("name", "definition1");
        definition2.put("aggregation_period", DAILY_AGGREGATION_PERIOD_IN_MINUTES);
        definition2.put("element", "cell");
        definition2.put("attribute", "VALUE_A");

        final Set<Definition> input = new HashSet<>(2);
        input.add(new Definition(definition1));
        input.add(new Definition(definition2));

        final Set<Definition> output = ModelDefinitionValidator.findContradictingDefinitions(input, compoundPrimaryKey);

        Assertions.assertThat(output).isEmpty();
    }
}

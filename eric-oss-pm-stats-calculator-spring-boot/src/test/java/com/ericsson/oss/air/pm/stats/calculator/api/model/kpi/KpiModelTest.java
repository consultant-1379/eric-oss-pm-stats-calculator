/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class KpiModelTest {
    @Test
    void shouldCreateKpiModel() {
        final String name = "name";
        final String version = "version";
        final String namespace = "namespace";
        final Kpi element = new Kpi();

        final KpiModel actual = new KpiModel(name, namespace, version, element);

        Assertions.assertThat(actual.getName()).isEqualTo(name);
        Assertions.assertThat(actual.getNamespace()).isEqualTo(namespace);
        Assertions.assertThat(actual.getVersion()).isEqualTo(version);
        Assertions.assertThat(actual.getElement()).isEqualTo(element);
    }
}
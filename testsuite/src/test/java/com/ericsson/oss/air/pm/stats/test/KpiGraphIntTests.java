/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test;

import static com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils.getClasspathResourceAsString;
import static com.ericsson.oss.air.pm.stats.test.util.RequestUtils.onFailure;

import com.ericsson.oss.air.pm.stats.test.dto.input._assert.ErrorResponseAssertions;
import com.ericsson.oss.air.pm.stats.test.integration.IntegrationTest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
@IntegrationTest
class KpiGraphIntTests {

    /**
     * <img src="./../../../../../../../../resources/graphs/failure_kpi_definition_graph_loop.png" alt="Created graph" />
     * <p>
     * To edit the image:
     * <p>
     * <a href="http://graphonline.ru/en/?graph=wfPmDPtxQfbPLAxT">http://graphonline.ru/en/?graph=wfPmDPtxQfbPLAxT</a>
     */
    @Test
    void shouldFailOnKpiDefinitionLevelLoops() throws Exception {
        final String payload = getClasspathResourceAsString("graphs/failure_kpi_definition_graph_loop.json");
        onFailure().postKpiDefinition(payload,errorResponse -> {
            log.info("Loop detected: {}", errorResponse);
            ErrorResponseAssertions.assertThat(errorResponse).isConflictWithMessage(
                    "Following KPIs in the same group have circular definition: [" +
                    "loop_detection.complex_2 -> loop_detection.complex_1 -> loop_detection.complex_6 -> loop_detection.complex_7 -> loop_detection.complex_3 -> loop_detection.complex_2, " +
                    "loop_detection.complex_1 -> loop_detection.complex_6 -> loop_detection.complex_7 -> loop_detection.complex_3 -> loop_detection.complex_4 -> loop_detection.complex_1" +
                    ']'
            );
        });
    }
}

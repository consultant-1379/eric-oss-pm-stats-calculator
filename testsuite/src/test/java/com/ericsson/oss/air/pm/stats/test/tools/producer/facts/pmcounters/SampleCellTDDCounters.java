/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters;

import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.euTDD.ExampleCounter1TDD;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.euTDD.ExampleCounter2TDD;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.euTDD.ExampleCounterTDD;
import lombok.Data;

@Data
public class SampleCellTDDCounters {
    private ExampleCounterTDD exampleCounter;
    private ExampleCounter1TDD exampleCounter1;
    private ExampleCounter2TDD exampleCounter2;
}

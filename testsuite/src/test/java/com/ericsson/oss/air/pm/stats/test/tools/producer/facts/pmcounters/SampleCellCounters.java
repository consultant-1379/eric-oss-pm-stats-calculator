/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters;

import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.ExampleCompressed;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.ExampleCounter;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.ExampleCounter1;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.ExampleCounter2;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.ExampleCounter3;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.ExampleCounter4;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.ExampleCounter5;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.ExampleCounter6;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.PdfCounter;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.PmExampleCounter1;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.PmExampleCounter2;

import lombok.Data;

@Data
public class SampleCellCounters {
    private ExampleCounter exampleCounter;
    private ExampleCounter1 exampleCounter1;
    private ExampleCounter2 exampleCounter2;
    private ExampleCounter3 exampleCounter3;
    private ExampleCounter4 exampleCounter4;
    private ExampleCounter5 exampleCounter5;
    private ExampleCounter6 exampleCounter6;
    private PmExampleCounter1 pmExampleCounter1;
    private PmExampleCounter2 pmExampleCounter2;
    private PdfCounter pdfCounter;
    private ExampleCompressed exampleCompressed;
}

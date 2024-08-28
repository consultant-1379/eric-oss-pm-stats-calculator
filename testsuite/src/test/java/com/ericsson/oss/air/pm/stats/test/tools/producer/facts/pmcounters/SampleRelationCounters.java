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

import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.CompressedPdfCounter;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.PdfCounter;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.columns.eu.PmExampleCounter1;

import lombok.Data;

@Data
public class SampleRelationCounters {
    private PmExampleCounter1 pmExampleSingleCounter;
    private PdfCounter pmExamplePdfCounter;
    private CompressedPdfCounter pmExampleCompressedPdfCounter;
}

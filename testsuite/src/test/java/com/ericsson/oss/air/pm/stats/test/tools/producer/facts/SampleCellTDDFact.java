/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools.producer.facts;

import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.SampleCellTDDCounters;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SampleCellTDDFact extends CommonFact {
    private SampleCellTDDCounters pmCounters;
}

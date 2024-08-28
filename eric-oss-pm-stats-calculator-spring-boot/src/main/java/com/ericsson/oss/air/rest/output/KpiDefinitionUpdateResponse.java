/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.output;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

/**
 * <table>
 *  <caption>Available attributes per KPI Definition types</caption>
 *     <tr><th>Attribute            </th><th>ON_DEMAND</th><th>COMPLEX</th><th>SIMPLE</th></tr>
 *     <tr><td>name                 </td><td>OK       </td><td>OK     </td><td>OK    </td></tr>
 *     <tr><td>alias                </td><td>OK       </td><td>OK     </td><td>OK    </td></tr>
 *     <tr><td>aggregationPeriod    </td><td>OK       </td><td>OK     </td><td>OK    </td></tr>
 *     <tr><td>expression           </td><td>OK       </td><td>OK     </td><td>OK    </td></tr>
 *     <tr><td>objectType           </td><td>OK       </td><td>OK     </td><td>OK    </td></tr>
 *     <tr><td>aggregationType      </td><td>OK       </td><td>OK     </td><td>OK    </td></tr>
 *     <tr><td>aggregationElements  </td><td>OK       </td><td>OK     </td><td>OK    </td></tr>
 *     <tr><td>filters              </td><td>OK       </td><td>OK     </td><td>OK    </td></tr>
 *     <tr><td>executionGroup       </td><td>         </td><td>OK     </td><td>NOK   </td></tr>
 *     <tr><td>dataReliabilityOffset</td><td>         </td><td>OK     </td><td>OK    </td></tr>
 *     <tr><td>dataLookbackLimit    </td><td>         </td><td>OK     </td><td>OK    </td></tr>
 *     <tr><td>inpDataIdentifier    </td><td>         </td><td>       </td><td>OK    </td></tr>
 *     <tr><td>reexportLateData     </td><td>         </td><td>OK     </td><td>OK    </td></tr>
 *     <tr><td>exportable           </td><td>OK       </td><td>OK     </td><td>OK    </td></tr>
 * </table>
 */
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KpiDefinitionUpdateResponse {
    private String name;
    private String alias;
    private Integer aggregationPeriod;
    private String expression;
    private String objectType;
    private String aggregationType;
    private List<String> aggregationElements;
    private List<String> filters;
    private String executionGroup;
    private Integer dataReliabilityOffset;
    private Integer dataLookbackLimit;
    private String inpDataIdentifier;
    private Boolean reexportLateData;
    private Boolean exportable;
}

/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.validator.helper.EntityTransformers;

import kpi.model.api.table.definition.ComplexKpiDefinitions;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import lombok.NonNull;

@ApplicationScoped
public class KpiDefinitionRequestValidator {

    public void validateRequestAttributes(@NonNull final KpiDefinitionEntity kpiDefinitionEntity) {
        //  Validation logic called at object construction
        //  There are some validations understood on definitions level - for example:
        //      ValidationResults.validateFiltersAreEmptyWhenExpressionContainsPostAggregationDatasource
        if (kpiDefinitionEntity.isSimple()) {
            final SimpleKpiDefinition simple = EntityTransformers.toSimple(kpiDefinitionEntity);
            SimpleKpiDefinitions.of(List.of(simple));
        } else if (kpiDefinitionEntity.isOnDemand()) {
            final OnDemandKpiDefinition onDemand = EntityTransformers.toOnDemand(kpiDefinitionEntity);
            OnDemandKpiDefinitions.of(List.of(onDemand));
        } else {
            final ComplexKpiDefinition complex = EntityTransformers.toComplex(kpiDefinitionEntity);
            ComplexKpiDefinitions.of(List.of(complex));
        }
    }
}

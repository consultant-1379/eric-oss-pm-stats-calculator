/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import static lombok.AccessLevel.PUBLIC;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.cache.SchemaDetailCache;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;

import kpi.model.api.table.definition.api.InpDataIdentifierAttribute;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class SchemaSubjectHelper {

    @Inject
    private SchemaDetailCache schemaDetailCache;

    public String createSubjectRepresentation(final DataIdentifier dataIdentifier) {
        final SchemaDetail schemaDetail = schemaDetailCache.get(dataIdentifier);

        return String.format("%s.%s", schemaDetail.getNamespace(), dataIdentifier.schema());
    }

    public String createSubjectRepresentation(final InpDataIdentifierAttribute dataIdentifier) {
        final SchemaDetail schemaDetail = schemaDetailCache.get(dataIdentifier);

        return String.format("%s.%s", schemaDetail.getNamespace(), dataIdentifier.schema());
    }
}
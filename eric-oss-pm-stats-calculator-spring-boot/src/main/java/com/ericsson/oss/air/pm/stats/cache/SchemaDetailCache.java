/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.cache;

import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;

import kpi.model.api.table.definition.api.InpDataIdentifierAttribute;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SchemaDetailCache {

    private final ConcurrentHashMap<DataIdentifier, SchemaDetail> cache = new ConcurrentHashMap<>();

    public void put(final DataIdentifier dataIdentifier, final SchemaDetail schemaDetail) {
        cache.put(dataIdentifier, schemaDetail);
    }

    public void put(final InpDataIdentifierAttribute dataIdentifier, final SchemaDetail schemaDetail) {
        put(DataIdentifier.of(dataIdentifier.value()), schemaDetail);
    }

    public boolean hasValue(final DataIdentifier dataIdentifier) {
        return cache.containsKey(dataIdentifier);
    }

    public boolean hasValue(final InpDataIdentifierAttribute dataIdentifier) {
        return hasValue(DataIdentifier.of(dataIdentifier.value()));
    }

    public SchemaDetail get(final DataIdentifier dataIdentifier) {
        return cache.get(dataIdentifier);
    }

    public SchemaDetail get(final InpDataIdentifierAttribute dataIdentifier) {
        return get(DataIdentifier.of(dataIdentifier.value()));
    }

}
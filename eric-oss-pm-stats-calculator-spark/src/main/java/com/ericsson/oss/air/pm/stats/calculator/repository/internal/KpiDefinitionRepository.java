/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.EntityNotFoundException;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KpiDefinitionRepository extends JpaRepository<KpiDefinition, Integer> {
    List<KpiDefinition> findByNameIn(Collection<String> names);

    Optional<KpiDefinition> findByName(String name);

    default KpiDefinition forceFindByName(String name) {
        return findByName(name).orElseThrow(() -> new EntityNotFoundException(String.format("KpiDefinition with name: %s not found", name)));
    }
}
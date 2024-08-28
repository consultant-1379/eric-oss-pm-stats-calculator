/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.custom;

import com.ericsson.oss.air.pm.stats.calculator.model.exception.EntityNotFoundException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
@SuppressWarnings("squid:S119")
public interface ForceFetchRepository<T, ID> extends JpaRepository<T, ID> {
    /**
     * Method to forcefully fetch an entity by id.
     * <br>
     * Forcefully means if entity is not found then it throws a common {@link EntityNotFoundException}.
     *
     * @param id
     *         The id of the entity.
     * @return Entity fetched by id.
     */
    default T forceFetchById(final ID id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(
                String.format("Entity with id '%s' is not found.", id)
        ));
    }
}

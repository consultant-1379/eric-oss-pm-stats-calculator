/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.embedded.id;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.ericsson.oss.air.pm.stats.calculator.api.annotation.Generated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Getter
@Setter
@Builder
@ToString
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class LatestSourceDataId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "aggregation_period_minutes", nullable = false)
    private Integer aggregationPeriodMinutes;

    @Column(name = "execution_group", nullable = false, length = 100)
    private String executionGroup;

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !Objects.equals(Hibernate.getClass(this), Hibernate.getClass(o))) {
            return false;
        }
        final LatestSourceDataId entity = (LatestSourceDataId) o;
        return Objects.equals(source, entity.source)
               && Objects.equals(aggregationPeriodMinutes, entity.aggregationPeriodMinutes)
               && Objects.equals(executionGroup, entity.executionGroup);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(source, aggregationPeriodMinutes, executionGroup);
    }

}
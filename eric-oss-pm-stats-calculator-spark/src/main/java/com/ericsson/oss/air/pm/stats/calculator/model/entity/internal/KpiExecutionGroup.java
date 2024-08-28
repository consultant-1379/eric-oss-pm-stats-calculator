/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model.entity.internal;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.ericsson.oss.air.pm.stats.calculator.api.annotation.Generated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "kpi_execution_groups",
        schema = "kpi",
        indexes = @Index(
                name = "kpi_execution_groups_execution_group_key",
                columnList = "execution_group",
                unique = true)
)
public class KpiExecutionGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "execution_group", nullable = false)
    private String executionGroup;

    @Exclude
    @OneToMany(mappedBy = "executionGroup")
    private Set<LatestProcessedOffset> latestProcessedOffsets = new LinkedHashSet<>();

    @Exclude
    @OneToMany(mappedBy = "executionGroup")
    private Set<KpiDefinition> kpiDefinitions = new LinkedHashSet<>();

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !Objects.equals(Hibernate.getClass(this), Hibernate.getClass(obj))) {
            return false;
        }
        final KpiExecutionGroup that = (KpiExecutionGroup) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    @Generated
    public int hashCode() {
        return getClass().hashCode();
    }
}

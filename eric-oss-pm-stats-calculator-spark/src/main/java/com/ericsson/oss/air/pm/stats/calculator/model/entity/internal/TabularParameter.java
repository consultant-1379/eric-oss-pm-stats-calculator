/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model.entity.internal;

import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "on_demand_tabular_parameters", schema = "kpi", uniqueConstraints = { @UniqueConstraint(columnNames = {"name"}) })
public class TabularParameter {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "tabularParameterId")
    private List<Parameter> columns;

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !Objects.equals(Hibernate.getClass(this), Hibernate.getClass(obj))) {
            return false;
        }

        final TabularParameter that = (TabularParameter) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(id);
    }
}

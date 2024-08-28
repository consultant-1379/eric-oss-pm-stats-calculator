/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model.entity.internal;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.ericsson.oss.air.pm.stats.calculator.api.annotation.Generated;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;

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
@Table(name = "kpi_calculation", schema = "kpi")
public class Calculation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "calculation_id", nullable = false)
    private UUID id;

    @Column(name = "time_created", nullable = false)
    private LocalDateTime timeCreated;

    @Column(name = "time_completed")
    private LocalDateTime timeCompleted;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 32)
    private KpiCalculationState state;

    @Column(name = "execution_group", nullable = false, length = 100)
    private String executionGroup;

    @Exclude
    @OneToMany(mappedBy = "kpiCalculationId")
    private List<ReadinessLog> readinessLog = new ArrayList<>();

    @Column(name = "parameters")
    @Type(type = "org.hibernate.type.TextType")
    private String parameters;

    @Enumerated(EnumType.STRING)
    @Column(name = "kpi_type", nullable = false)
    private KpiType kpiType;

    @Exclude
    @OneToMany(mappedBy = "calculation")
    private List<CalculationReliability> onDemandReliabilities = new ArrayList<>();

    @Exclude
    @ManyToMany
    @JoinTable(
            name = "on_demand_definitions_per_calculation",
            joinColumns = @JoinColumn(name = "calculation_id"),
            inverseJoinColumns = @JoinColumn(name = "kpi_definition_id"))
    private Set<KpiDefinition> onDemandDefinitions = new HashSet<>();

    @Exclude
    @OneToMany(mappedBy = "calculation")
    private List<DimensionTable> dimensionTables;

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !Objects.equals(Hibernate.getClass(this), Hibernate.getClass(obj))) {
            return false;
        }

        final Calculation that = (Calculation) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    @Generated
    public int hashCode() {
        return getClass().hashCode();
    }
}
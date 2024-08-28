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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.ericsson.oss.air.pm.stats.calculator.api.annotation.Generated;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "kpi_definition", schema = "kpi")
@TypeDef(name = "list-array", typeClass = ListArrayType.class)
public class KpiDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 56)
    private String name;

    @Column(name = "alias", nullable = false, length = 61)
    private String alias;

    @Column(name = "expression", nullable = false)
    private String expression;

    @Column(name = "object_type", nullable = false)
    private String objectType;

    @Column(name = "aggregation_type", nullable = false)
    private String aggregationType;

    @Column(name = "aggregation_period", nullable = false)
    private Integer aggregationPeriod;

    @Builder.Default
    @Column(name = "exportable", nullable = false)
    private Boolean exportable = false;

    @Column(name = "schema_data_space")
    private String schemaDataSpace;

    @Column(name = "schema_category")
    private String schemaCategory;

    @Column(name = "schema_name")
    private String schemaName;

    @ManyToOne
    @JoinColumn(name = "execution_group_id")
    private KpiExecutionGroup executionGroup;

    @Column(name = "data_reliability_offset")
    private Integer dataReliabilityOffset;

    @Column(name = "data_lookback_limit")
    private Integer dataLookbackLimit;

    @Column(name = "reexport_late_data")
    private Boolean reexportLateData = false;

    @Column(name ="collection_id", nullable = false)
    private UUID collectionId;

    @Type(type = "list-array")
    @Column(name = "aggregation_elements", columnDefinition = "varchar[] not null")
    private List<String> aggregationElements;

    @Type(type = "list-array")
    @Column(name = "filters", columnDefinition = "varchar[]")
    private List<String> filters = new ArrayList<>();

    @Exclude
    @ManyToMany (mappedBy = "onDemandDefinitions")
    private Set<Calculation> calculations = new HashSet<>();

    @Exclude
    @OneToMany(mappedBy = "kpiDefinition")
    private Set<CalculationReliability> onDemandReliabilities = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "schema_detail_id")
    private SchemaDetail schemaDetail;

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !Objects.equals(Hibernate.getClass(this), Hibernate.getClass(obj))) {
            return false;
        }
        final KpiDefinition that = (KpiDefinition) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    @Generated
    public int hashCode() {
        return getClass().hashCode();
    }
}
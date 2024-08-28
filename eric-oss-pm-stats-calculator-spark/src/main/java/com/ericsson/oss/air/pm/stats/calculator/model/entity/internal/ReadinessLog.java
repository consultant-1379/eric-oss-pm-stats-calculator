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
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
@Table(name = "readiness_log", schema = "kpi")
public class ReadinessLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "collected_rows_count", nullable = false)
    private Long collectedRowsCount;

    @Column(name = "datasource", nullable = false)
    private String datasource;

    @Column(name = "earliest_collected_data", nullable = false)
    private LocalDateTime earliestCollectedData;

    @Column(name = "latest_collected_data", nullable = false)
    private LocalDateTime latestCollectedData;

    @ManyToOne(optional = false)
    @JoinColumn(name = "kpi_calculation_id", nullable = false)
    private Calculation kpiCalculationId;

    public boolean hasDifferentDatasource(@NonNull final ReadinessLog other) {
        return !hasSameDatasource(other);
    }

    public boolean hasSameDatasource(@NonNull final ReadinessLog other) {
        return Objects.equals(datasource, other.getDatasource());
    }

    public boolean hasDifferentCalculationId(@NonNull final ReadinessLog other) {
        return !hasSameCalculationId(other);
    }

    public boolean hasSameCalculationId(@NonNull final ReadinessLog other) {
        final Calculation otherCalculation = Objects.requireNonNull(other.getKpiCalculationId(), "other calculation");

        return Objects.equals(kpiCalculationId.getId(), otherCalculation.getId());
    }

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !Objects.equals(Hibernate.getClass(this), Hibernate.getClass(obj))) {
            return false;
        }
        final ReadinessLog that = (ReadinessLog) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    @Generated
    public int hashCode() {
        return getClass().hashCode();
    }
}

/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.entity;

import com.ericsson.oss.air.pm.stats.calculator.api.annotation.Generated;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Builder(setterPrefix = "with", toBuilder = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "metric", schema = "kpi", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class Metric {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private Long value;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metric metric)) return false;
        return Objects.equals(getId(), metric.getId());
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(getId());
    }
}
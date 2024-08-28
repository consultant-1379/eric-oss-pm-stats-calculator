/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;

import lombok.NonNull;

public enum AggregateFunction {
    MIN {
        @Override
        public String getAggregationFunction() {
            return "MIN";
        }
    },
    MAX {
        @Override
        public String getAggregationFunction() {
            return "MAX";
        }
    };

    public abstract String getAggregationFunction();

    public String surround(@NonNull final Column column) {
        return String.format("%s(%s)", getAggregationFunction(), column.getName());
    }
}

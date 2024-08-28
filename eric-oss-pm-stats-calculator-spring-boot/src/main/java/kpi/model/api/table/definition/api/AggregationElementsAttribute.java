/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.table.definition.api;

import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.common.model.element.AggregationElement;
import com.ericsson.oss.air.pm.stats.common.model.element.Element;

import kpi.model.api.IterableAttribute;

public interface AggregationElementsAttribute<T extends AggregationElement> extends IterableAttribute<T> {

    default List<String> listOfValues() {
        return value().stream().map(Element::value).collect(Collectors.toList());
    }
}

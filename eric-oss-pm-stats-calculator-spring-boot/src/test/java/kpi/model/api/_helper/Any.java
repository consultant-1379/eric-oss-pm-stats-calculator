/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api._helper;

import lombok.Data;

@Data
public class Any {
    @SuppressWarnings("unchecked")
    public static <T> T any() {
        return (T) new Any();
    }

    @Override
    public String toString() {
        return "<any>";
    }
}

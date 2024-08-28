/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.util;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.common.model.attribute.Attribute;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Strings {

    public static String stringifyIterable(@NonNull final Attribute<? extends Collection<?>> attribute) {
        return String.format(
                "%s = [%s]",
                attribute.name(),
                attribute.value().stream().map(Objects::toString).collect(Collectors.joining(", "))
        );
    }
}

/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model._helper;

import java.util.Objects;

import com.google.common.base.Charsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonLoaders {

    @SneakyThrows
    public static String load(final String resourceName) {
        final Thread thread = Thread.currentThread();
        return IOUtils.toString(Objects.requireNonNull(thread.getContextClassLoader().getResourceAsStream(resourceName)), Charsets.UTF_8);
    }
}

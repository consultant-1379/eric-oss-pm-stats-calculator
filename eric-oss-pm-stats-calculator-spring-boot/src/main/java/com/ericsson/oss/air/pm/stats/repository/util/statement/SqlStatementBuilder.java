/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.statement;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

@Data
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlStatementBuilder {
    @NonNull
    private final String template;
    private final Map<String, String> valueMap = new HashMap<>();

    public static SqlStatementBuilder template(final String template) {
        return new SqlStatementBuilder(template);
    }

    public SqlStatementBuilder replace(@NonNull final String placeholder, @NonNull final String value) {
        valueMap.put(placeholder, value);
        return this;
    }

    public SqlStatement create() {
        return SqlStatement.of(StringSubstitutor.replace(template, valueMap));
    }
}

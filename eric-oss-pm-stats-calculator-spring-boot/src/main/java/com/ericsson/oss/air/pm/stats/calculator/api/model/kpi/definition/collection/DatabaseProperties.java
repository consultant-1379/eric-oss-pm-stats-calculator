/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;

import com.ericsson.oss.air.pm.stats.common.model.collection.Database;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Delegate;


@ToString
@EqualsAndHashCode
@NoArgsConstructor(staticName = "newInstance")
public class DatabaseProperties {
    private static final String CONNECTION_URL = "jdbcUrl";

    @Delegate(types = MapDelegate.class)
    private final Map<Database, Properties> map = new HashMap<>();

    private DatabaseProperties(@NonNull final Database database, @NonNull final Properties properties) {
        map.put(database, properties);
    }

    public static DatabaseProperties newInstance(final Database database, final Properties properties) {
        return new DatabaseProperties(database, properties);
    }

    public String getDatabaseJdbcUrl(@NonNull final Database database) {
        final Properties properties = Objects.requireNonNull(
                map.get(database),
                () -> String.format("Database '%s' is not found in the '%s'", database.getName(), getClass().getSimpleName())
        );
        return properties.getProperty(CONNECTION_URL);
    }

    public Properties computeIfAbsent(final Database database) {
        return map.computeIfAbsent(database, properties -> new Properties());
    }

    public Properties getDatabaseProperties(final Database database) {
        final Properties properties = new Properties();
        properties.putAll(map.get(database));
        return properties;
    }

    public Connection connectionTo(final Database database) throws SQLException {
        return DriverManager.getConnection(getDatabaseJdbcUrl(database), getDatabaseProperties(database));
    }

    public interface MapDelegate {
        Properties get(Database key);

        Properties put(Database key, Properties value);

        void forEach(BiConsumer<Database, Properties> action);

        Set<Entry<Database, Properties>> entrySet();
    }
}

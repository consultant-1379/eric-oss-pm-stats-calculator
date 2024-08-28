/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.flyway;


import static javax.ejb.TransactionManagementType.BEAN;
import static org.flywaydb.core.api.ErrorCode.CHECKSUM_MISMATCH;
import static org.flywaydb.core.api.ErrorCode.VALIDATE_ERROR;

import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;

import com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.output.MigrateOutput;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.output.RepairOutput;
import org.flywaydb.core.api.output.RepairResult;

@Slf4j
@Startup
@Singleton
@TransactionManagement(BEAN)
public class FlywayIntegration {
    public static final String DEFAULT_SCHEMA = "kpi";

    @PostConstruct
    private void initialize() {
        final String url = DatabaseProperties.getKpiServiceJdbcServiceConnection();
        final Properties properties = DatabaseProperties.getSuperUserJdbcProperties();

        log.info("Running Flyway database migration scripts");

        final FluentConfiguration configuration = Flyway.configure();
        configuration.dataSource(url, properties.getProperty("user"), properties.getProperty("password"));
        configuration.baselineVersion("0");
        configuration.baselineOnMigrate(true);
        configuration.validateMigrationNaming(true);
        configuration.defaultSchema(DEFAULT_SCHEMA);

        final Flyway flyway = configuration.load();

        if (isNonInitialDeployment(flyway)) {
            log.info("Flyway detected existing migrations, do repair validation errors if needed");
            repairValidationErrors(flyway);
        }

        migrate(flyway);
    }

    /**
     * Returns true if <strong>Flyway</strong> has already executed migration(s) in previous deployments.
     * <br>
     * This precondition check before {@link #repairValidationErrors(Flyway)} is essential as if there is no <strong>kpi</strong> schema
     * then {@link Flyway#validate()} fails on <pre>{@code
     *      org.flywaydb.core.api.exception.FlywayValidateException:
     *      Validate failed: Schema "kpi" doesn't exist yet
     * }</pre>
     *
     * @param flyway the {@link Flyway}
     * @return true if non-initial deployment otherwise false
     */
    private boolean isNonInitialDeployment(final Flyway flyway) {
        final MigrationInfoService migrationInfoService = flyway.info();
        return migrationInfoService.current() != null;
    }

    private void migrate(final Flyway flyway) {
        final MigrateResult migrate = flyway.migrate();
        if (migrate.migrations.isEmpty()) {
            final MigrationInfo migrationInfo = flyway.info().current();
            log.info("Flyway is up-to-date '{}', no migration has been executed", prettyMigrationInfo(migrationInfo));
        } else {
            for (final MigrateOutput migrateOutput : migrate.migrations) {
                log.info("Flyway migration script '{}' took '{}' ms", prettyMigrationInfo(migrateOutput), migrateOutput.executionTime);
            }
        }
    }

    private void repairValidationErrors(final Flyway flyway) {
        try {
            flyway.validate();
        } catch (final FlywayException flywayException) {
            if (flywayException.getErrorCode() == VALIDATE_ERROR) {
                if (flywayException.getMessage().contains("Migration checksum mismatch for migration version 001")) {
                    //  V001 - Failed on production migration as there was on_demand_bulk_parameters table created by admin user
                    //  so new exception handling was added
                    final RepairResult repair = flyway.repair();

                    repair.repairActions.forEach(repairAction -> log.info("Executed repair action: {}", repairAction));
                    repair.migrationsAligned.forEach(repairOutput -> log.info("Migration aligned '{}'", prettyRepairOutput(repairOutput)));

                    log.error("Flyway validation failed for '{}' and has been repaired", CHECKSUM_MISMATCH, flywayException);
                } else if (flywayException.getMessage().contains("Detected resolved migration not applied to database")) {
                    log.info("There are un-applied versioned migration files");
                } else {
                    throw flywayException;
                }
            } else {
                throw flywayException;
            }
        }
    }

    private String prettyRepairOutput(final RepairOutput repairOutput) {
        return prettyVersionDescription(repairOutput.version, repairOutput.description);
    }

    private String prettyMigrationInfo(final MigrateOutput migrateOutput) {
        return prettyVersionDescription(migrateOutput.version, migrateOutput.description);
    }

    private String prettyMigrationInfo(final MigrationInfo migrationInfo) {
        return prettyVersionDescription(migrationInfo.getVersion().getVersion(), migrationInfo.getDescription());
    }

    private String prettyVersionDescription(final String version, final String description) {
        return String.format("(V%s) %s", version, description);
    }

}

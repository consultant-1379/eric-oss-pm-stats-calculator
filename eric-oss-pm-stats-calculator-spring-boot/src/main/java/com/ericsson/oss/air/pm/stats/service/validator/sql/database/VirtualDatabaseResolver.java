/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.alias;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.column;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.relation;
import static com.ericsson.oss.air.pm.stats.model.exception.KpiDefinitionValidationException.badRequest;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.filter.RelationFilterer.relationFilterer;
import static lombok.AccessLevel.PUBLIC;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlExtractorService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlRelationExtractor;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabases;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.resolver.DatabaseResolver;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;

import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor(access = PUBLIC) /* Internal testing only */
public class VirtualDatabaseResolver {
    @Inject
    private SqlExtractorService extractorService;
    @Inject
    private SqlRelationExtractor relationExtractor;

    @SuppressWarnings({"squid:S1602", "squid:S3776"})
    public ResolutionResult resolveReferences(final VirtualDatabases virtualDatabases, final KpiDefinition kpiDefinition, final int aggregationPeriod) {
        final Set<Reference> references = references(kpiDefinition);
        final Set<Relation> relations = relationExtractor.extractColumns(kpiDefinition);

        final ResolutionResult resolutionResult = new ResolutionResult(kpiDefinition);
        final DatabaseResolver databaseResolver = new DatabaseResolver(aggregationPeriod, virtualDatabases);

        //  Notation:
        //      <!>    - it does not matter if the value is present or not
        //      <?>    - do not know if it has value or not
        //      <null> - has no value
        //  RELATION:  <datasource>.<table> AS <alias>
        //  REFERENCE: <datasource>.<table>.<column> AS <alias>
        for (final Reference reference : references) {
            reference.alias().ifPresentOrElse(alias -> {
                reference.column().ifPresentOrElse(column -> {
                    reference.table().ifPresentOrElse(table -> {
                        reference.datasource().ifPresentOrElse(datasource -> {
                            //  REFERENCE: <datasource>.<table>.<column> AS <alias>
                            //  RELATION:  <datasource>.<table> AS <!>
                            relationFilterer(relations).onDatasources(datasource).onTables(table).ifPresentOrElse(relation -> {
                                if (databaseResolver.canResolve(datasource, table, column)) {
                                    resolutionResult.addResolvedResolution(relation, reference);
                                } else {
                                    resolutionResult.addUnresolvedResolution(relation, reference);
                                }
                            }, () -> {
                                resolutionResult.addUnresolvedResolution(null, reference);
                            });
                        }, () -> {
                            //  REFERENCE: <null>.<table>.<column> AS <alias>
                            //  RELATION:  <!>.<table> AS <alias>

                            final Set<Relation> filteredRelations = relationFilterer(relations).onTableOrAlias(table, alias).collect();
                            databaseResolver.anyResolve(filteredRelations, table, column).ifPresentOrElse(relation -> {
                                resolutionResult.addResolvedResolution(relation, reference);
                            }, () -> {
                                resolutionResult.addUnresolvedResolution(null, reference);
                            });
                        });
                    }, () -> {
                        //  REFERENCE: <null>.<null>.<column> AS <alias>
                        //  RELATION:  <!>.<table> AS <alias>
                        //  If <table> is null then <datasource> must be null as well - validate it
                        ensureMissing(reference.datasource(), reference);

                        databaseResolver.anyResolve(relations, null, column).ifPresentOrElse(relation -> {
                            resolutionResult.addResolvedResolution(relation, reference);
                        }, () -> {
                            resolutionResult.addUnresolvedResolution(null, reference);
                        });
                    });

                }, () -> {
                    //  REFERENCE: <null>.<null>.<null> AS <alias>
                    //  If <column> is null then <datasource> and <table> must be null as well - validate it
                    ensureMissing(reference.datasource(), reference);
                    ensureMissing(reference.table(), reference);

                    databaseResolver.anyResolve(relations, null, column(alias.name())).ifPresentOrElse(relation -> {
                        resolutionResult.addResolvedResolution(relation, reference);
                    }, () -> {
                        //  Try to locate the KPI Definition output table and check if that table contains the column for the alias
                        virtualDatabases.locateTable(KPI_DB, column(resolutionResult.kpiDefinitionName())).ifPresentOrElse(table -> {
                            if (databaseResolver.canResolve(KPI_DB, table, column(alias.name()))) {
                                resolutionResult.addResolvedResolution(relation(KPI_DB, table, null), reference);
                            } else {
                                resolutionResult.addUnresolvedResolution(null, reference);
                            }
                        }, () -> {
                            resolutionResult.addUnresolvedResolution(null, reference);
                        });
                    });
                });
            }, () -> {
                reference.column().ifPresentOrElse(column -> {
                    reference.table().ifPresentOrElse(table -> {
                        reference.datasource().ifPresentOrElse(datasource -> {
                            //  REFERENCE: <datasource>.<table>.<column> AS <null>
                            //  RELATION:  <datasource>.<table> AS <!>
                            relationFilterer(relations).onDatasources(datasource).onTables(table).ifPresentOrElse(relation -> {
                                if (databaseResolver.canResolve(datasource, table, column)) {
                                    resolutionResult.addResolvedResolution(relation, reference);
                                } else {
                                    resolutionResult.addUnresolvedResolution(relation, reference);
                                }
                            }, () -> {
                                resolutionResult.addUnresolvedResolution(null, reference);
                            });
                        }, () -> {
                            //  REFERENCE: <null>.<table>.<column> AS <null>
                            //  RELATION:  <!>.<table> AS <alias>
                            final Set<Relation> filteredRelations = relationFilterer(relations).onTableOrAlias(table, alias(table.getName())).collect();
                            databaseResolver.anyResolve(filteredRelations, table, column).ifPresentOrElse(relation -> {
                                resolutionResult.addResolvedResolution(relation, reference);
                            }, () -> {
                                resolutionResult.addUnresolvedResolution(null, reference);
                            });
                        });
                    }, () -> {
                        //  REFERENCE: <null>.<null>.<column> AS <null>
                        //  RELATION:  <!>.<table> AS <alias>
                        //  If <table> is null then <datasource> must be null as well - validate it
                        ensureMissing(reference.datasource(), reference);

                        databaseResolver.anyResolve(relations, null, column).ifPresentOrElse(relation -> {
                            resolutionResult.addResolvedResolution(relation, reference);
                        }, () -> {
                            resolutionResult.addUnresolvedResolution(null, reference);
                        });
                    });
                }, () -> {
                    //  REFERENCE: <?>.<?>.<null> AS <null>
                    throw badRequest(String.format(
                            "For '%s' '%s' '%s' and '%s' missing",
                            Reference.class.getSimpleName(), reference, Table.class.getSimpleName(), Alias.class.getSimpleName()
                    ));
                });
            });
        }

        ensureAllReferenceConsumed(references, resolutionResult);

        return resolutionResult;
    }

    private static void ensureAllReferenceConsumed(@NonNull final Collection<Reference> references, final ResolutionResult resolutionResult) {
        for (final Reference reference : references) {
            if (resolutionResult.contains(reference)) {
                continue;
            }

            resolutionResult.addUnresolvedResolution(null, reference);
        }
    }

    private Set<Reference> references(final KpiDefinition kpiDefinition) {
        if (kpiDefinition instanceof ComplexKpiDefinition) {
            return extractorService.extractColumns((ComplexKpiDefinition) kpiDefinition);
        }

        if (kpiDefinition instanceof OnDemandKpiDefinition) {
            return extractorService.extractColumns((OnDemandKpiDefinition) kpiDefinition);
        }

        throw badRequest(String.format(
                "'%s' is not supported for reference validation", kpiDefinition.getClass().getSimpleName()
        ));
    }

    private static <T> void ensureMissing(@NonNull final Optional<T> optional, final Reference reference) {
        optional.ifPresent(value -> {
            throw new IllegalArgumentException(String.format("'%s' is present for '%s'", value.getClass().getSimpleName(), reference));
        });
    }


}

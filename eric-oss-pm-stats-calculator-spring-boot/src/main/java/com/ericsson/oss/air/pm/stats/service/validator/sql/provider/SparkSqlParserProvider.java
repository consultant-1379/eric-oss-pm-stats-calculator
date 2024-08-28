/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.provider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.execution.SparkSqlParser;

@Slf4j
@ApplicationScoped
public class SparkSqlParserProvider {

    @Produces
    public SparkSqlParser provideSqlParser() {
        final SparkSqlParser sparkSqlParser = new SparkSqlParser();
        log.info("'{}' loaded successfully", SparkSqlParser.class.getSimpleName());
        return sparkSqlParser;
    }

}

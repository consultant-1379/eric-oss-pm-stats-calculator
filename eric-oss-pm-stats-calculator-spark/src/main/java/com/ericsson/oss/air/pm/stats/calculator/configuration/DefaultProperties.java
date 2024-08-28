/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration;

import java.util.Properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultProperties {

    public static Properties defaultProperties() {
        final SparkConf sparkConf = SparkSession.builder().getOrCreate().sparkContext().getConf();

        final int counter = Integer.parseInt(sparkConf.get("spark.calculation.counter"));
        final String hibernateDDL = counter > 1 ? "none" : "validate";

        log.info("Spring started for the '{}' time, spring.jpa.hibernate.ddl-auto is set to '{}'", counter, hibernateDDL);

        final Properties properties = new Properties();
        properties.setProperty("spring.jpa.hibernate.ddl-auto", hibernateDDL);
        return properties;
    }
}

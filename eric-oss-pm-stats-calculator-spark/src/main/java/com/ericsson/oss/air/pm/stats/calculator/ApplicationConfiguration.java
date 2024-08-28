/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import com.ericsson.oss.air.pm.stats.calculator.configuration.KafkaConfiguration;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.DataLoader;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.api.FilterHandler;
import com.ericsson.oss.air.pm.stats.calculator.dataset.offset.OffsetPersistency;
import com.ericsson.oss.air.pm.stats.calculator.dataset.writer.api.DatasetWriter;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.calculator.service.calculator.api.CalculatorHandler;
import com.ericsson.oss.air.pm.stats.calculator.service.util.UserDefinedFunctionRegisterImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandler;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandlerPostgres.AggregationTimestampCache;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.plugin.core.config.EnablePluginRegistries;

@Slf4j
@Configuration
@EnableCaching
@Import(KafkaConfiguration.class)
@EntityScan(basePackages = "com.ericsson.oss.air.pm.stats.calculator.model.entity")
@EnableJpaRepositories(basePackages = "com.ericsson.oss.air.pm.stats.calculator.repository")
@ConfigurationPropertiesScan(basePackages = "com.ericsson.oss.air.pm.stats.calculator.configuration.property")
@EnablePluginRegistries({OffsetHandler.class, FilterHandler.class, CalculatorHandler.class, DatasetWriter.class, DataLoader.class, OffsetPersistency.class})
public class ApplicationConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public SparkSession sparkSession(@NonNull final UserDefinedFunctionRegisterImpl userDefinedFunctionRegister){
        final SparkSession sparkSession = SparkSession.builder().getOrCreate();

        userDefinedFunctionRegister.register(sparkSession);

        return sparkSession;
    }

    @Bean
    public DatasourceRegistry datasourceRegistry() {
        return DatasourceRegistry.getInstance();    //  TODO: Create Spring Boot managed Cache for this later on
    }

    @Bean
    public KpiDefinitionHierarchy kpiDefinitionHierarchy(final KpiDefinitionService kpiDefinitionService) {
        return new KpiDefinitionHierarchy(kpiDefinitionService.loadDefinitionsToCalculate());
    }

    @Bean
    public AggregationTimestampCache aggregationPeriodStartTimeStamp() {
        return AggregationTimestampCache.of();
    }

    @Bean
    public AggregationTimestampCache aggregationPeriodEndTimeStamp() {
        return AggregationTimestampCache.of();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public Logger logger(@NonNull final InjectionPoint injectionPoint) {
        return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass());
    }

}

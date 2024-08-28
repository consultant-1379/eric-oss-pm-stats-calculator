/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.startup.health.check;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.service.startup.health.HealthCheckMonitor;
import com.ericsson.oss.air.pm.stats.service.startup.health.check.api.HealthCheckComponent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.KafkaException;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
@DependsOn("kpiStartupService")
public class KafkaHealthCheckComponent implements HealthCheckComponent {
    private CalculatorProperties calculatorProperties;
    private HealthCheckMonitor healthCheckMonitor;

    @Override
    public Component getComponent() {
        return Component.KAFKA;
    }

    @Override
    @Scheduled(fixedRate = 30_000, initialDelay = 5_000)
    public void execute() {
        log.debug("Checking health of Kpi Service Kafka Bootstrap Servers");

        final Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, calculatorProperties.getKafkaBootstrapServers());
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000);
        properties.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 5000);

        try (final AdminClient adminClient = AdminClient.create(properties)) {
            adminClient.describeCluster().nodes().get();
            healthCheckMonitor.markHealthy(getComponent());
        } catch (InterruptedException | ExecutionException | KafkaException e) { //NOSONAR Exception suitably logged
            log.warn("Kafka health check failed");
            healthCheckMonitor.markUnHealthy(getComponent());
        }
    }
}
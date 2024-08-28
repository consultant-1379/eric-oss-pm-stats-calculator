# How to configure External Data Stores for kpi-service

The structure below can be included in the values.yaml to specify External Data Stores needed by
eric-oss-pm-stats-calculator.

Or, if being deployed from a parent chart, the values can be set in the parent chart and override with the "known"
values for the DBs as deployed by the services which own these data stores.

```yaml
externalDataSources:
  pm-stats:
    kubernetesSecretName: eric-pm-stats-processor-er
    driver: "org.postgresql.Driver"
    jdbcUrl: "jdbc:postgresql://eric-pm-stats-processor-data-v2:5432/pm_stats_service_db"
    type: FACT
  pm-events:
    kubernetesSecretName: eric-pm-events-processor-er
    driver: "org.postgresql.Driver"
    jdbcUrl: "jdbc:postgresql://eric-pm-events-processor-data-v2:5432/pm_events_service_db"
    type: FACT
  cm:
    kubernetesSecretName: eric-cm-topology-model-sn
    driver: org.postgresql.Driver
    jdbcUrl: jdbc:postgresql://eric-cm-son-topology-data:5432/cm_service_db
    type: DIM
```

# How to enable and update CNOM dashboard configurations.
To enable/disable CNOM dashboards simply set the below parameter to true or false respectively in values.yaml.
```yaml
CNOM:
  enabled: true/false
```

CNOM dashboards are configured via configmap templates and they need to include the following label:
```
ericsson.com/cnom-server-dashboard-models: "true"
```

This will allow the CNOM service to pick them up automatically.

## RED/USE templates:
```
configmap-cnom-red.yaml
configmap-cnon-use.yaml
```


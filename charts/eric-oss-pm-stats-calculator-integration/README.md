# How to run Helm Test for KPI Calculator Service

These commands have been created to closely mirror the commands from the Jenkinsfile
to ensure that both local Helm Test and kpi-service Jenkins pipeline are using
the same environment and test commands

There are certain differences because local testing does not have BOB version
support. These are included in the extra environment variables:

* HELM_SET_EXTRA
* HELM_SET_EXTRA_INT

In addition, the commands do not wait for a timeout but return immediately to allow
the console to be used for progress monitoring. It is up to the user to ensure the
K8s dashboard is green before running the next command

## Change to kpi-service directory within your workspace

```bash
cd <WORKSPACE>/kpi-service
```

## Specify unique values for namespace and docker tag

Replace the two values below

* nnn is any unique number/label for your signum/namespace - 1,2,3, 10,20,30, 20191225T1730 ...
** e.g. export ERIC_PM_KPI_CALCULATOR_TAGNUM=20191225T1730
* your-signum is your signum or username to ensure docker tag is unique
** e.g. export SIGNUM=ehaygre

```bash
export ERIC_PM_KPI_CALCULATOR_TAGNUM=nnn
export SIGNUM=ehaygre
```

## Prepare environment

```bash
export ERIC_PM_KPI_CALCULATOR_TAG=${SIGNUM}-${ERIC_PM_KPI_CALCULATOR_TAGNUM}
export ERIC_PM_KPI_CALCULATOR_INT_TAG=int-${ERIC_PM_KPI_CALCULATOR_TAG}
export REPO_PATH_DEV=proj-ec-son-dev

export SERVICE_NAME="eric-oss-pm-stats-calculator"
export HELM_INSTALL_RELEASE_NAME=kpi-service
export HELM_INSTALL_RELEASE_NAME_INT=kpi-service-int
export HELM_CHART_PACKAGED=charts/${SERVICE_NAME}
export HELM_CHART_PACKAGED_INT=charts/${SERVICE_NAME}-integration

export HELM_INSTALL_NAMESPACE=${SIGNUM}
export HELM_INSTALL_TIMEOUT=1800
export INT_TEST_RELEASE_NAME=${HELM_INSTALL_RELEASE_NAME_INT}

export HELM_SET="imageCredentials.pullSecret=${SERVICE_NAME}-secret,zookeeper.persistence.persistentVolumeClaim.storageClassName=,zookeeper.persistence.persistentVolumeClaim.size=1Gi,kafka.persistence.persistentVolumeClaim.enabled=false"

export HELM_SET_INT="${HELM_SET},eric-nfs-provisioner-pm-events.enabled=true,calculationSchedule=,hourlyCalculationSchedule=,maxLookBackPeriodInMinutes=ALL,externalDataSources.pm-stats.kubernetesSecretName=eric-pm-stats-processor-er,externalDataSources.pm-stats.driver=org.postgresql.Driver,externalDataSources.pm-stats.jdbcUrl=jdbc:postgresql://eric-pm-stats-processor-data-v2:5432/pm_stats_service_db,externalDataSources.pm-stats.type=FACT,externalDataSources.pm-events.kubernetesSecretName=eric-pm-events-processor-er,externalDataSources.pm-events.driver=org.postgresql.Driver,externalDataSources.pm-events.jdbcUrl=jdbc:postgresql://eric-pm-events-processor-data-v2:5432/pm_events_service_db,externalDataSources.pm-events.type=FACT,externalDataSources.cm.kubernetesSecretName=eric-cm-topology-model-sn,externalDataSources.cm.driver=org.postgresql.Driver,externalDataSources.cm.jdbcUrl=jdbc:postgresql://eric-cm-son-topology-data:5432/cm_service_db,externalDataSources.cm.type=DIM"

export HELM_SET_EXTRA=imageCredentials.repoPath=${REPO_PATH_DEV},images.eric-oss-pm-stats-calculator.tag=${ERIC_PM_KPI_CALCULATOR_TAG}

export HELM_SET_EXTRA_INT=images.eric-oss-pm-stats-calculator-integration.name=${SERVICE_NAME},images.eric-oss-pm-stats-calculator-integration.tag=${ERIC_PM_KPI_CALCULATOR_INT_TAG},images.eric-oss-pm-stats-calculator-integration.repoPath=${REPO_PATH_DEV}

helm dependency update ${HELM_CHART_PACKAGED}
helm dependency update ${HELM_CHART_PACKAGED_INT}
```

## Build and push docker images to registry

```bash
mvn clean install
docker build . -t armdocker.rnd.ericsson.se/${REPO_PATH_DEV}/eric-oss-pm-stats-calculator:${ERIC_PM_KPI_CALCULATOR_TAG}
docker push armdocker.rnd.ericsson.se/${REPO_PATH_DEV}/eric-oss-pm-stats-calculator:${ERIC_PM_KPI_CALCULATOR_TAG}

cd testsuite/src/test/docker/helm-integration/
docker build . -t armdocker.rnd.ericsson.se/${REPO_PATH_DEV}/eric-oss-pm-stats-calculator:${ERIC_PM_KPI_CALCULATOR_INT_TAG}
docker push armdocker.rnd.ericsson.se/${REPO_PATH_DEV}/eric-oss-pm-stats-calculator:${ERIC_PM_KPI_CALCULATOR_INT_TAG}
cd -
```

## Install KPI Calculator chart

```bash
helm upgrade --install ${HELM_INSTALL_RELEASE_NAME} ${HELM_CHART_PACKAGED} --set ${HELM_SET} --namespace ${HELM_INSTALL_NAMESPACE} --devel --set ${HELM_SET_EXTRA}
```

WAIT FOR DASHBOARD TO SHOW GREEN!

## Install KPI Calculator Integration Test chart

```bash
helm install --name ${INT_TEST_RELEASE_NAME} ${HELM_CHART_PACKAGED_INT} --set imageCredentials.pullSecret=${SERVICE_NAME}-secret --namespace ${HELM_INSTALL_NAMESPACE} --set ${HELM_SET_EXTRA} --set ${HELM_SET_EXTRA_INT}
```

WAIT FOR DASHBOARD TO SHOW GREEN!

## Upgrade KPI Calculator chart with external datasource parameters

```bash
helm upgrade --install ${HELM_INSTALL_RELEASE_NAME} ${HELM_CHART_PACKAGED} --set "${HELM_SET_INT}" --namespace ${HELM_INSTALL_NAMESPACE} --devel --set ${HELM_SET_EXTRA}
```

WAIT FOR DASHBOARD TO SHOW GREEN!

## Run Helm Test with timeout

```bash
helm test ${INT_TEST_RELEASE_NAME} --timeout ${HELM_INSTALL_TIMEOUT}
```

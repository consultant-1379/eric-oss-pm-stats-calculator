#!/bin/bash
#
# COPYRIGHT Ericsson 2023
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#

HELM_TEST_LOCAL=$(pwd)
PROJECT_ROOT="${HELM_TEST_LOCAL}/.."

USER=$(echo $(whoami) | tr '[:upper:]' '[:lower:]' | sed -e 's/\([[:alpha:]]*\)[[:punct:]]\([[:alpha:]]*\)/\2/')
TAG="${USER}-$(date +%Y-%m-%d-%H-%M-%S)"

# Service Requirements
SERVICE_NAME="eric-oss-pm-stats-calculator"
SERVICE_TAG="$TAG"
SERVICE_VALUES_FILE="${HELM_TEST_LOCAL}/values-kpi-service.yaml"
SERVICE_RELEASE_NAME=${SERVICE_NAME}
SERVICE_SECRET=""
SECRET_NAME="armdocker"

# Integration Requirements
SERVICE_INTEGRATION_NAME="${SERVICE_NAME}-integration"
SERVICE_INTEGRATION_TAG="$TAG"
SERVICE_INTEGRATION_RELEASE_NAME=${SERVICE_INTEGRATION_NAME}

NO_COLOR='\033[0m'
GREEN='\033[0;32m'
RED='\033[0;31m'

TIMEOUT_INT_INSTALL="300s"
TIMEOUT_UPGRADE="300s"
TIMEOUT_HELM_TESTS="2000s"
TIMEOUT_DEPLOYMENT_READY="300s"

function log() {
  echo -e "\n${GREEN} --- ${1} --- ${NO_COLOR}\n"
}

function checkExitCode() {
  if [ $? -ne 0 ]; then
    echo -e "\n${RED} --- ERROR: $1 --- ${NO_COLOR}\n"
    exit 255
  fi
}

function addRepos() {
  log "Adding repositories"
  helm repo add proj-ec-son-drop-helm https://arm.seli.gic.ericsson.se/artifactory/proj-ec-son-drop-helm
  helm repo add proj-adp-gs-all-helm https://arm.sero.gic.ericsson.se/artifactory/proj-adp-gs-all-helm
  helm repo add proj-eric-oss-released-helm-local https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-released-helm-local --username ${SELI_ARTIFACTORY_REPO_USER} --password ${SELI_ARTIFACTORY_REPO_PASS}
}

function getUserOptions() {
  userInput "Do you wish to run Integration Tests on ECCD/KaaS server (Yes/No)? [y/n] ?"
  remote="$?"
  userInput "Do you want this script to clean the environment after (Yes/No)? [y/n] ?"
  cleanAfter="$?"
  userInput "Do you want to initialize Helm repositories (Yes/No)? [y/n] ?"
  initRepositories="$?"
  if [[ "$remote" -eq 1 && "$SKIP_KUBERNETES_NS_CHECK" != true ]]; then
    log "Listing available Namespaces"
    kubectl get namespace | awk '{ print $1 }'
    provideNamespace "Please use a namespace given to the allocated cluster! You can provide it here. (eg. ns-velociraptorsX)"
    local namespaces
    namespaces=$(kubectl get namespaces -o jsonpath='{.items[*].metadata.name}')
    while [[ -z "$NAMESPACE" || $(echo "$namespaces" | grep -c "$NAMESPACE") -eq 0 ]]; do
      provideNamespace "Provided Namespace does not exist, retry"
    done
  elif [[ -z "$NAMESPACE" ]]; then
    provideNamespace "Please use a namespace given to the allocated cluster! You can provide it here. (eg. ns-velociraptorsX)"
  fi
  echo "Your namespace is ${NAMESPACE}"
}

function userInput() {
  local _prompt _default _response

  _prompt=$1

  while true; do
    read -r -p "$_prompt " _response
    case "$_response" in
    [Yy][Ee][Ss] | [Yy])
      echo "Yes"
      return 1
      ;;
    [Nn][Oo] | [Nn])
      echo "No"
      return 0
      ;;
    *) # Anything else (including a blank) is invalid.
      ;;
    esac
  done
}

function provideNamespace() {
  local _prompt _response

  if [[ -z "$DEFAULT_NAMESPACE" ]]; then
    DEFAULT_NS_MESSAGE=""
  else
    DEFAULT_NS_MESSAGE=" [$DEFAULT_NAMESPACE]"
  fi

  _prompt=$1

  while true; do
    read -r -p "$_prompt$DEFAULT_NS_MESSAGE: " _response
    NAMESPACE="$_response"
    if [[ -z "$NAMESPACE" ]]; then
      NAMESPACE="$DEFAULT_NAMESPACE"
    fi
    return
  done
}

function ensureOnCorrectServer() {
  if [[ "$remote" -eq 0 && -n "$KUBECONFIG" ]]; then
    log "Changing KUBECONFIG from ${KUBECONFIG} to empty."
    export KUBECONFIG=""
  fi
}

function applyCorrectValues() {
  if [[ $remote -eq 1 ]]; then
    log "Deploying to the cluster with appropriate values"
    SERVICE_VALUES_FILE="${HELM_TEST_LOCAL}/values-kpi-service-cluster.yaml"
  fi
}

function initRepo() {
  if [ "$initRepositories" -eq 1 ]; then
    log "Initializing repositories..."

    rm -rf "${PROJECT_ROOT}/charts/${SERVICE_NAME}/charts"
    rm -rf "${PROJECT_ROOT}/charts/${SERVICE_NAME}/tmpcharts"
    rm -rf "${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}/charts/*.tgz"
    rm -rf "${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}/tmpcharts"

    cd "${PROJECT_ROOT}/charts/${SERVICE_NAME}"
    helm dependency update

    checkExitCode "Failed to update ${SERVICE_NAME} dependencies!"

    cd "${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}"
    helm dependency update

    checkExitCode "Failed to update ${SERVICE_INTEGRATION_NAME} dependencies!"
  fi
}

function cleanUpHelmEnvironment() {

  log "Cleaning up Helm environment"
  if [[ "$remote" -eq 0 ]]; then
    log "Deleting namespace ${NAMESPACE}"
    kubectl delete namespace $NAMESPACE
  fi

  helm uninstall $SERVICE_NAME -n $NAMESPACE
  helm uninstall $SERVICE_INTEGRATION_NAME -n $NAMESPACE

  PODS=$(kubectl get pod -n $NAMESPACE | awk '{ print $1 }' | grep -v NAME | grep eric-oss-pm-stats-calculator-integration)

  for POD in $PODS; do
    kubectl delete pod -n ${NAMESPACE} ${POD}
  done

  log "Removing old PV's"

  PVCS=$(kubectl get pvc -n $NAMESPACE | awk '{ print $1 }' | grep -v NAME)

  for PVC in $PVCS; do
    sleep 5s
    kubectl delete pvc $PVC -n $NAMESPACE --force --grace-period=0
  done
}

function buildDockerImages() {
  log "Building Docker Images"
  cd $PROJECT_ROOT
  mvn clean install -V -DskipTests=true -Denforcer.skip
  checkExitCode "Failed to build the project"

  log "Building armdocker.rnd.ericsson.se/proj-eric-oss-dev/${SERVICE_NAME}:${SERVICE_TAG}"
  docker build . -t armdocker.rnd.ericsson.se/proj-eric-oss-dev/$SERVICE_NAME:$SERVICE_TAG
  checkExitCode "Failed to build armdocker.rnd.ericsson.se/proj-eric-oss-dev/${SERVICE_NAME}:${SERVICE_TAG}"

  log "Building armdocker.rnd.ericsson.se/proj-eric-oss-dev/${SERVICE_INTEGRATION_NAME}:${SERVICE_INTEGRATION_TAG}"
  cd "testsuite/src/test/docker/helm-integration/"
  docker build . -t armdocker.rnd.ericsson.se/proj-eric-oss-dev/$SERVICE_INTEGRATION_NAME:$SERVICE_INTEGRATION_TAG
  checkExitCode "Failed to build armdocker.rnd.ericsson.se/proj-eric-oss-dev/${SERVICE_INTEGRATION_NAME}:${SERVICE_INTEGRATION_TAG}"

  if [[ "$remote" -eq 1 ]]; then
    docker push armdocker.rnd.ericsson.se/proj-eric-oss-dev/${SERVICE_NAME}:${SERVICE_TAG}
    docker push armdocker.rnd.ericsson.se/proj-eric-oss-dev/${SERVICE_INTEGRATION_NAME}:${SERVICE_INTEGRATION_TAG}
  fi

  cd ${HELM_TEST_LOCAL}
}

function packageIntegrationService() {
  log "Packaging ${SERVICE_INTEGRATION_NAME}"
  sed -i s/VERSION/${TAG}/g ${PROJECT_ROOT}/charts/eric-oss-pm-stats-calculator-integration/eric-product-info.yaml
  sed -i s/REPO_PATH/proj-eric-oss-dev/g ${PROJECT_ROOT}/charts/eric-oss-pm-stats-calculator-integration/eric-product-info.yaml
  helm package "${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}" --destination "${PROJECT_ROOT}/helm-test-local/"
  sed -i s/${TAG}/VERSION/g ${PROJECT_ROOT}/charts/eric-oss-pm-stats-calculator-integration/eric-product-info.yaml
  sed -i s/proj-eric-oss-dev/REPO_PATH/g ${PROJECT_ROOT}/charts/eric-oss-pm-stats-calculator-integration/eric-product-info.yaml
}

function installIntegrationService() {
  packageIntegrationService
  log "Installing ${SERVICE_INTEGRATION_NAME}"
  cd ${PROJECT_ROOT}
  INSTALL_COMMAND_INT="upgrade ${SERVICE_INTEGRATION_RELEASE_NAME} ${PROJECT_ROOT}/helm-test-local/${SERVICE_INTEGRATION_RELEASE_NAME}-0.0.0.tgz \
                               --install \
                               --namespace ${NAMESPACE} \
                               --wait \
                               --timeout ${TIMEOUT_INT_INSTALL} \
                               ${SERVICE_SECRET}"
  echo "helm" ${INSTALL_COMMAND_INT}
  helm ${INSTALL_COMMAND_INT}
}

function packageService() {
  log "Packaging ${SERVICE_NAME}"
  sed -i s/VERSION/${TAG}/g ${PROJECT_ROOT}/charts/eric-oss-pm-stats-calculator/eric-product-info.yaml
  sed -i s/REPO_PATH/proj-eric-oss-dev/g ${PROJECT_ROOT}/charts/eric-oss-pm-stats-calculator/eric-product-info.yaml
  helm package "${PROJECT_ROOT}/charts/${SERVICE_NAME}" --destination "${PROJECT_ROOT}/helm-test-local/"
  sed -i s/${TAG}/VERSION/g ${PROJECT_ROOT}/charts/eric-oss-pm-stats-calculator/eric-product-info.yaml
  sed -i s/proj-eric-oss-dev/REPO_PATH/g ${PROJECT_ROOT}/charts/eric-oss-pm-stats-calculator/eric-product-info.yaml
}

function installService() {
  packageService
  log "Installing ${SERVICE_NAME}"
  INSTALL_COMMAND="upgrade ${SERVICE_RELEASE_NAME} ${PROJECT_ROOT}/helm-test-local/${SERVICE_RELEASE_NAME}-0.0.0.tgz \
                           --install \
                           --values ${SERVICE_VALUES_FILE} \
                           --namespace ${NAMESPACE} \
                           --wait \
                           --timeout ${TIMEOUT_UPGRADE} \
                           ${SERVICE_SECRET}"
  echo "helm" ${INSTALL_COMMAND}
  helm ${INSTALL_COMMAND}
  checkExitCode "Failed to upgrade ${SERVICE_NAME}"
}

function waitForDeploymentReady() {
  log "Waiting for deployment to be ready"
  kubectl rollout status deployment/${SERVICE_NAME} -n ${NAMESPACE} --timeout ${TIMEOUT_DEPLOYMENT_READY}
}

function extractLogs() {
  log "Extracting logs"
  kubectl logs ${SERVICE_INTEGRATION_NAME} --namespace ${NAMESPACE} > ${HELM_TEST_LOCAL}/helmTestResults.log
  log "Finished extracting logs"
}

function runHelmTest() {
  log "Running ${SERVICE_INTEGRATION_RELEASE_NAME} helm test"
  TEST_COMMAND="test ${SERVICE_INTEGRATION_RELEASE_NAME} -n ${NAMESPACE} --timeout ${TIMEOUT_HELM_TESTS}"
  echo "helm" ${TEST_COMMAND}
  helm ${TEST_COMMAND}
  extractLogs
}

function createSecret() {
  log "Creating secret"
  SERVICE_SECRET="--set imageCredentials.pullSecret=${SECRET_NAME} \
                  --set global.pullSecret=${SECRET_NAME}"
  kubectl create secret docker-registry armdocker \
    --docker-username="${SELI_ARTIFACTORY_REPO_USER}" \
    --docker-password="${SELI_ARTIFACTORY_REPO_PASS}" \
    --docker-server=https://armdocker.rnd.ericsson.se/ \
    --namespace="${NAMESPACE}"
}

function checkEnvValues() {
  log "Checking if all needed environment values are set"

  if [[ ! -v SELI_ARTIFACTORY_REPO_USER ]]; then
    echo -e "\n${RED} --- ERROR: SELI_ARTIFACTORY_REPO_USER is not set in your system! Please set it before you try to run IT --- ${NO_COLOR}"
    echo -e "${RED} --- ERROR: https://arm.seli.gic.ericsson.se/ui/admin/artifactory/user_profile --- ${NO_COLOR}\n"
    exit 255
  fi

  if [[ ! -v SELI_ARTIFACTORY_REPO_PASS ]]; then
    echo -e "\n${RED} --- ERROR: SELI_ARTIFACTORY_REPO_PASS is not set in your system! Please set it before you try to run IT --- ${NO_COLOR}"
    echo -e "${RED} --- ERROR: https://arm.seli.gic.ericsson.se/ui/admin/artifactory/user_profile --- ${NO_COLOR}\n"
    exit 255
  fi
}

function cleanUpPackages() {
  log "Cleaning up packaged charts"
  rm -v -f ${PROJECT_ROOT}/helm-test-local/eric-oss-pm-stats-calculator-*.tgz
}

function runGenericHelmTestScript() {
  getUserOptions
  ensureOnCorrectServer
  applyCorrectValues
  checkEnvValues
  addRepos
  initRepo
  cleanUpHelmEnvironment
  buildDockerImages

  if [[ "$remote" -eq 0 ]]; then
    log "Installing ${SERVICE_NAME}"
    kubectl create ns ${NAMESPACE}
  fi

  createSecret
  installIntegrationService
  installService
  waitForDeploymentReady

  runHelmTest
}

#########
# main
#########
runGenericHelmTestScript

if [[ $cleanAfter -eq 1 ]]; then
  cleanUpPackages
  cleanUpHelmEnvironment
fi

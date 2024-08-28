#!/usr/bin/env groovy

def defaultBobImage = 'armdocker.rnd.ericsson.se/proj-adp-cicd-drop/bob.2.0:1.7.0-55'
def bob = new BobCommand()
    .bobImage(defaultBobImage)
    .envVars([
        HOME:'${HOME}',
        ISO_VERSION:'${ISO_VERSION}',
        RELEASE:'${RELEASE}',
        SONAR_HOST_URL:'${SONAR_HOST_URL}',
        SONAR_AUTH_TOKEN:'${SONAR_AUTH_TOKEN}',
        GERRIT_CHANGE_NUMBER:'${GERRIT_CHANGE_NUMBER}',
        GERRIT_CHANGE_URL:'${GERRIT_CHANGE_URL}',
        KUBECONFIG:'${KUBECONFIG}',
        K8S_NAMESPACE:'${K8S_NAMESPACE}',
        USER:'${USER}',
        SELI_ARTIFACTORY_REPO_USER:'${CREDENTIALS_SELI_ARTIFACTORY_USR}',
        SELI_ARTIFACTORY_REPO_PASS:'${CREDENTIALS_SELI_ARTIFACTORY_PSW}',
        SERO_ARTIFACTORY_REPO_USER:'${CREDENTIALS_SERO_ARTIFACTORY_USR}',
        SERO_ARTIFACTORY_REPO_PASS:'${CREDENTIALS_SERO_ARTIFACTORY_PSW}',
        FOSSA_API_KEY:'${CREDENTIALS_FOSSA_API_KEY}',
        SCAS_TOKEN:'${CREDENTIALS_SCAS_TOKEN}',
        BAZAAR_TOKEN:'${CREDENTIALS_BAZAAR}',
        XRAY_USER:'${CREDENTIALS_XRAY_SELI_ARTIFACTORY_USR}',
        XRAY_APIKEY:'${CREDENTIALS_XRAY_SELI_ARTIFACTORY_PSW}',
        VHUB_API_TOKEN:'${VHUB_API_TOKEN}',
        MAVEN_CLI_OPTS:'${MAVEN_CLI_OPTS}',
        OPEN_API_SPEC_DIRECTORY:'${OPEN_API_SPEC_DIRECTORY}',
        ERIDOC_USERNAME:'${ERIDOC_USERNAME}',
        ERIDOC_PASSWORD:'${ERIDOC_PASSWORD}',
        ADP_PORTAL_API_KEY:'${ADP_PORTAL_API_KEY}',
        GERRIT_USERNAME:'${GERRIT_USERNAME}',
        GERRIT_PASSWORD:'${GERRIT_PASSWORD}'
    ])
    .needDockerSocket(true)
    .toString()

def LOCKABLE_RESOURCE_LABEL = "kaas"

def validateSdk = 'false'
def Boolean FOSS_CHANGED = true
@Library('oss-common-pipeline-lib@dVersion-2.0.0-hybrid') _ // Shared library from the OSS/com.ericsson.oss.ci/oss-common-ci-utils

pipeline {
    agent {
        node {
            label NODE_LABEL
        }
    }

    options {
        timestamps()
        timeout(time: 90, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '50', artifactNumToKeepStr: '50'))
    }

    environment {
        RELEASE = "true"
        TEAM_NAME = "Velociraptors"
        KUBECONFIG = "${WORKSPACE}/.kube/config"
        CREDENTIALS_SELI_ARTIFACTORY = credentials('SELI_ARTIFACTORY')
        CREDENTIALS_SERO_ARTIFACTORY = credentials('SERO_ARTIFACTORY')
        MAVEN_CLI_OPTS = "-Duser.home=${env.HOME} -B -s ${env.WORKSPACE}/settings.xml"
        OPEN_API_SPEC_DIRECTORY = "src/main/resources/v1"
        // FOSSA
        CREDENTIALS_BAZAAR = credentials('BAZAAR_token')
        CREDENTIALS_FOSSA_API_KEY = credentials('FOSSA_API_token')
        CREDENTIALS_SCAS_TOKEN = credentials('SCAS_token')
        FOSSA_ENABLED = "true"
        // Vulnerability Analysis
        VHUB_API_TOKEN = credentials('vhub-api-key-id')
        CREDENTIALS_XRAY_SELI_ARTIFACTORY = credentials('XRAY_SELI_ARTIFACTORY')
        ANCHORE_ENABLED = "false"
        HADOLINT_ENABLED = "true"
        KUBEAUDIT_ENABLED = "false"
        KUBEHUNTER_ENABLED = "false"
        KUBESEC_ENABLED = "false"
        TRIVY_ENABLED = "false"
        XRAY_ENABLED = "false"
    }

    // Stage names (with descriptions) taken from ADP Microservice CI Pipeline Step Naming Guideline: https://confluence.lmera.ericsson.se/pages/viewpage.action?pageId=122564754
    stages {
        stage('Clean') {
            steps {
                echo 'Inject settings.xml into workspace:'
                configFileProvider([configFile(fileId: "${env.SETTINGS_CONFIG_FILE_NAME}", targetLocation: "${env.WORKSPACE}")]) {}
                archiveArtifacts allowEmptyArchive: true, artifacts: 'ruleset2.0.yaml, publish.Jenkinsfile'
                sh "${bob} clean"
            }
        }

        stage('Init') {
            steps {
                sh "${bob} init-drop"
                archiveArtifacts 'artifact.properties'
                script {
                    authorName = sh(returnStdout: true, script: 'git show -s --pretty=%an')
                    currentBuild.displayName = currentBuild.displayName + ' / ' + authorName
                }
            }
        }

        stage('Lint') {
            steps {
                parallel(
                    "lint markdown": {
                        sh "${bob} lint:markdownlint lint:vale"
                    },
                    "lint helm": {
                        sh "${bob} lint:helm"
                    },
                    "lint helm design rule checker": {
                        sh "${bob} lint:helm-chart-check"
                    },
                    "lint code": {
                        sh "${bob} lint:license-check"
                    },
                    "lint OpenAPI spec": {
                        sh "${bob} lint:oas-bth-linter"
                    },
                    "lint metrics": {
                        sh "${bob} lint:metrics-check"
                    },
                    "SDK Validation": {
                        script {
                            if (validateSdk == "true") {
                                sh "${bob} validate-sdk"
                            }
                        }
                    }
                )
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: '**/*bth-linter-output.html, **/design-rule-check-report.*'
                }
            }
        }

        stage('Generate') {
            steps {
                parallel(
                    "Open API Spec": {
                        sh "${bob} rest-2-html:check-has-open-api-been-modified"
                        script {
                            def val = readFile '.bob/var.has-openapi-spec-been-modified'
                            if (val.trim().equals("true")) {
                                sh "${bob} rest-2-html:zip-open-api-doc"
                                sh "${bob} rest-2-html:generate-html-output-files"

                                manager.addInfoBadge("OpenAPI spec has changed. HTML Output files will be published to the CPI library.")
                                archiveArtifacts artifacts: "${OPEN_API_SPEC_DIRECTORY}/rest_conversion_log.txt"
                            }
                        }
                    }
                )
            }
        }

        stage('FOSSA Stream') {
            stages {
                stage('Maven Dependency Tree Check') {
                    when {
                        expression {
                            env.FOSSA_ENABLED == "true"
                        }
                    }
                    steps {
                        script {
                            withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                                sh "${bob} generate-mvn-dep-tree"
                            }
                            if (ci_pipeline_scripts.compareDepTreeFiles("${WORKSPACE}/fossa/local_dep_tree.txt", "${WORKSPACE}/build/dep_tree.txt")) {
                                FOSS_CHANGED = false
                            }
                            echo "FOSS Changed: $FOSS_CHANGED"
                        }
                        archiveArtifacts allowEmptyArchive: true, artifacts: 'build/dep_tree.txt'
                    }
                }

                stage('3PP Analysis') {
                    when {
                        expression { env.FOSSA_ENABLED == "true" && FOSS_CHANGED }
                    }
                    steps {
                        withCredentials([string(credentialsId: 'FOSSA_API_token', variable: 'FOSSA_API_KEY'), string(credentialsId: 'SCAS_token', variable: 'SCAS_TOKEN'), string(credentialsId: 'BAZAAR_token', variable: 'BAZAAR_TOKEN'), string(credentialsId: 'munin_token', variable: 'MUNIN_TOKEN')]) {
                            sh "${bob} 3pp-analysis"
                        }
                    }
                }

                stage('Dependencies Validate') {
                    when {
                        expression { env.FOSSA_ENABLED == "true" && FOSS_CHANGED }
                    }
                    steps {
                        withCredentials([string(credentialsId: 'FOSSA_API_token', variable: 'FOSSA_API_KEY'), string(credentialsId: 'SCAS_token', variable: 'SCAS_TOKEN'), string(credentialsId: 'BAZAAR_token', variable: 'BAZAAR_TOKEN'), string(credentialsId: 'munin_token', variable: 'MUNIN_TOKEN')]) {
                            sh "${bob} dependencies-validate || true"
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                sh "${bob} build"
            }
        }

        stage('Test') {
            steps {
                sh "${bob} test"
            }
        }

        stage('SonarQube') {
            when {
                expression { env.SQ_ENABLED == "true" }
            }
            steps {
                withSonarQubeEnv("${env.SQ_SERVER}") {
                    sh "${bob} sonar-enterprise-release"
                }
            }
        }

        stage('Image') {
            steps {
                sh "${bob} image"
                script {
                    try {
                        sh "${bob} image-dr-check"
                    } catch (final Exception ignored) { // TODO: Fix image related issues, and then remove try {} catch () {}
                        echo 'Fix image related issues...'
                    }
                }
            }
            post {
                always {
                    archiveArtifacts([
                            allowEmptyArchive: true,
                            artifacts: '**/image-design-rule-check-report*'
                    ])
                }
            }
        }

        stage('Package') {
            steps {
                sh "${bob} package"
                sh "${bob} package-jars"
            }
        }

        stage('K8S Resource Lock') {
            options {
                lock(label: LOCKABLE_RESOURCE_LABEL, variable: 'RESOURCE_NAME', quantity: 1)
            }
            environment {
                K8S_TEST = 'true'
                K8S_CLUSTER_ID = sh(script: "echo \${RESOURCE_NAME} | cut -d'_' -f1", returnStdout: true).trim()
                K8S_NAMESPACE = sh(script: "echo \${RESOURCE_NAME} | cut -d',' -f1 | cut -d'_' -f2", returnStdout: true).trim()
            }
            stages {
                stage('Helm Install') {
                    when {
                        expression { env.K8S_TEST == 'true' }
                    }
                    steps {
                        echo "Inject kubernetes config file (${env.K8S_CLUSTER_ID}) based on the Lockable Resource name: ${env.RESOURCE_NAME}"
                        configFileProvider([
                                configFile(fileId: "${env.K8S_CLUSTER_ID}", targetLocation: "${env.KUBECONFIG}")
                        ]) {}
                        echo "The namespace (${env.K8S_NAMESPACE}) is reserved and locked based on the Lockable Resource name: ${env.RESOURCE_NAME}"

                        sh "${bob} helm-dry-run"
                        sh "${bob} create-namespace"

                        sh "${bob} helm-install-integration"

                        script {
                            echo "HELM_UPGRADE is ${env.HELM_UPGRADE}"
                            if (env.HELM_UPGRADE == 'true') {
                                sh "${bob} helm-upgrade"
                            } else {
                                sh "${bob} helm-install-service"
                            }
                        }

                        echo 'sh ${bob} healthcheck is SKIPPED'
                        //  sh "${bob} healthcheck"
                    }
                    post {
                        always {
                            sh "${bob} kaas-info || true"
                            archiveArtifacts([
                                    allowEmptyArchive: true,
                                    artifacts: 'build/kaas-info.log'
                            ])
                        }
                        unsuccessful {
                            sh "${bob} collect-k8s-logs || true"
                            archiveArtifacts([
                                    allowEmptyArchive: true,
                                    artifacts: 'k8s-logs/*'
                            ])
                            sh "${bob} delete-namespace"
                        }
                    }
                }

                stage('K8S Test') {
                    when {
                        expression { env.K8S_TEST == 'true' }
                    }
                    steps {
                        sh "${bob} helm-test"
                    }
                    post {
                        always {
                            sh "${bob} collect-k8s-logs || true"
                            archiveArtifacts([
                                    allowEmptyArchive: true,
                                    artifacts: 'k8s-logs/*'
                            ])
                        }
                        unsuccessful {
                            sh "${bob} delete-namespace"
                        }
                    }
                }

                stage("Vulnerability Analysis") {
                    steps {
                        parallel(
                            "Hadolint": {
                                script {
                                    if (env.HADOLINT_ENABLED == "true") {
                                        sh "${bob} hadolint-scan"
                                        echo "Evaluating Hadolint Scan Resultcodes..."
                                        sh "${bob} evaluate-design-rule-check-resultcodes"
                                        archiveArtifacts "build/va-reports/hadolint-scan/**.*"
                                    } else {
                                        echo "stage Hadolint skipped"
                                    }
                                }
                            },
                            "Kubehunter": {
                                script {
                                    if (env.K8S_TEST == 'true' && env.KUBEHUNTER_ENABLED == "true") {
                                        sh "${bob} kubehunter-scan"
                                        archiveArtifacts "build/va-reports/kubehunter-report/**/*"
                                    } else {
                                        echo "stage Kubehunter skipped"
                                    }
                                }
                            },
                            "Kubeaudit": {
                                script {
                                    if (env.KUBEAUDIT_ENABLED == "true") {
                                        sh "${bob} kube-audit"
                                        archiveArtifacts "build/va-reports/kube-audit-report/**/*"
                                    } else {
                                        echo "stage Kubeaudit skipped"
                                    }
                                }
                            },
                            "Kubsec": {
                                script {
                                    if (env.KUBESEC_ENABLED == "true") {
                                        sh "${bob} kubesec-scan"
                                        archiveArtifacts "build/va-reports/kubesec-reports/*"
                                    } else {
                                        echo "stage Kubsec skipped"
                                    }
                                }
                            },
                            "Trivy": {
                                script {
                                    if (env.TRIVY_ENABLED == "true") {
                                        sh "${bob} trivy-inline-scan"
                                        archiveArtifacts "build/va-reports/trivy-reports/**.*"
                                        archiveArtifacts "trivy_metadata.properties"
                                    } else {
                                        echo "stage Trivy skipped"
                                    }
                                }
                            },
                            "X-Ray": {
                                script {
                                    if (env.XRAY_ENABLED == "true") {
                                        sleep(60)
                                        sh "${bob} fetch-xray-report"
                                        archiveArtifacts "build/va-reports/xray-reports/xray_report.json"
                                        archiveArtifacts "build/va-reports/xray-reports/raw_xray_report.json"
                                    } else {
                                        echo "stage X-Ray skipped"
                                    }
                                }
                            },
                            "Anchore-Grype": {
                                script {
                                    if (env.ANCHORE_ENABLED == "true") {
                                        sh "${bob} anchore-grype-scan"
                                        archiveArtifacts "build/va-reports/anchore-reports/**.*"
                                    } else {
                                        echo "stage Anchore-Grype skipped"
                                    }
                                }
                            }
                        )
                    }
                    post {
                        unsuccessful {
                            sh "${bob} collect-k8s-logs || true"
                            archiveArtifacts allowEmptyArchive: true, artifacts: 'k8s-logs/**/*.*'
                        }
                        cleanup {
                            script {
                                if (env.K8S_TEST == 'true') {
                                    sh "${bob} delete-namespace"
                                }
                            }
                            sh "${bob} delete-images:cleanup-anchore-trivy-images"
                            sh "rm -f ${env.KUBECONFIG}"
                        }
                    }
                }
            }
        }
        stage('Generate Vulnerability report V2.0') {
            steps {
                sh "${bob} generate-VA-report-V2:no-upload"
                archiveArtifacts allowEmptyArchive: true, artifacts: 'build/va-reports/Vulnerability_Report_2.0.md'
            }
        }
        stage('Upload Marketplace Documentation') {
            when {
                anyOf {
                    changeset "doc/**/*.md"
                    expression { readFile('.bob/var.has-openapi-spec-been-modified').trim().equals("true") }
                }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS'),
                                 string(credentialsId: 'PMSC_ADP_PORTAL_API_KEY', variable: 'ADP_PORTAL_API_KEY')]) {
                    // upload release version
                    sh "${bob} generate-docs"
                    script {
                        echo "Marketplace upload"
                        sh "${bob} marketplace-upload-release"
                    }
                }
            }
        }
        stage('Upload EriDoc Documentation') {
            when {
                changeset "doc/**/*.md"
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'eridoc-user', usernameVariable: 'ERIDOC_USERNAME', passwordVariable: 'ERIDOC_PASSWORD')]) {
                    sh "${bob} generate-docs:markdown-to-pdf"
                    script {
                        echo "Upload EriDoc Documentation"
                        sh "${bob} eridoc-upload-documents:eridoc-upload"
                        sh "${bob} eridoc-upload-documents:eridoc-approve"
                    }
                }
            }
        }
        stage('Publish') {
            steps {
                sh "${bob} publish"
            }
        }
    }
    post {
        success {
            withCredentials([usernamePassword(credentialsId: "GERRIT_PASSWORD", usernameVariable: "GERRIT_USERNAME", passwordVariable: "GERRIT_PASSWORD")])
            {
                sh "${bob} create-git-tag"
                sh "git pull origin HEAD:master"
                bumpVersion("publish", "fossa/local_dep_tree.txt")
            }
            script {
                sh "${bob} helm-chart-check-report-warnings"
                sendHelmDRWarningEmail()
                modifyBuildDescription()
            }
        }
        cleanup {
            sh "${bob} delete-images"
            sh "${bob} move-reports"
        }
    }
}

def modifyBuildDescription() {

    def CHART_NAME = "eric-oss-pm-stats-calculator"
    def DOCKER_IMAGE_NAME = "eric-oss-pm-stats-calculator"

    def VERSION = readFile('.bob/var.version').trim()

    def CHART_DOWNLOAD_LINK = "https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-released-helm-local/${CHART_NAME}/${CHART_NAME}-${VERSION}.tgz"
    def DOCKER_IMAGE_DOWNLOAD_LINK = "https://armdocker.rnd.ericsson.se/artifactory/docker-v2-global-local/proj-eric-oss-drop/${CHART_NAME}/${VERSION}/"

    currentBuild.description = "Helm Chart: <a href=${CHART_DOWNLOAD_LINK}>${CHART_NAME}-${VERSION}.tgz</a><br>Docker Image: <a href=${DOCKER_IMAGE_DOWNLOAD_LINK}>${DOCKER_IMAGE_NAME}-${VERSION}</a><br>Gerrit: <a href=${env.GERRIT_CHANGE_URL}>${env.GERRIT_CHANGE_URL}</a> <br>"
}

def sendHelmDRWarningEmail() {
    def val = readFile '.bob/var.helm-chart-check-report-warnings'
    if (val.trim().equals("true")) {
        echo "WARNING: One or more Helm Design Rules have a WARNING state. Review the Archived Helm Design Rule Check Report: design-rule-check-report.html"
        manager.addWarningBadge("One or more Helm Design Rules have a WARNING state. Review the Archived Helm Design Rule Check Report: design-rule-check-report.html")
        echo "Sending an email to Helm Design Rule Check distribution list: ${env.HELM_DR_CHECK_DISTRIBUTION_LIST}"
        try {
            mail to: "${env.HELM_DR_CHECK_DISTRIBUTION_LIST}",
            from: "${env.GERRIT_PATCHSET_UPLOADER_EMAIL}",
            cc: "${env.GERRIT_PATCHSET_UPLOADER_EMAIL}",
            subject: "[${env.JOB_NAME}] One or more Helm Design Rules have a WARNING state. Review the Archived Helm Design Rule Check Report: design-rule-check-report.html",
            body: "One or more Helm Design Rules have a WARNING state. <br><br>" +
            "Please review Gerrit and the Helm Design Rule Check Report: design-rule-check-report.html: <br><br>" +
            "&nbsp;&nbsp;<b>Gerrit master branch:</b> https://gerrit-gamma.gic.ericsson.se/gitweb?p=${env.GERRIT_PROJECT}.git;a=shortlog;h=refs/heads/master <br>" +
            "&nbsp;&nbsp;<b>Helm Design Rule Check Report:</b> ${env.BUILD_URL}artifact/.bob/design-rule-check-report.html <br><br>" +
            "For more information on the Design Rules and ADP handling process please see: <br>" +
            "&nbsp;&nbsp; - <a href='https://confluence.lmera.ericsson.se/display/AA/Helm+Chart+Design+Rules+and+Guidelines'>Helm Design Rule Guide</a><br>" +
            "&nbsp;&nbsp; - <a href='https://confluence.lmera.ericsson.se/display/ACD/Design+Rule+Checker+-+How+DRs+are+checked'>More Details on Design Rule Checker</a><br>" +
            "&nbsp;&nbsp; - <a href='https://confluence.lmera.ericsson.se/display/AA/General+Helm+Chart+Structure'>General Helm Chart Structure</a><br><br>" +
            "<b>Note:</b> This mail was automatically sent as part of the following Jenkins job: ${env.BUILD_URL}",
            mimeType: 'text/html'
        } catch(Exception e) {
            echo "Email notification was not sent."
            print e
        }
    }
}

/*  increase pom & prefix version - minor number
    e.g.  1.0.0 -> 1.1.0

    this one will replace the old one, with maven dep tree updated.
*/
def bumpVersion(String job_type, String mvnDepTreePath) {
    if (job_type=="publish") {
        def properties_file = "common-properties.yaml"
        def generatedDependencies = ""
        def configDependencies = ""
        if (fileExists("${workspace}/${properties_file}")) {
            def properties = readFile("${properties_file}").trim()
            for (data in properties.split("\n")){
                if (data.contains("ci-artifacts-directory")){
                    generatedDependencies = data.split(":")[1].trim()
                    println generatedDependencies
                }
                if (data.contains("dependencies-files-folder")){
                    configDependencies = data.split(":")[1].trim()
                    println configDependencies
                }
            }
        }
        env.oldPomVersion = readFile ".bob/var.pom-version"
        env.POM_VERSION_OLD = env.oldPomVersion.trim()
        // increase patch number to version_prefix
        sh 'git stash'
        sh 'docker run --rm -v $PWD/VERSION_PREFIX:/app/VERSION -w /app --user $(id -u):$(id -g) armdocker.rnd.ericsson.se/proj-eric-oss-drop/utilities/bump minor'
        env.versionPrefix = readFile "VERSION_PREFIX"
        env.newPatchVersionPrefix = env.versionPrefix.trim() + "-SNAPSHOT"
        env.VERSION_PREFIX_UPDATED = env.newPatchVersionPrefix.trim()
        echo "pom version has been bumped from ${POM_VERSION_OLD} to ${VERSION_PREFIX_UPDATED}"
        sh """
            find ./ -name 'pom.xml' -type f -exec sed -i -e "0,/${POM_VERSION_OLD}/s//${VERSION_PREFIX_UPDATED}/" {} \\;
            git add VERSION_PREFIX
            find ./ -name 'pom.xml' -type f -exec git add pom.xml {} \\;
            if [ -f ${WORKSPACE}/build/dep_tree.txt ]; then
                mv ${WORKSPACE}/build/dep_tree.txt $mvnDepTreePath
                sed -i 's/${POM_VERSION_OLD}/${VERSION_PREFIX_UPDATED}/g' $mvnDepTreePath
                git add $mvnDepTreePath
            fi
            if [ -f ${WORKSPACE}/${generatedDependencies}/dependencies-3pp.yaml ]; then
                mv ${WORKSPACE}/${generatedDependencies}/dependencies-3pp.yaml ${configDependencies}/dependencies-3pp.yaml
                git add ${configDependencies}/dependencies-3pp.yaml
            fi
            git commit -m "Automatically updating VERSION_PREFIX to ${versionPrefix}"
            git push origin HEAD:master
        """
    }
}

// More about @Builder: http://mrhaki.blogspot.com/2014/05/groovy-goodness-use-builder-ast.html
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class BobCommand {
    def bobImage = 'bob.2.0:latest'
    def envVars = [:]
    def needDockerSocket = false

    String toString() {
        def env = envVars
                .collect({ entry -> "-e ${entry.key}=\"${entry.value}\"" })
                .join(' ')

        def cmd = """\
            |docker run
            |--init
            |--rm
            |--workdir \${PWD}
            |--user \$(id -u):\$(id -g)
            |-v \${PWD}:\${PWD}
            |-v /etc/group:/etc/group:ro
            |-v /etc/passwd:/etc/passwd:ro
            |-v /proj/mvn/:/proj/mvn
            |-v \${HOME}:\${HOME}
            |${needDockerSocket ? '-v /var/run/docker.sock:/var/run/docker.sock' : ''}
            |${env}
            |\$(for group in \$(id -G); do printf ' --group-add %s' "\$group"; done)
            |--group-add \$(stat -c '%g' /var/run/docker.sock)
            |${bobImage}
            |"""
        return cmd
                .stripMargin()           // remove indentation
                .replace('\n', ' ')      // join lines
                .replaceAll(/[ ]+/, ' ') // replace multiple spaces by one
    }
}

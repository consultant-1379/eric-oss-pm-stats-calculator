#
# COPYRIGHT Ericsson 2024
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#

ARG CBOS_VERSION=6.14.0-10

FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:${CBOS_VERSION}

ARG CBOS_VERSION
ARG CBO_REPO_URL=https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/${CBOS_VERSION}

#Install openJDK 17
ENV JAVA_HOME=/usr/

RUN zypper addrepo -C -G -f ${CBO_REPO_URL}?ssl_verify=no COMMON_BASE_OS_SLES_REPO \
    && zypper install -l -y java-17-openjdk-headless curl \
    && zypper clean --all \
    && zypper rr COMMON_BASE_OS_SLES_REPO

COPY /uber_jar /test-runner/
COPY /kafka_files /kafka_files/

ENTRYPOINT [ "java","-jar","/test-runner/testsuite-uber-jar.jar" ]

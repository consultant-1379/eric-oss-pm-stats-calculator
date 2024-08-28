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

ENV SPARK_HOME=/usr/local/spark

# Set the Java env vars
ENV JAVA_HOME=/usr/ \
    JAVA_OPTS="-Djava.net.preferIPv4Stack=true"

ENV KPI_USER=174023

ARG CBOS_VERSION
ARG SPARK_VER=3.3.3
ARG HADOOP_VER=3
ARG SPARK_DIR=spark-${SPARK_VER}-bin-hadoop${HADOOP_VER}
ARG SPARK_GROUPID=org.apache.spark
ARG SPARK_LAUNCHER_ARTIFACTID=spark-launcher_2.12
ARG SPARK_LAUNCHER_VERSION=3.3.3
ARG PG_VERSION=13

ARG CBO_REPO_URL=https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/${CBOS_VERSION}
ARG SPARK_URL=https://arm1s11-eiffel112.eiffel.gic.ericsson.se:8443/nexus/content/groups/public/org/apache/spark

RUN zypper addrepo -C -G -f ${CBO_REPO_URL}?ssl_verify=no \
    COMMON_BASE_OS_SLES_REPO \
    && zypper install -l -y java-17-openjdk-headless curl postgresql${PG_VERSION}\
    && zypper clean --all \
    && zypper rr COMMON_BASE_OS_SLES_REPO


RUN curl  -O --insecure ${SPARK_URL}/${SPARK_DIR}/${SPARK_VER}/${SPARK_DIR}-${SPARK_VER}.gz \
    && tar xf ${SPARK_DIR}-${SPARK_VER}.gz \
    && rm ${SPARK_DIR}-${SPARK_VER}.gz \
    && mkdir -p /usr/local/spark/logs \
    && chown -R $KPI_USER:0 /usr/local/spark/logs \
    && chmod -R g=u /usr/local/spark/logs \
    && cp -r spark-${SPARK_VER}-bin-hadoop${HADOOP_VER}/* /usr/local/spark \
    && rm -rf spark-${SPARK_VER}-bin-hadoop${HADOOP_VER} \
    && zypper addrepo -C -G -f ${CBO_REPO_URL}?ssl_verify=no COMMON_BASE_OS_SLES_REPO \
    && zypper clean --all \
    && zypper rr COMMON_BASE_OS_SLES_REPO

COPY ./Docker/spark/metrics.properties $SPARK_HOME/conf/
COPY ./Docker/spark/logback.xml $SPARK_HOME/conf/

RUN mkdir -p /tmp/spark-events \
    && chown -R $KPI_USER:0 /tmp/spark-events \
    && chmod -R g=u /tmp/spark-events

RUN echo "$KPI_USER:x:$KPI_USER:$KPI_USER:An Identity for eric-oss-pm-stats-calculator:/kpiuser:/bin/false" >> /etc/passwd \
    && echo "$KPI_USER:!::0:::::" >> /etc/shadow

ADD eric-oss-pm-stats-calculator-spring-boot/target/eric-oss-pm-stats-calculator-spring-boot-*-SNAPSHOT-spring-boot.jar eric-oss-pm-stats-calculator.jar
COPY src/main/resources/jmx/* /jmx/
RUN chmod 600 /jmx/jmxremote.password
RUN chown $KPI_USER /jmx/jmxremote.password

USER $KPI_USER

COPY ./eric-oss-pm-stats-calculator-spark/target/dependency/*.jar /kpiuser/ericsson/eric-oss-pm-stats-calculator/kpi-spark/

CMD java ${JAVA_OPTS} -Dcom.sun.management.jmxremote=true \
-Dcom.sun.management.jmxremote.port=10002 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.rmi.port=10002 \
-jar eric-oss-pm-stats-calculator.jar

ARG BUILD_DATE
ARG COMMIT
ARG APP_VERSION
ARG RSTATE
ARG IMAGE_PRODUCT_NUMBER

LABEL \
    org.opencontainers.image.title=eric-oss-pm-stats-calculator-jsb \
    org.opencontainers.image.created=$BUILD_DATE \
    org.opencontainers.image.revision=$COMMIT \
    org.opencontainers.image.vendor=Ericsson \
    org.opencontainers.image.version=$APP_VERSION \
    com.ericsson.product-revision="${RSTATE}" \
    com.ericsson.product-number="$IMAGE_PRODUCT_NUMBER"
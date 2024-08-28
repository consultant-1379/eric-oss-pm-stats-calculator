#!/bin/sh
#
# COPYRIGHT Ericsson 2022
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#

METRICS_EXPOSURE_TUTORIAL_URL="https://confluence-oss.seli.wh.rnd.internal.ericsson.com/pages/viewpage.action?spaceKey=ESO&title=How+to+add+metrics+to+a+microservice";

checkValuesYAML(){
    SERVICE_NAME=$1
    echo -e "prometheus:\n  path: /kpi-handling/actuator/prometheus\n  scrape: true\n  port: 8080" > .bob/var.compareToLine;

    grep -A3 "^prometheus:" ./charts/$SERVICE_NAME/values.yaml > .bob/var.scrapedLine || true;

    if cmp -s .bob/var.compareToLine .bob/var.scrapedLine; then
        echo "SUCCESS: values.yaml containes all the lines necessary for metrcis exposure.";
        echo "true" >> .bob/var.metrics-exposed;
    else
        echo -e "FAILURE: This stage has failed as the lines needed for metric exposure are not correctly implemented inside values.yaml.\nPlease refer to the page provided:\n$METRICS_EXPOSURE_TUTORIAL_URL";
        echo -e "What is needed:"
        cat .bob/var.compareToLine
        echo -e "\nWhat was provided:"
        cat .bob/var.scrapedLine
        echo "false" >> .bob/var.metrics-exposed;
    fi

    if cmp -s .bob/var.compareToLine .bob/var.scrapedLine; then
        echo "SUCCESS: values.yaml containes all the lines necessary for metrcis exposure.";
        echo "true" >> .bob/var.metrics-exposed;
    else
        echo -e "FAILURE: This stage has failed as the lines needed for metric exposure are not correctly implemented inside values.yaml.\nPlease refer to the page provided:\n$METRICS_EXPOSURE_TUTORIAL_URL";
        echo -e "What is needed:"
        cat .bob/var.compareToLine
        echo -e "\nWhat was provided:"
        cat .bob/var.scrapedLine
        echo "false" >> .bob/var.metrics-exposed;
    fi
}

checkServiceYAML(){
    SERVICE_NAME=$1
    if grep -q "{{- include \"$SERVICE_NAME.annotations\" . | nindent [0-9] }}" ./charts/$SERVICE_NAME/templates/service.yaml &&
       grep -q "\"$SERVICE_NAME.prometheus\"" ./charts/$SERVICE_NAME/templates/_helpers.tpl; then
        echo "SUCCESS: service.yaml containes all the lines necessary for metrics exposure.";
        echo "true" >> .bob/var.metrics-exposed;
    else
        echo -e "FAILURE: This stage has failed as the lines needed for metric exposure are not correctly implemented inside service.yaml.\nPlease refer to the page provided:\n$METRICS_EXPOSURE_TUTORIAL_URL";
        echo -e "What is needed:"
        echo -e "{{- include \"$SERVICE_NAME.annotations\" . | nindent [0-9] }} in service.yaml"
        echo -e "Where [0-9] is to be replaced by the indent number. Line has to be provided under 'annotations'."
        echo -e "and \"$SERVICE_NAME.prometheus\" in _helpers.tpl"
        echo "false" >> .bob/var.metrics-exposed;
    fi
}

checkDeploymentYAML(){
    SERVICE_NAME=$1
    if grep -q "{{- include \"$SERVICE_NAME.annotations\" . | nindent [0-9] }}" ./charts/$SERVICE_NAME/templates/deployment.yaml &&
       grep -q "\"$SERVICE_NAME.prometheus\"" ./charts/$SERVICE_NAME/templates/_helpers.tpl; then
        echo "SUCCESS: deployment.yaml containes all the lines necessary for metrics exposure.";
        echo "true" >> .bob/var.metrics-exposed;
    else
        echo -e "FAILURE: This stage has failed as the lines needed for metric exposure are not correctly implemented inside deployment.yaml.\nPlease refer to the page provided:\n$METRICS_EXPOSURE_TUTORIAL_URL";
        echo -e "What is needed:"
        echo -e "{{- include \"$SERVICE_NAME.annotations\" . | nindent [0-9] }} in deployment.yaml"
        echo -e "Where [0-9] is to be replaced by the indent number."
        echo -e "and \"$SERVICE_NAME.prometheus\" in _helpers.tpl"
        echo "false" >> .bob/var.metrics-exposed;
    fi
}

checkHelperTPL(){
    SERVICE_NAME=$1
    echo -e "{{/*\nCreate prometheus info\n*/}}" > .bob/var.compareToLine;
    echo -e "{{- define \"$SERVICE_NAME.prometheus\" -}}" >> .bob/var.compareToLine;
    echo -e "prometheus.io/path: {{ .Values.prometheus.path | quote }}" >> .bob/var.compareToLine;
    echo -e "{{/*Port commented out so prometheus can identify all exposed ports for metrics til JMX exporter can be removed*/}}" >> .bob/var.compareToLine;
    echo -e "{{/*prometheus.io/port: {{ .Values.prometheus.port | quote }}*/}}" >> .bob/var.compareToLine;
    echo -e "prometheus.io/scrape: {{ .Values.prometheus.scrape | quote }}" >> .bob/var.compareToLine;
    echo -e "{{- end -}}" >> .bob/var.compareToLine;

    grep -B4 -A4 "^prometheus.io/path:" ./charts/$SERVICE_NAME/templates/_helpers.tpl > .bob/var.scrapedLine || true;

    if cmp -s .bob/var.compareToLine .bob/var.scrapedLine; then
        echo "SUCCESS: helper.yaml containes all the lines necessary for metrics exposure.";
        echo "true" >> .bob/var.metrics-exposed;
    else
        echo -e "FAILURE: This stage has failed as the lines needed for metric exposure are not correctly implemented inside helper.tpl.\nPlease refer to the page provided:\n$METRICS_EXPOSURE_TUTORIAL_URL";
        echo -e "What is needed:"
        cat .bob/var.compareToLine
        echo -e "\nWhat was provided:"
        cat .bob/var.scrapedLine
        echo "false" >> .bob/var.metrics-exposed;
    fi
}

checkPomXML(){
    echo -e "<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
    </dependency>
    <dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>" | sed 's/^[ \t]*//;s/[ \t]*$//' > .bob/var.compareToLine;

    grep -B2 -A7 "<artifactId>micrometer-core</artifactId>" ./pom.xml | sed 's/^[ \t]*//;s/[ \t]*$//' > .bob/var.scrapedLine || true;
    sort -u .bob/var.scrapedLine -o .bob/var.scrapedLine;
    sort -u .bob/var.compareToLine -o .bob/var.compareToLine;

    comm -23 .bob/var.compareToLine .bob/var.scrapedLine > .bob/var.dependancy;
    if [ -s ".bob/var.dependancy" ]
    then
        echo -e "FAILURE: This stage has failed as the lines needed for metric exposure are not correctly implemented inside pom.xml.\nPlease refer to the page provided:\n$METRICS_EXPOSURE_TUTORIAL_URL";
        echo -e "Dependancies needed:"
        cat .bob/var.compareToLine
        echo -e "\nWhat was provided:"
        cat .bob/var.scrapedLine
        echo "false" >> .bob/var.metrics-exposed;
    else
        echo "SUCCESS: pom.xml containes all the lines necessary for metrcis exposure.";
        echo "true" >> .bob/var.metrics-exposed;
    fi
}

passOrFailCheck(){
    if grep -q "false" .bob/var.metrics-exposed; then
        echo "FAILURE: Please review console output to find the files which should be corrected.";
        exit 1;
    else
        echo "SUCCESS: All necessary lines for metrics exposure implemented correctly.";
    fi

}

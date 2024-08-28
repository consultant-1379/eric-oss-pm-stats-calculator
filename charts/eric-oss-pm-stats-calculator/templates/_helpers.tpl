{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-oss-pm-stats-calculator.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "eric-oss-pm-stats-calculator.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-oss-pm-stats-calculator.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Argument: imageName
Returns image path of provided imageName based on eric-product-info.yaml.
*/}}
{{- define "eric-oss-pm-stats-calculator.imagePath" }}
  {{- $global := fromJson (include "eric-oss-pm-stats-calculator.global" .) -}}
  {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
  {{- $image := (get $productInfo.images .imageName) -}}
  {{- $registryUrl := $image.registry -}}
  {{- $repoPath := $image.repoPath -}}
  {{- $name := $image.name -}}
  {{- $tag := $image.tag -}}
  {{- if $global.registry.url -}}
    {{- $registryUrl = $global.registry.url -}}
  {{- end -}}
  {{- if not (kindIs "invalid" $global.registry.repoPath) -}}
    {{- $repoPath = $global.registry.repoPath -}}
  {{- end -}}
  {{- if .Values.imageCredentials -}}
    {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
      {{- $repoPath = .Values.imageCredentials.repoPath -}}
    {{- end -}}
    {{- if hasKey .Values.imageCredentials .imageName -}}
      {{- $credImage := get .Values.imageCredentials .imageName }}
      {{- if $credImage.registry -}}
        {{- if $credImage.registry.url -}}
          {{- $registryUrl = $credImage.registry.url -}}
        {{- end -}}
      {{- end -}}
      {{- if not (kindIs "invalid" $credImage.repoPath) -}}
        {{- $repoPath = $credImage.repoPath -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- if $repoPath -}}
    {{- $repoPath = printf "%s/" $repoPath -}}
  {{- end -}}
  {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Create a map from ".Values.global" with defaults if missing in values file.
This hides defaults from values file.
*/}}
{{- define "eric-oss-pm-stats-calculator.global" -}}
  {{- $globalDefaults := dict "annotations" (dict) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "internalIPFamily" "") -}}
  {{- $globalDefaults := merge $globalDefaults (dict "labels" (dict)) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "networkPolicy" (dict "enabled" false)) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "nodeSelector" (dict)) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "pullSecret" "") -}}
  {{- $globalDefaults := merge $globalDefaults (dict "registry" (dict "imagePullPolicy" "IfNotPresent")) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "registry" (dict "repoPath" nil)) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "registry" (dict "url" "")) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "security" (dict "policyBinding" (dict "create" false))) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "security" (dict "policyReferenceMap" (dict "default-restricted-security-policy" "default-restricted-security-policy"))) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "security" (dict "privilegedPolicyClusterRoleName" "")) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "security" (dict "tls" (dict "enabled" true))) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "timezone" "UTC") -}}
  {{- $globalDefaults := merge $globalDefaults (dict "tolerations" (list)) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "securityPolicy" (dict "rolekind" "")) -}}
  {{- if .Values.global -}}
    {{- mergeOverwrite $globalDefaults .Values.global | toJson -}}
  {{- else -}}
    {{- $globalDefaults | toJson -}}
  {{- end -}}
{{- end -}}

{{- define "eric-oss-pm-stats-calculator.name.noQuote" }}
  {{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "eric-oss-pm-stats-calculator.configmap.name" }}
  {{- include "eric-oss-pm-stats-calculator.name.noQuote" . | printf "%s-configmap" | quote }}
{{- end }}

{{/*
Define timezone
*/}}
{{- define "eric-oss-pm-stats-calculator.timezone" -}}
{{- $glob := fromJson (include "eric-oss-pm-stats-calculator.global" .) -}}
{{- $timezone := $glob.timezone }}
{{- print $timezone | quote -}}
{{- end -}}

{{/*
Create a merged set of nodeSelectors from global and service level - calculator.
*/}}
{{- define "eric-oss-pm-stats-calculator.nodeSelector" -}}
{{- $global := fromJson (include "eric-oss-pm-stats-calculator.global" .) -}}
{{- $globalValue := (dict) -}}
{{- $globalValue = $global.nodeSelector -}}
{{- if .Values.nodeSelector -}}
  {{- range $key, $localValue := .Values.nodeSelector -}}
    {{- if hasKey $globalValue $key -}}
      {{- $Value := index $globalValue $key -}}
      {{- if ne $Value $localValue -}}
        {{- printf "nodeSelector \"%s\" is specified in both global (%s: %s) and service level (%s: %s) with differing values which is not allowed." $key $key $globalValue $key $localValue | fail -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- toYaml (merge $globalValue .Values.nodeSelector) | trim | nindent 2 -}}
{{- else -}}
  {{- if not ( empty $globalValue ) -}}
    {{- toYaml $globalValue | trim | nindent 2 -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence
*/}}
{{- define "eric-oss-pm-stats-calculator.pullSecrets" -}}
{{- $global := fromJson (include "eric-oss-pm-stats-calculator.global" .) -}}
{{- $pullSecret := $global.pullSecret -}}
{{- if .Values.imageCredentials -}}
  {{- if .Values.imageCredentials.pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.pullSecret -}}
  {{- end -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Support user defined labels
*/}}
{{- define "eric-oss-pm-stats-calculator.user-labels" }}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end }}
{{- end }}

{{/*
Helm and Kubernetes labels
*/}}
{{- define "eric-oss-pm-stats-calculator.helmK8s-labels" }}
app.kubernetes.io/name: {{ include "eric-oss-pm-stats-calculator.name" . }}
helm.sh/chart: {{ include "eric-oss-pm-stats-calculator.chart" . }}
app.kubernetes.io/version: {{ include "eric-oss-pm-stats-calculator.version" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app: {{ include "eric-oss-pm-stats-calculator.name" . }}
chart: {{ include "eric-oss-pm-stats-calculator.chart" . }}
release: {{ .Release.Name }}
heritage: {{ .Release.Service }}

{{- end -}}

{{/*
Common labels
*/}}
{{- define "eric-oss-pm-stats-calculator.labels" -}}
  {{- $global := fromJson (include "eric-oss-pm-stats-calculator.global" .) -}}
  {{- $helmK8sLabels := include "eric-oss-pm-stats-calculator.helmK8s-labels" . | fromYaml -}}
  {{- $globalLabels := $global.labels -}}
  {{- $serviceLabels := .Values.labels -}}
  {{- include "eric-oss-pm-stats-calculator.mergeLabels" (dict "location" .Template.Name "sources" (list $helmK8sLabels $globalLabels $serviceLabels)) | trim }}
{{- end -}}

{{/*
CNOM labels
*/}}
{{- define "eric-oss-pm-stats-calculator.cnom-labels" -}}
ericsson.com/cnom-server-dashboard-models: "true"
{{- end -}}

{{/*
CNOM pod label to use for groupping and splitting in the dashboard and prometheus queries, depending on service mesh and tls settings
*/}}
{{- define "eric-oss-pm-stats-calculator.cnom-pod-label" -}}
{{- $serviceMesh := include "eric-oss-pm-stats-calculator.service-mesh-enabled" . | trim -}}
{{- $tls := include "eric-oss-pm-stats-calculator.global-security-tls-enabled" . | trim -}}
{{- if (eq $tls "true") -}}
pod_name
{{- else -}}
kubernetes_pod_name
{{- end -}}
{{- end -}}

{{/*
Common annotations
*/}}
{{- define "eric-oss-pm-stats-calculator.annotations" -}}
  {{- $global := fromJson (include "eric-oss-pm-stats-calculator.global" .) -}}
  {{- $productInfoAnn := include "eric-oss-pm-stats-calculator.product-info" . | fromYaml -}}
  {{- $globalAnn := $global.annotations -}}
  {{- $serviceAnn := .Values.annotations -}}
  {{- $prometheusAnn := include "eric-oss-pm-stats-calculator.prometheus" . | fromYaml -}}
  {{- include "eric-oss-pm-stats-calculator.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfoAnn $globalAnn $serviceAnn $prometheusAnn)) | trim }}
{{- end -}}

{{/*
Test annotations
*/}}
{{- define "eric-oss-pm-stats-calculator.testAnnotations" -}}
  {{- $helmHooks := dict "helm.sh/hook" "test-success" -}}
  {{- $commonAnn := include "eric-oss-pm-stats-calculator.annotations" . | fromYaml }}
  {{- include "eric-oss-pm-stats-calculator.mergeAnnotations" (dict "location" .Template.Name "sources" (list $helmHooks $commonAnn)) | trim | nindent 4 }}
{{- end -}}

{{/*
Secret annotations
*/}}
{{- define "eric-oss-pm-stats-calculator.secretAnnotations" -}}
  {{- $global := fromJson (include "eric-oss-pm-stats-calculator.global" .) -}}
  {{- $productInfoAnn := include "eric-oss-pm-stats-calculator.product-info" . | fromYaml -}}
  {{- $globalAnn := $global.annotations -}}
  {{- $serviceAnn := .Values.annotations -}}
  {{- $helmHooks := dict "helm.sh/hook" "pre-install,pre-upgrade" -}}
  {{- $helmHookWeights := dict "helm.sh/hook-weight" "1" -}}
  {{- $helmHookDeletePolicies := dict "helm.sh/hook-delete-policy" "before-hook-creation" -}}
  {{- include "eric-oss-pm-stats-calculator.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfoAnn $globalAnn $serviceAnn $helmHooks $helmHookWeights $helmHookDeletePolicies)) | trim }}
{{- end -}}

{{/*
Merge global and service tolerations
*/}}
{{- define "eric-oss-pm-stats-calculator.tolerations" -}}
  {{- $global := fromJson (include "eric-oss-pm-stats-calculator.global" . ) -}}
  {{- include "eric-oss-pm-stats-calculator.merge-tolerations" (dict "globalTolerations" $global.tolerations "serviceTolerations" .Values.tolerations) -}}
{{- end -}}

{{/*
Create prometheus info
*/}}
{{- define "eric-oss-pm-stats-calculator.prometheus" -}}
prometheus.io/path: {{ .Values.prometheus.path | quote }}
{{/*Port commented out so prometheus can identify all exposed ports for metrics til JMX exporter can be removed*/}}
{{/*prometheus.io/port: {{ .Values.prometheus.port | quote }}*/}}
prometheus.io/scrape: {{ .Values.prometheus.scrape | quote }}
{{- end -}}

{{- define "eric-oss-pm-stats-calculator.product-info" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
The name of the cluster role used during openshift deployments.
This helper is provided to allow use of the new global.security.privilegedPolicyClusterRoleName if set, otherwise
use the previous naming convention of <release_name>-allowed-use-privileged-policy for backwards compatibility.
*/}}
{{- define "eric-oss-pm-stats-calculator.privileged.cluster.role.name" -}}
  {{- $global := fromJson (include "eric-oss-pm-stats-calculator.global" .) -}}
  {{- if $global.security.privilegedPolicyClusterRoleName -}}
    {{ $global.security.privilegedPolicyClusterRoleName }}
  {{- else -}}
    {{ .Release.Name }}-allowed-use-privileged-policy
  {{- end -}}
{{- end -}}

{{/*
Argument: imageName
Returns imagePullPolicy for provided imageName based on eric-product-info.yaml.
*/}}
{{- define "eric-oss-pm-stats-calculator.imagePullPolicy" }}
  {{- $global := fromJson (include "eric-oss-pm-stats-calculator.global" .) -}}
  {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
  {{- $image := (get $productInfo.images .imageName) -}}
  {{- $imagePullPolicy := $global.registry.imagePullPolicy -}}
  {{- if .Values.imageCredentials -}}
    {{- if hasKey .Values.imageCredentials .imageName -}}
      {{- $credImage := get .Values.imageCredentials .imageName }}
      {{- if $credImage.registry -}}
        {{- if $credImage.registry.imagePullpolicy -}}
          {{- $imagePullPolicy = $credImage.registry.imagePullpolicy -}}
        {{- end -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- print $imagePullPolicy -}}
{{- end -}}

{{/*
check global.security.tls.enabled
*/}}
{{- define "eric-oss-pm-stats-calculator.global-security-tls-enabled" -}}
{{- if  .Values.global -}}
  {{- if  .Values.global.security -}}
    {{- if  .Values.global.security.tls -}}
      {{- .Values.global.security.tls.enabled | toString -}}
    {{- else -}}
      {{- "false" -}}
    {{- end -}}
  {{- else -}}
    {{- "false" -}}
  {{- end -}}
{{- else -}}
  {{- "false" -}}
{{- end -}}
{{- end -}}

{{/*
DR-D470217-007-AD This helper defines whether the service mesh is enabled or not.
*/}}
{{- define "eric-oss-pm-stats-calculator.service-mesh-enabled" }}
  {{- $globalMeshEnabled := "false" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.serviceMesh -}}
        {{- $globalMeshEnabled = .Values.global.serviceMesh.enabled -}}
    {{- end -}}
  {{- end -}}
  {{- $globalMeshEnabled -}}
{{- end -}}

{{/*
DR-D1135-540 This helper defines the backup type for br agent
*/}}
{{- define "eric-oss-pm-stats-calculator.agent-backupTypes" }}
{{- range $i, $e := .Values.brAgent.backupTypeList -}}
{{- if eq $i 0 -}}{{- printf " " -}}{{- else -}}{{- printf ";" -}}{{- end -}}{{- . -}}
{{- end -}}
{{- end -}}

{{/*
DR-D470217-011 This helper defines the annotation which brings the service into the mesh.
*/}}
{{- define "eric-oss-pm-stats-calculator.service-mesh-inject" }}
{{- if eq (include "eric-oss-pm-stats-calculator.service-mesh-enabled" .) "true" }}
sidecar.istio.io/inject: "true"
{{- else -}}
sidecar.istio.io/inject: "false"
{{- end -}}
{{- end -}}

{{/*
Define kafka bootstrap server
*/}}
{{- define "eric-oss-pm-stats-calculator.kafka-bootstrap-server" -}}
{{- $kafkaBootstrapServer := "" -}}
{{- $serviceMesh := ( include "eric-oss-pm-stats-calculator.service-mesh-enabled" . ) -}}
{{- $tls := ( include "eric-oss-pm-stats-calculator.global-security-tls-enabled" . ) -}}
{{- if and (eq $serviceMesh "true") (eq $tls "true") -}}
    {{- $kafkaBootstrapServer = printf "%s:%s" .Values.kafka.hostname (.Values.kafka.portTls | toString) | quote -}}
{{ else }}
    {{- $kafkaBootstrapServer = printf "%s:%s" .Values.kafka.hostname (.Values.kafka.port | toString) | quote -}}
{{ end }}
{{- print $kafkaBootstrapServer -}}
{{- end -}}

{{/*
This helper defines the annotation for define service mesh volume.
*/}}
{{- define "eric-oss-pm-stats-calculator.service-mesh-volume" }}
{{- if and (eq (include "eric-oss-pm-stats-calculator.service-mesh-enabled" .) "true") (eq (include "eric-oss-pm-stats-calculator.global-security-tls-enabled" .) "true") }}
{{- if and (eq (include "eric-oss-pm-stats-calculator.osm2ism-enabled" .) "true") (eq (include "eric-oss-pm-stats-calculator.osmService-csac-enabled" .) "true") -}}
sidecar.istio.io/userVolume: '{"eric-oss-pm-stats-calculator-bro-cert-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-bro-secret","optional":true}},"eric-data-document-database-pg-postgres-cert-tls":{"secret":{"secretName":"eric-pm-kpi-data-postgres-cert","optional":true}},"eric-oss-pm-stats-calculator-eric-pm-kpi-data-certs-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-eric-pm-kpi-data-secret","optional":true}},"eric-oss-pm-stats-calculator-kafka-bootstrap-certs-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-eric-oss-dmm-kf-op-sz-kafka-bootstrap-secret","optional":true}},"eric-oss-pm-stats-calculator-search-engine-certs-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-search-engine-secret","optional":true}},"eric-oss-pm-stats-calculator-eric-log-transformer-certs-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-eric-log-transformer-secret","optional":true}},"eric-oss-pm-stats-calculator-osm-service-csac-certs-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-{{ include "eric-oss-pm-stats-calculator.osmService-csac-name" . }}-secret","optional":true}},"eric-oss-pm-stats-calculator-eric-dst-collector-certs-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-eric-dst-collector-secret","optional":true}},"eric-oss-pm-stats-calculator-certs-ca-tls":{"secret":{"secretName":"eric-sec-sip-tls-trusted-root-cert"}}}'
sidecar.istio.io/userVolumeMount: '{"eric-oss-pm-stats-calculator-bro-cert-tls":{"mountPath":"/etc/istio/tls/bro/","readOnly":true},"eric-data-document-database-pg-postgres-cert-tls":{"mountPath":"/etc/istio/tls/pg-postgres/","readOnly":true},"eric-oss-pm-stats-calculator-eric-pm-kpi-data-certs-tls":{"mountPath":"/etc/istio/tls/eric-pm-kpi-data/","readOnly":true},"eric-oss-pm-stats-calculator-kafka-bootstrap-certs-tls":{"mountPath":"/etc/istio/tls/eric-oss-dmm-kf-op-sz-kafka-bootstrap/","readOnly":true},"eric-oss-pm-stats-calculator-search-engine-certs-tls":{"mountPath":"/etc/istio/tls/search-engine/","readOnly":true},"eric-oss-pm-stats-calculator-eric-log-transformer-certs-tls":{"mountPath":"/etc/istio/tls/eric-log-transformer/","readOnly":true},"eric-oss-pm-stats-calculator-osm-service-csac-certs-tls":{"mountPath":"/etc/istio/tls/{{ include "eric-oss-pm-stats-calculator.osmService-csac-name" . }}/","readOnly":true},"eric-oss-pm-stats-calculator-eric-dst-collector-certs-tls":{"mountPath":"/etc/istio/tls/eric-dst-collector/","readOnly":true},"eric-oss-pm-stats-calculator-certs-ca-tls":{"mountPath":"/etc/istio/tls-ca","readOnly":true}}'
{{- else -}}
sidecar.istio.io/userVolume: '{"eric-oss-pm-stats-calculator-bro-cert-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-bro-secret","optional":true}},"eric-data-document-database-pg-postgres-cert-tls":{"secret":{"secretName":"eric-pm-kpi-data-postgres-cert","optional":true}},"eric-oss-pm-stats-calculator-eric-pm-kpi-data-certs-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-eric-pm-kpi-data-secret","optional":true}},"eric-oss-pm-stats-calculator-kafka-bootstrap-certs-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-eric-oss-dmm-kf-op-sz-kafka-bootstrap-secret","optional":true}},"eric-oss-pm-stats-calculator-search-engine-certs-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-search-engine-secret","optional":true}},"eric-oss-pm-stats-calculator-eric-log-transformer-certs-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-eric-log-transformer-secret","optional":true}},"eric-oss-pm-stats-calculator-eric-dst-collector-certs-tls":{"secret":{"secretName":"eric-oss-pm-stats-calculator-eric-dst-collector-secret","optional":true}},"eric-oss-pm-stats-calculator-certs-ca-tls":{"secret":{"secretName":"eric-sec-sip-tls-trusted-root-cert"}}}'
sidecar.istio.io/userVolumeMount: '{"eric-oss-pm-stats-calculator-bro-cert-tls":{"mountPath":"/etc/istio/tls/bro/","readOnly":true},"eric-data-document-database-pg-postgres-cert-tls":{"mountPath":"/etc/istio/tls/pg-postgres/","readOnly":true},"eric-oss-pm-stats-calculator-eric-pm-kpi-data-certs-tls":{"mountPath":"/etc/istio/tls/eric-pm-kpi-data/","readOnly":true},"eric-oss-pm-stats-calculator-kafka-bootstrap-certs-tls":{"mountPath":"/etc/istio/tls/eric-oss-dmm-kf-op-sz-kafka-bootstrap/","readOnly":true},"eric-oss-pm-stats-calculator-search-engine-certs-tls":{"mountPath":"/etc/istio/tls/search-engine/","readOnly":true},"eric-oss-pm-stats-calculator-eric-log-transformer-certs-tls":{"mountPath":"/etc/istio/tls/eric-log-transformer/","readOnly":true},"eric-oss-pm-stats-calculator-eric-dst-collector-certs-tls":{"mountPath":"/etc/istio/tls/eric-dst-collector/","readOnly":true},"eric-oss-pm-stats-calculator-certs-ca-tls":{"mountPath":"/etc/istio/tls-ca","readOnly":true}}'
{{ end }}
{{ end }}
{{- end -}}

{{/*
This helper defines which out-mesh services are reached by the eric-oss-pm-stats-calculator.
*/}}
{{- define "eric-oss-pm-stats-calculator.service-mesh-ism2osm-labels" }}
{{- if eq (include "eric-oss-pm-stats-calculator.service-mesh-enabled" .) "true" }}
  {{- if eq (include "eric-oss-pm-stats-calculator.global-security-tls-enabled" .) "true" }}
eric-oss-dmm-kf-op-sz-kafka-ism-access: "true"
eric-pm-kpi-data-ism-access: "true"
eric-pm-kpi-data-service-ism-access: "true"
eric-log-transformer-ism-access: "true"
eric-dst-collector-ism-access: "true"
  {{- end }}
{{- end -}}
{{- end -}}

{{/*
GL-D470217-080-AD
This helper captures the service mesh version from the integration chart to
annotate the workloads so they are redeployed in case of service mesh upgrade.
*/}}
{{- define "eric-oss-pm-stats-calculator.service-mesh-version" }}
{{- if eq (include "eric-oss-pm-stats-calculator.service-mesh-enabled" .) "true" }}
  {{- if .Values.global.serviceMesh -}}
    {{- if .Values.global.serviceMesh.annotations -}}
      {{ .Values.global.serviceMesh.annotations | toYaml }}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
Merged labels for Default, which includes Standard and Config
*/}}
{{- define "eric-oss-pm-stats-calculator.service-mesh-labels" -}}
  {{- $servicemeshInject := include "eric-oss-pm-stats-calculator.service-mesh-inject" . | fromYaml -}}
  {{- $servicemeshIsm2osm := include "eric-oss-pm-stats-calculator.service-mesh-ism2osm-labels" . | fromYaml -}}
  {{- include "eric-oss-pm-stats-calculator.mergeLabels" (dict "location" .Template.Name "sources" (list $servicemeshInject $servicemeshIsm2osm)) | trim }}
{{- end -}}

{{/*
Merged annotations for Default, which includes productInfo and config
*/}}
{{- define "eric-oss-pm-stats-calculator.service-mesh-annotations" -}}
  {{- $servicemeshInject := include "eric-oss-pm-stats-calculator.service-mesh-inject" . | fromYaml -}}
  {{- $servicemeshVersion := include "eric-oss-pm-stats-calculator.service-mesh-version" . | fromYaml -}}
  {{- $servicemeshVolume := include "eric-oss-pm-stats-calculator.service-mesh-volume" . | fromYaml }}
  {{- $servicemeshProxy := include "eric-oss-pm-stats-calculator.service-mesh-proxy" . | fromYaml }}
  {{- include "eric-oss-pm-stats-calculator.mergeAnnotations" (dict "location" .Template.Name "sources" (list $servicemeshInject $servicemeshVersion $servicemeshVolume $servicemeshProxy)) | trim }}
{{- end -}}

{{/*
This helper get the custom user used to connect to postgres DB instance.
*/}}
{{ define "eric-oss-pm-stats-calculator.dbuser" }}
  {{- $secret := (lookup "v1" "Secret" .Release.Namespace .Values.kpiData.credentials.kubernetesSecretName) -}}
  {{- if $secret -}}
    {{ index $secret.data "username" | b64dec | quote }}
  {{- else -}}
    {{- "kpi_service_user" | quote -}}
  {{- end -}}
{{- end -}}

{{/*
Starting port number of the spark driver process.
In case of multiple driver instances, next port is the last assigned port incremented by 1.
Port range must be large enough to handle nr. of driver instances.
*/}}
{{- define "eric-oss-pm-stats-calculator.spark-driver-port" }}
{{- 34444 }}
{{- end -}}

{{/*
Starting port number of the spark blockmanager process.
In case of multiple driver instances, next port is the last assigned port incremented by 1.
Port range must be large enough to handle nr. of driver instances.
*/}}
{{- define "eric-oss-pm-stats-calculator.spark-blockmanager-port" }}
{{- 35555 }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "eric-oss-pm-stats-calculator.serviceAccountName" -}}
  {{- if .Values.serviceAccount.create }}
    {{- default (print (include "eric-oss-pm-stats-calculator.name" .) "-sa") .Values.serviceAccount.name }}
  {{- else }}
    {{- default "default" .Values.serviceAccount.name }}
  {{- end }}
{{- end }}

{{/*
Define rolekind and rolename for DR-D1123-134
*/}}
{{- define "eric-oss-pm-stats-calculator.securityPolicy.rolekind" -}}
  {{- $global := fromJson (include "eric-oss-pm-stats-calculator.global" .) -}}
  {{- $rolekind := $global.securityPolicy.rolekind -}}
  {{- if and (ne $rolekind "Role") (ne $rolekind "ClusterRole") -}}
    {{- printf "For global.securityPolicy.rolekind only \"Role\", \"ClusterRole\" or \"\" is allowed as values." | fail -}}
  {{- end -}}
  {{- $rolekind -}}
{{- end -}}

{{- define "eric-oss-pm-stats-calculator.securityPolicy.rolename" -}}
  {{- if (.Values.securityPolicy).rolename -}}
    {{ .Values.securityPolicy.rolename }}
  {{- else -}}
    {{ include "eric-oss-pm-stats-calculator.name" . }}
  {{- end -}}
{{- end -}}

{{/*
Define RolebindingName - DR-D1123-134
*/}}
{{- define "eric-oss-pm-stats-calculator.securityPolicy.rolebindingName" -}}
  {{- $global := fromJson (include "eric-oss-pm-stats-calculator.global" .) -}}
  {{- $rolekind := $global.securityPolicy.rolekind -}}
  {{- if (eq $rolekind "Role") -}}
    {{- print "sp-" (include "eric-oss-pm-stats-calculator.serviceAccountName" .) "-r-" (include "eric-oss-pm-stats-calculator.securityPolicy.rolename" .) -}}
  {{- else if (eq $rolekind "ClusterRole") -}}
    {{- print "sp-" (include "eric-oss-pm-stats-calculator.serviceAccountName" .) "-c-" (include "eric-oss-pm-stats-calculator.securityPolicy.rolename" .) -}}
  {{- end }}
{{- end }}

{{/*
Streaming method selection logic.
Precedence order:
  log.streamingMethod > log.outputs > global.log.streamingMethod > global.log.outputs > "indirect"
In other words, the new streamingMethod parameter has higher importance than the old outputs parameter.
Local overrides global, and if nothing set then indirect (stdout) is chosen.
*/}}
{{ define "eric-oss-pm-stats-calculator.log-streamingMethod" }}
  {{- $streamingMethod := "indirect" -}}
  {{- if (((.Values.global).log).outputs) -}}
    {{- if has "stdout" .Values.global.log.outputs -}}
    {{- $streamingMethod = "indirect" -}}
    {{- end -}}
    {{- if has "stream" .Values.global.log.outputs -}}
    {{- $streamingMethod = "direct" -}}
    {{- end -}}
    {{- if and (has "stdout" .Values.global.log.outputs) (has "stream" .Values.global.log.outputs) -}}
    {{- $streamingMethod = "dual" -}}
    {{- end -}}
  {{- end -}}
  {{- if (((.Values.global).log).streamingMethod) -}}
    {{- $streamingMethod = .Values.global.log.streamingMethod -}}
  {{- end -}}
  {{- if ((.Values.log).streamingMethod) -}}
    {{- $streamingMethod = .Values.log.streamingMethod -}}
  {{- end -}}
  {{- printf "%s" $streamingMethod -}}
{{ end }}

{{- define "eric-oss-pm-stats-calculator.log-streaming-activated" }}
  {{- $streamingMethod := (include "eric-oss-pm-stats-calculator.log-streamingMethod" .) -}}
  {{- if or (eq $streamingMethod "dual") (eq $streamingMethod "direct") -}}
    {{- printf "%t" true -}}
  {{- else -}}
    {{- printf "%t" false -}}
  {{- end -}}
{{- end -}}

{{- define "eric-oss-pm-stats-calculator.pod-disruption-budget" -}}
{{- if or (eq "0" (.Values.podDisruptionBudget.minAvailable | toString )) (not (empty .Values.podDisruptionBudget.minAvailable )) }}
minAvailable: {{ .Values.podDisruptionBudget.minAvailable }}
{{- else if or (eq "0" (.Values.podDisruptionBudget.maxUnavailable | toString )) (not (empty .Values.podDisruptionBudget.maxUnavailable )) }}
maxUnavailable: {{ .Values.podDisruptionBudget.maxUnavailable }}
{{- else }}
minAvailable: 1
{{- end }}
{{- end -}}

{{/*
Define eric-oss-pm-stats-calculator.appArmorProfileAnnotation
*/}}
{{- define "eric-oss-pm-stats-calculator.appArmorProfileAnnotation" -}}
{{- $containerList := list "eric-oss-pm-stats-calculator" -}}
{{- if and (.Values.jmxExporter.enabled) (eq .Values.jmxExporter.enabled true) }}
  {{- $containerList = append $containerList "eric-oss-pm-stats-calculator-jmx-exporter" -}}
{{- end -}}
{{- $containerList = append $containerList "eric-oss-pm-stats-calculator-data-init" -}}
{{- $acceptedProfiles := list "unconfined" "runtime/default" "localhost" }}
{{- $commonProfile := dict -}}
{{- if .Values.appArmorProfile.type -}}
  {{- $_ := set $commonProfile "type" .Values.appArmorProfile.type -}}
  {{- if and (eq .Values.appArmorProfile.type "localhost") .Values.appArmorProfile.localhostProfile -}}
    {{- $_ := set $commonProfile "localhostProfile" .Values.appArmorProfile.localhostProfile -}}
  {{- end -}}
{{- end -}}
{{- $profiles := dict -}}
{{- range $container := $containerList -}}
  {{- if and (hasKey $.Values.appArmorProfile $container) (index $.Values.appArmorProfile $container "type") -}}
    {{- $_ := set $profiles $container (index $.Values.appArmorProfile $container) -}}
  {{- else -}}
    {{- $_ := set $profiles $container $commonProfile -}}
  {{- end -}}
{{- end -}}
{{- range $key, $value := $profiles -}}
  {{- if $value.type -}}
    {{- if not (has $value.type $acceptedProfiles) -}}
      {{- fail (printf "Unsupported appArmor profile type: %s, use one of the supported profiles %s" $value.type $acceptedProfiles) -}}
    {{- end -}}
    {{- if and (eq $value.type "localhost") (empty $value.localhostProfile) -}}
      {{- fail "The 'localhost' appArmor profile requires a profile name to be provided in localhostProfile parameter." -}}
    {{- end }}
container.apparmor.security.beta.kubernetes.io/{{ $key }}: {{ $value.type }}{{ eq $value.type "localhost" | ternary (printf "/%s" $value.localhostProfile) ""  }}
  {{- end -}}
{{- end -}}
{{- end }}

{{/*
This helper defines checks whether osm2ism is enabled for pmsc.
*/}}
{{- define "eric-oss-pm-stats-calculator.osm2ism-enabled" }}
  {{- $osm2ismEnabled := "false" -}}
  {{- if .Values.osm2ism -}}
    {{- if .Values.osm2ism.enabled -}}
        {{- $osm2ismEnabled = .Values.osm2ism.enabled -}}
    {{- end -}}
  {{- end -}}
  {{- $osm2ismEnabled -}}
{{- end -}}

{{/*
This helper checks osm2ism is enabled for the out-mesh service CSAC
*/}}
{{- define "eric-oss-pm-stats-calculator.osmService-csac-enabled" }}
{{- if eq (include "eric-oss-pm-stats-calculator.osm2ism-enabled" .) "true" }}
  {{- $serviceCsacEnabled := false -}}
  {{- if .Values.osm2ism -}}
    {{- if .Values.osm2ism.outMeshServices.csac -}}
      {{- $serviceCsacEnabled = .Values.osm2ism.outMeshServices.csac.enabled -}}
    {{- end -}}
  {{- end -}}
  {{- $serviceCsacEnabled -}}
{{- end -}}
{{- end -}}

{{/*
This helper checks the issuer reference is enabled for the out-mesh service CSAC.
*/}}
{{- define "eric-oss-pm-stats-calculator.osmService-csac-issuerRef-enabled" }}
{{- if eq (include "eric-oss-pm-stats-calculator.osm2ism-enabled" .) "true" }}
  {{- $osmIntermediateCaEnabled := false -}}
  {{- if .Values.osm2ism -}}
    {{- if .Values.osm2ism.outMeshServices.csac -}}
      {{- if eq (include "eric-oss-pm-stats-calculator.osmService-csac-enabled" .) "true" }}
        {{- if .Values.osm2ism.outMeshServices.csac.intermediateCA -}}
          {{- if .Values.osm2ism.outMeshServices.csac.intermediateCA.enabled -}}
              {{- $osmIntermediateCaEnabled = .Values.osm2ism.outMeshServices.csac.intermediateCA.enabled -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- $osmIntermediateCaEnabled -}}
{{- end -}}
{{- end -}}

{{/*
This helper captures the issuer reference of the out-mesh service CSAC, that wants to communicate with this service.
*/}}
{{- define "eric-oss-pm-stats-calculator.osmService-csac-issuerRef" }}
{{- if eq (include "eric-oss-pm-stats-calculator.osm2ism-enabled" .) "true" }}
  {{- $outMeshServiceIssuerRef := "" -}}
  {{- if .Values.osm2ism -}}
    {{- if .Values.osm2ism.outMeshServices -}}
      {{- if .Values.osm2ism.outMeshServices.csac -}}
        {{- if eq (include "eric-oss-pm-stats-calculator.osmService-csac-enabled" .) "true" }}
          {{- if .Values.osm2ism.outMeshServices.csac.intermediateCA -}}
            {{- if eq (include "eric-oss-pm-stats-calculator.osmService-csac-issuerRef-enabled" .) "true" }}
                {{- $outMeshServiceIssuerRef = .Values.osm2ism.outMeshServices.csac.intermediateCA.name -}}
            {{- end -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- $outMeshServiceIssuerRef -}}
{{- end -}}
{{- end -}}


{{/*
This helper captures the repo name of out-mesh service CSAC, which wants to communicate with this service.
*/}}
{{- define "eric-oss-pm-stats-calculator.osmService-csac-name" }}
{{- if eq (include "eric-oss-pm-stats-calculator.osm2ism-enabled" .) "true" }}
  {{- $outMeshServiceName := "" -}}
  {{- if .Values.osm2ism -}}
    {{- if .Values.osm2ism.outMeshServices.csac -}}
      {{- if eq (include "eric-oss-pm-stats-calculator.osmService-csac-enabled" .) "true" }}
          {{- $outMeshServiceName = .Values.osm2ism.outMeshServices.csac.name | trunc 30 -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
  {{- $outMeshServiceName -}}
{{- end -}}
{{- end -}}


{{- /*
Define JVM_LOCAL_OPTS for JMX Exporter
*/}}
{{- define "eric-oss-pm-stats-calculator.jmx_jvm_local_opts" -}}
{{- $opts_list := (default list .Values.jmxExporter.jvmLocalOpts) -}}
{{- if (index .Values.jmxExporter "debug" "enabled") -}}
    {{- $opts_list = append $opts_list "-Djava.util.logging.config.file=/opt/jmx_exporter/logging.properties" }}
{{- end -}}
{{- $opts_list | join " " -}}
{{- end -}}


{{/*
Define logstash port
*/}}
{{- define "eric-oss-pm-stats-calculator.logstash-port" -}}
{{- $logstashPort := "" -}}
{{- $serviceMesh := ( include "eric-oss-pm-stats-calculator.service-mesh-enabled" . ) -}}
{{- $tls := ( include "eric-oss-pm-stats-calculator.global-security-tls-enabled" . ) -}}
{{- if and (eq $serviceMesh "true") (eq $tls "true") -}}
    {{- $logstashPort = .Values.log.logstash_port_tls -}}
{{ else }}
    {{- $logstashPort = .Values.log.logstash_port -}}
{{ end }}
{{- print $logstashPort -}}
{{- end -}}


{{/*
Define the log streaming method
*/}}
{{- define "eric-oss-pm-stats-calculator.streamingMethod" -}}
{{- $streamingMethod := "indirect" -}}
{{- if .Values.global -}}
  {{- if .Values.global.log -}}
      {{- if .Values.global.log.streamingMethod -}}
        {{- $streamingMethod = .Values.global.log.streamingMethod }}
      {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.log -}}
  {{- if .Values.log.streamingMethod -}}
    {{- $streamingMethod = .Values.log.streamingMethod }}
  {{- end -}}
{{- end -}}
{{- print $streamingMethod -}}
{{- end -}}


{{/*
Define the label needed for reaching eric-log-transformer
*/}}
{{- define "eric-oss-pm-stats-calculator.directStreamingLabel" -}}
{{- $streamingMethod := (include "eric-oss-pm-stats-calculator.streamingMethod" .) -}}
{{- if or (eq "direct" $streamingMethod) (eq "dual" $streamingMethod) }}
logger-communication-type: "direct"
{{- end -}}
{{- end -}}


{{/*
Define logging environment variables
*/}}
{{- define "eric-oss-pm-stats-calculator.loggingEnv" -}}
- name: POD_NAME
  valueFrom:
    fieldRef:
      fieldPath: metadata.name
- name: POD_UID
  valueFrom:
    fieldRef:
      fieldPath: metadata.uid
- name: CONTAINER_NAME
  value: eric-oss-pm-stats-calculator
- name: NODE_NAME
  valueFrom:
    fieldRef:
      fieldPath: spec.nodeName
{{- $streamingMethod := (include "eric-oss-pm-stats-calculator.streamingMethod" .) -}}
{{- if or (eq "direct" $streamingMethod) (eq "dual" $streamingMethod) -}}
  {{- if eq "direct" $streamingMethod }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-http.xml"
  {{- end }}
  {{- if eq "dual" $streamingMethod }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-dual.xml"
  {{ end }}
- name: LOGSTASH_DESTINATION
  value: {{ .Values.log.logstash_destination | default "eric-log-transformer" }}
- name: LOGSTASH_PORT
  value: {{ include "eric-oss-pm-stats-calculator.logstash-port" . | toString | default "9080" | quote }}
{{- else if eq $streamingMethod "indirect" }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-json.xml"
{{- else }}
  {{- fail ".log.streamingMethod unknown" }}
{{- end -}}
{{ end }}


{{/*
This helper defines whether DST is enabled or not.
*/}}
{{- define "eric-oss-pm-stats-calculator.dst-enabled" }}
  {{- $dstEnabled := "false" -}}
  {{- if .Values.dst -}}
    {{- if .Values.dst.enabled -}}
        {{- $dstEnabled = .Values.dst.enabled -}}
    {{- end -}}
  {{- end -}}
  {{- $dstEnabled -}}
{{- end -}}

{{/*
Define the labels needed for DST
*/}}
{{- define "eric-oss-pm-stats-calculator.dstLabels" -}}
{{- if eq (include "eric-oss-pm-stats-calculator.dst-enabled" .) "true" }}
eric-dst-collector-access: "true"
{{- end }}
{{- end -}}

{{/*
Define DST environment variables
*/}}
{{ define "eric-oss-pm-stats-calculator.dstEnv" }}
{{- $dstEnabled := (include "eric-oss-pm-stats-calculator.dst-enabled" .) }}
- name: ERIC_TRACING_ENABLED
  value: {{ $dstEnabled | quote }}
{{- if eq $dstEnabled "true" }}
- name: ERIC_EXPORTER_ENDPOINT
  value: {{ .Values.dst.collector.host }}:{{ .Values.dst.collector.portOtlpGrpc }}
- name: ERIC_SAMPLER_JAEGER_REMOTE_ENDPOINT
  value: {{ .Values.dst.collector.host }}:{{ .Values.dst.collector.portJagerGrpc }}
- name: ERIC_ZIPKIN_ENDPOINT
  value: {{ .Values.dst.collector.host }}:{{ .Values.dst.collector.portZipkinHttp }}
- name: ERIC_PROPAGATOR_TYPE
  value: {{ .Values.dst.propagator.type | quote }}
- name: SPRING_SLEUTH_ENABLED
  value: "true"
{{- end -}}
{{ end }}

{{/*
Define the labels needed for BRO network connection
*/}}
{{- define "eric-oss-pm-stats-calculator.broConnection" -}}
{{- if .Values.brAgent.enabled }}
eric-ctrl-bro-access: "true"
adpbrlabelkey: {{ .Values.brAgent.backupRegistrationName | quote }}
{{- end }}
{{- end -}}

{{/*
Set the sidecar istio container resources
*/}}
{{- define "eric-oss-pm-stats-calculator.service-mesh-proxy" }}
{{- if eq (include "eric-oss-pm-stats-calculator.service-mesh-enabled" .) "true" }}
sidecar.istio.io/proxyCPU: {{ .Values.resources.istio.requests.cpu | quote }}
sidecar.istio.io/proxyCPULimit: {{ .Values.resources.istio.limits.cpu | quote }}
sidecar.istio.io/proxyMemory: {{ .Values.resources.istio.requests.memory | quote }}
sidecar.istio.io/proxyMemoryLimit: {{ .Values.resources.istio.limits.memory | quote }}
{{- end -}}
{{- end -}}
{{/* vim: set filetype=mustache: */}}
{{/*
Truncate the name of the chart.
*/}}
{{- define "eric-oss-pm-stats-calculator-integration.name" -}}
  {{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "eric-oss-pm-stats-calculator-integration.version" -}}
  {{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-oss-pm-stats-calculator-integration.chart" -}}
  {{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Argument: imageName
Returns image path of provided imageName based on eric-product-info.yaml.
*/}}
{{- define "eric-oss-pm-stats-calculator-integration.imagePath" }}
  {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
  {{- $image := (get $productInfo.images .imageName) -}}
  {{- $registryUrl := $image.registry -}}
  {{- $repoPath := $image.repoPath -}}
  {{- $name := $image.name -}}
  {{- $tag := $image.tag -}}
  {{- if .Values.global -}}
    {{- if .Values.global.registry -}}
      {{- if .Values.global.registry.url -}}
        {{- $registryUrl = .Values.global.registry.url -}}
      {{- end -}}
      {{- if not (kindIs "invalid" .Values.global.registry.repoPath) -}}
        {{- $repoPath = .Values.global.registry.repoPath -}}
      {{- end -}}
    {{- end -}}
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
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "eric-oss-pm-stats-calculator-integration.fullname" -}}
  {{- if .Values.fullnameOverride -}}
    {{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
  {{- else -}}
    {{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
  {{- end -}}
{{- end -}}

{{/*
Create image pull secrets
*/}}
{{- define "eric-oss-pm-stats-calculator-integration.pullSecrets" -}}
  {{- if .Values.imageCredentials.pullSecret -}}
    {{- print .Values.imageCredentials.pullSecret -}}
  {{- else if .Values.global.registry.pullSecret -}}
    {{- print .Values.global.registry.pullSecret -}}
  {{- end -}}
{{- end -}}
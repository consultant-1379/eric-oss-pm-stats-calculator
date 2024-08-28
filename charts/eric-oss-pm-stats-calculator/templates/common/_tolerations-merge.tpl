{{/*
Merge global tolerations with service tolerations (DR-D1120-061-AD).
*/}}
{{- define "eric-oss-pm-stats-calculator.merge-tolerations" -}}
  {{- if .globalTolerations }}
      {{- $globalTolerations := .globalTolerations -}}
      {{- $serviceTolerations := list -}}
      {{- if .serviceTolerations -}}
        {{- if eq (typeOf .serviceTolerations) ("[]interface {}") -}}
          {{- $serviceTolerations = .serviceTolerations -}}
        {{- else if eq (typeOf .serviceTolerations) ("map[string]interface {}") -}}
          {{- $serviceTolerations = index .serviceTolerations .podbasename -}}
        {{- end -}}
      {{- end -}}
      {{- $result := list -}}
      {{- $nonMatchingItems := list -}}
      {{- $matchingItems := list -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-oss-pm-stats-calculator.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-oss-pm-stats-calculator.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-oss-pm-stats-calculator.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-oss-pm-stats-calculator.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-oss-pm-stats-calculator.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-oss-pm-stats-calculator.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $serviceItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $serviceItem -}}
        {{- end -}}
      {{- end -}}
      {{- $combinedTolerations := concat $result $matchingItems $nonMatchingItems -}}
      {{- if ne (len $combinedTolerations) 0 -}}
      {{- toYaml $combinedTolerations -}}
      {{- end -}}
  {{- else -}}
      {{- if .serviceTolerations -}}
        {{- if eq (typeOf .serviceTolerations) ("[]interface {}") -}}
          {{- toYaml .serviceTolerations -}}
        {{- else if eq (typeOf .serviceTolerations) ("map[string]interface {}") -}}
          {{- $serviceTolerations := index .serviceTolerations .podbasename -}}
          {{- if $serviceTolerations -}}
          {{- toYaml $serviceTolerations -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
  {{- end -}}
{{- end -}}

{{/*
Helper function to get the identifier of a tolerations array element.
Assumes 'key' is used to uniquely identify
a tolerations array element.
*/}}
{{ define "eric-oss-pm-stats-calculator.merge-tolerations.get-identifier" }}
  {{ printf "Debug: root.Values.global: %v" . | println }}
  {{- $keyValues := list -}}
  {{- range $key := (keys . | sortAlpha) -}}
    {{- if eq $key "key" -}}
      {{- $keyValues = append $keyValues (printf "%s=%s" $key (index $ $key)) -}}
    {{- end -}}
  {{- end -}}
  {{- printf "%s" (join "," $keyValues) -}}
{{ end }}
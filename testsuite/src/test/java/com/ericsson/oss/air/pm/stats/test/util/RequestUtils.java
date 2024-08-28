/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.impl.client.HttpClients.createDefault;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.KpiDefinitionPatchRequest;
import com.ericsson.oss.air.pm.stats.test.dto.input.ErrorResponse;
import com.ericsson.oss.air.pm.stats.test.dto.output.KpiDefinitionUpdateResponse;
import com.ericsson.oss.air.pm.stats.test.util.http.KpiHttpDelete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.json.JsonMapper.Builder;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

@NoArgsConstructor(access = PRIVATE)
public final class RequestUtils {
    private static final ObjectMapper OBJECT_MAPPER = objectMapper();

    public static OnSuccess onSuccess() {
        return new OnSuccess();
    }

    public static OnFailure onFailure() {
        return new OnFailure();
    }

    public static final class OnSuccess {

        @SneakyThrows
        public void updateKpiDefinition(
                final String kpiDefinitionName,
                final Optional<String> collectionId,
                final UnaryOperator<? super KpiDefinitionPatchRequest> requestFunction,
                final BiConsumer<Integer, KpiDefinitionUpdateResponse> responseConsumer
        ) {
            final HttpEntity httpEntity = new StringEntity(OBJECT_MAPPER.writeValueAsString(requestFunction.apply(new KpiDefinitionPatchRequest())));
            RequestUtils.updateKpiDefinition(kpiDefinitionName, collectionId, httpEntity, KpiDefinitionUpdateResponse.class, responseConsumer);
        }

        @SneakyThrows
        public void deleteKpiDefinitions(
                final List<String> kpiDefinitionNames,
                final BiConsumer<Integer, List> responseConsumer
        ) {
            deleteKpiDefinitions(kpiDefinitionNames, "", responseConsumer);
        }

        @SneakyThrows
        public void deleteKpiDefinitions(
                final List<String> kpiDefinitionNames,
                final String collectionId,
                final BiConsumer<Integer, List> responseConsumer
        ) {
            final HttpEntity httpEntity = new StringEntity(OBJECT_MAPPER.writeValueAsString(kpiDefinitionNames));
            RequestUtils.deleteKpiDefinitions(httpEntity, List.class, responseConsumer, collectionId);
        }

    }

    public static final class OnFailure {

        @SneakyThrows
        public void updateKpiDefinition(
                final String kpiDefinitionName, Optional<String> collectionId, final String payload, final BiConsumer<Integer, ErrorResponse> responseConsumer
        ) {
            final HttpEntity httpEntity = new StringEntity(payload);
            RequestUtils.updateKpiDefinition(kpiDefinitionName, collectionId, httpEntity, ErrorResponse.class, responseConsumer);
        }

        @SneakyThrows
        public void deleteKpiDefinitions(
                final String payload, final BiConsumer<Integer, ErrorResponse> responseConsumer
        ) {
            deleteKpiDefinitions(payload, "", responseConsumer);
        }

        @SneakyThrows
        public void deleteKpiDefinitions(
                final String payload, final String collectionId, final BiConsumer<Integer, ErrorResponse> responseConsumer
        ) {
            final HttpEntity httpEntity = new StringEntity(payload);
            RequestUtils.deleteKpiDefinitions(httpEntity, ErrorResponse.class, responseConsumer, collectionId);
        }

        @SneakyThrows
        public void postKpiDefinition(final String payload, @NonNull final Consumer<ErrorResponse> responseConsumer) {
            try (final CloseableHttpClient closeableHttpClient = createDefault()) {
                final HttpPost httpPost = new HttpPost();
                httpPost.setHeader(CONTENT_TYPE, String.valueOf(ContentType.APPLICATION_JSON));
                httpPost.setHeader(ACCEPT, String.valueOf(ContentType.APPLICATION_JSON));
                httpPost.setEntity(new StringEntity(payload));
                httpPost.setURI(URI.create("http://eric-oss-pm-stats-calculator:8080/kpi-handling/model/v1/definitions/"));

                try (final CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpPost)) {
                    final String entity = EntityUtils.toString(httpResponse.getEntity());
                    final ErrorResponse responseObject = OBJECT_MAPPER.readValue(entity, ErrorResponse.class);
                    responseConsumer.accept(responseObject);
                }
            }
        }

    }

    @SneakyThrows
    private static <T> void updateKpiDefinition(
            final String kpiDefinitionName, final Optional<String> collectionId, final HttpEntity httpEntity, final Class<T> valueType, @NonNull final BiConsumer<Integer, T> responseConsumer
    ){
        try (final CloseableHttpClient closeableHttpClient = createDefault()) {
            final HttpPatch httpPatch = new HttpPatch();
            httpPatch.setHeader(CONTENT_TYPE, String.valueOf(ContentType.APPLICATION_JSON));
            httpPatch.setHeader(ACCEPT, String.valueOf(ContentType.APPLICATION_JSON));
            httpPatch.setEntity(httpEntity);
            String endUri = collectionId.map(id -> "/"+id+"/"+kpiDefinitionName).orElse("/"+kpiDefinitionName);
            httpPatch.setURI(URI.create(String.format(
                    "http://eric-oss-pm-stats-calculator:8080/kpi-handling/model/v1/definitions/%s",endUri
            )));

            try (final CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpPatch)) {
                final StatusLine statusLine = httpResponse.getStatusLine();
                final String entity = EntityUtils.toString(httpResponse.getEntity());
                final T responseObject = OBJECT_MAPPER.readValue(entity, valueType);
                responseConsumer.accept(statusLine.getStatusCode(), responseObject);
            }
        }
    }

    @SneakyThrows
    private static <T> void deleteKpiDefinitions(
            final HttpEntity httpEntity, final Class<T> valueType, @NonNull final BiConsumer<Integer, T> responseConsumer,
            final String collectionId
    ) {
        try (final CloseableHttpClient closeableHttpClient = createDefault()) {
            final KpiHttpDelete httpDelete = new KpiHttpDelete("http://eric-oss-pm-stats-calculator:8080/kpi-handling/model/v1/definitions/"+collectionId);
            httpDelete.setHeader(CONTENT_TYPE, String.valueOf(ContentType.APPLICATION_JSON));
            httpDelete.setHeader(ACCEPT, String.valueOf(ContentType.APPLICATION_JSON));
            httpDelete.setEntity(httpEntity);

            try (final CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpDelete)) {
                final StatusLine statusLine = httpResponse.getStatusLine();
                final String entity = EntityUtils.toString(httpResponse.getEntity());
                final T responseObject = OBJECT_MAPPER.readValue(entity, valueType);
                responseConsumer.accept(statusLine.getStatusCode(), responseObject);
            }
        }
    }

    private static JsonMapper objectMapper() {
        final Builder builder = JsonMapper.builder();
        builder.addModule(new JavaTimeModule());
        return builder.build();
    }
}

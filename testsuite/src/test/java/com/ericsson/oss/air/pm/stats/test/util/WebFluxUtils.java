/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.core.MediaType;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationRequestSuccessResponse;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.CalculationResponse;
import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.air.pm.stats.common.rest.exception.RestExecutionException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebFluxUtils {
    private static final WebClient WEB_CLIENT = WebClient.builder()
                                                         .baseUrl("http://eric-oss-pm-stats-calculator:8080/kpi-handling/")
                                                         .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                                         .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                                                         .build();

    public static Mono<ResponseEntity<CalculationRequestSuccessResponse>> postCalculationRequest(final String fileName, final boolean isGzipCompressed) {
        final byte[] payload;
        final String contentEncodingHeader;

        if (isGzipCompressed) {
            payload = compressPayload(loadPayload(fileName));
            contentEncodingHeader = "gzip";
        } else {
            payload = loadPayload(fileName).getBytes(StandardCharsets.UTF_8);
            contentEncodingHeader = null;
        }

        return WEB_CLIENT.post()
                         .uri("calc/v1/calculations")
                         .header(HttpHeaders.CONTENT_ENCODING, contentEncodingHeader)
                         .bodyValue(payload)
                         .retrieve()
                         .toEntity(CalculationRequestSuccessResponse.class);
    }

    @SneakyThrows
    public static void getCalculationResponseUntilFinished(final UUID calculationId) {
        while (true) {
            final CalculationResponse response = WEB_CLIENT.get()
                                                           .uri(String.format("calc/v1/calculations/%s", calculationId))
                                                           .retrieve()
                                                           .bodyToMono(CalculationResponse.class)
                                                           .block();

            final KpiCalculationState status = response.getStatus();
            log.info("Calculation with ID: '{}' is in state: '{}'", calculationId, status);
            if (status == KpiCalculationState.FINISHED || status == KpiCalculationState.FINALIZING) {
                return;
            }

            if (status == KpiCalculationState.FAILED) {
                throw new RestExecutionException("On Demand calculation failed");
            }
            TimeUnit.SECONDS.sleep(5);
        }
    }

    private static String loadPayload(final String fileName) {
        final String kpiCalculationRequestPayload;
        try {
            kpiCalculationRequestPayload = ResourceLoaderUtils.getClasspathResourceAsString(fileName);
        } catch (IOException e) {
            log.error("Calculation request json file {} is invalid", fileName);
            throw new RestExecutionException(e);
        }

        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.UK).withZone(ZoneOffset.UTC);
        final String twoDaysAgo = dateTimeFormatter.format(LocalDate.now(ZoneOffset.UTC).minusDays(2));

        return kpiCalculationRequestPayload.replace("<DATE_FOR_FILTER>", String.format("\"%s\"", twoDaysAgo));
    }

    private static byte[] compressPayload(final String payload) {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(payload.getBytes());
            gzipOutputStream.finish();
            log.info("KPI payload is compressed successfully!");
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RestExecutionException(e);
        }
    }


}

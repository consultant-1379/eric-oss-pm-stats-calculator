/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLContext;

import com.ericsson.oss.air.pm.stats.common.rest.metrics.HttpPoolMetricReporter;
import com.ericsson.oss.air.pm.stats.common.rest.metrics.HttpPoolMetrics;
import com.ericsson.oss.air.pm.stats.common.rest.metrics.HttpPoolType;
import com.ericsson.oss.air.pm.stats.common.rest.ssl.SslContextInitializer;
import com.ericsson.oss.air.pm.stats.common.rest.ssl.SslContextInterface;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;

/**
 * Class used to execute REST requests.
 */
public class RestExecutor implements Serializable {

    private static final long serialVersionUID = 4684046984427548289L;
    private static final HttpPoolMetricReporter HTTP_POOL_METRIC_REPORTER = HttpPoolMetricReporter.getInstance();
    private static ConcurrentHashMap<String, RestConnection> httpClientKeyedByHost;
    private static RestConnection unsecuredRestConnection;

    static {
        createHttpClient();
        createHttpSecuredClientCache();
    }

    static void createHttpClient() {
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        final PoolingHttpClientConnectionManager unsecuredConnectionManager = new RestConnectionPool().getUnsecuredConnectionManager();
        httpClientBuilder.setConnectionManager(unsecuredConnectionManager)
                         .setConnectionManagerShared(true);
        unsecuredRestConnection = new RestConnection(httpClientBuilder.build(), unsecuredConnectionManager);
    }

    static void createHttpSecuredClientCache() {
        httpClientKeyedByHost = new ConcurrentHashMap<>();
    }

    private static RestConnection createSslHttpClient(final SSLContext sslContext) {
        final SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslContext);
        final Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE)
                                                                 .register("https", factory)
                                                                 .build();
        final RequestConfig requestConfig = RequestConfig.custom()
                                                         .setConnectionRequestTimeout(30000)
                                                         .build();
        final PoolingHttpClientConnectionManager secureConnectionManager = new RestConnectionPool().getSecureConnectionManager(socketFactoryRegistry);
        final CookieStore cookieStore = new BasicCookieStore();

        final HttpClient httpClient = HttpClientBuilder.create()
                                                       .setDefaultCookieStore(cookieStore)
                                                       .setConnectionManager(secureConnectionManager)
                                                       .setDefaultRequestConfig(requestConfig)
                                                       .build();
        return new RestConnection(httpClient, secureConnectionManager);
    }

    /**
     * Executes the provided {@link HttpGet} REST request.
     *
     * @param httpGetRequest
     *            the {@link HttpGet} REST request
     * @return the {@link RestResponse} from the REST request
     * @throws IOException
     *             thrown if there is any issue executing the REST request
     */
    public RestResponse<String> sendGetRequest(final HttpGet httpGetRequest) throws IOException {
        final HttpResponse response = unsecuredRestConnection.getHttpclient().execute(httpGetRequest);
        logMetrics(unsecuredRestConnection.getHttpClientConnectionManager(), HttpPoolType.UNSECURE);
        return RestResponseBasic.create(response, httpGetRequest.getURI());
    }

    /**
     * Executes the provided {@link HttpPost} REST request.
     *
     * @param httpPostRequest
     *            the {@link HttpPost} REST request
     * @return the {@link RestResponse} from the REST request
     * @throws IOException
     *             thrown if there is any issue executing the REST request
     */
    public RestResponse<String> sendPostRequest(final HttpPost httpPostRequest) throws IOException {
        final HttpResponse response = unsecuredRestConnection.getHttpclient().execute(httpPostRequest);
        logMetrics(unsecuredRestConnection.getHttpClientConnectionManager(), HttpPoolType.UNSECURE);
        return RestResponseBasic.create(response, httpPostRequest.getURI());
    }

    /**
     * Executes the provided {@link HttpPut} REST request.
     *
     * @param httpPutRequest
     *            the {@link HttpPut} REST request
     * @return the {@link RestResponse} from the REST request
     * @throws IOException
     *             thrown if there is any issue executing the REST request
     */
    public RestResponse<String> sendPutRequest(final HttpPut httpPutRequest) throws IOException {
        final HttpResponse response = unsecuredRestConnection.getHttpclient().execute(httpPutRequest);
        logMetrics(unsecuredRestConnection.getHttpClientConnectionManager(), HttpPoolType.UNSECURE);
        return RestResponseBasic.create(response, httpPutRequest.getURI());
    }

    /**
     * Executes the provided {@link HttpGet} REST request with SSL support.
     *
     * @param httpGetRequest
     *            the {@link HttpGet} REST request
     * @param sslPort
     *            the {@link Integer} https port. If it is null, the default HTTPS port (443) is used
     * @return the {@link RestResponse} from the HTTPS REST request
     * @throws IOException
     *             thrown if there is any issue executing the REST request
     */
    public RestResponse<String> sendGetRequestWithSsl(final HttpGet httpGetRequest, final Integer sslPort) throws IOException {
        final HttpClient sslHttpClient = getSslRestConnection(httpGetRequest.getURI(), sslPort).getHttpclient();
        final HttpResponse response = sslHttpClient.execute(httpGetRequest);
        logMetrics(httpClientKeyedByHost.get(httpGetRequest.getURI().getHost()).getHttpClientConnectionManager(), HttpPoolType.SECURE);
        return RestResponseBasic.create(response, httpGetRequest.getURI());
    }

    private static RestConnection getSslRestConnection(final URI hostUri, final Integer sslPort){
        return httpClientKeyedByHost.computeIfAbsent(hostUri.getHost(), k -> {
            try {
                return createSslHttpClient(getSslContext(hostUri, sslPort));
            }catch (IOException e){
                throw new UncheckedIOException(e);
            }
        });
    }

    private static void logMetrics(final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager, final HttpPoolType httpPoolType) {
        final PoolStats poolStats = poolingHttpClientConnectionManager.getTotalStats();
        HTTP_POOL_METRIC_REPORTER.inc(httpPoolType, HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT, 1);
        HTTP_POOL_METRIC_REPORTER.inc(httpPoolType, HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT, poolStats.getPending());
    }

    private static SSLContext getSslContext(final URI hostUri, final Integer sslPort) throws IOException {
        final SslContextInterface sslContextInterface = new SslContextInitializer();
        return sslContextInterface.createSslContext(hostUri, sslPort);

    }
}

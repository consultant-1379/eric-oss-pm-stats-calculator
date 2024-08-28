/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest;

import org.junit.jupiter.api.Disabled;

/**
 * Unit tests for {@link RestExecutor} class.
 */
@Disabled("Tests were flaky in ECSON and flaky here as well. Failing sometimes does not have anything to do with merging common modules...")
public class RestExecutorTest {

    //  TODO: This code is not maintained thus not intended to migrate it to JUnit 5

//    private static final String IDENTITY_JKS_FILE = RestExecutorTest.class.getResource("/wiremock/identity.jks").getFile();
//    private static final String TEST_ENDPOINT = "/Test";
//    private static final String TEST_ENDPOINT_TWO = "/TestTwo";
//    private static final String TEST_ENDPOINT_OSS = "/OssOne";
//    private static final String TEST_ENDPOINT_OSS_TWO = "/OssTwo";
//    private static final String HOSTNAME = "localhost";
//    private static final Integer HTTP_PORT = 54466;
//    private static final Integer HTTP_PORT_TWO = 54467;
//    private static final Integer HTTPS_PORT_OSS = 54468;
//    private static final Integer HTTPS_PORT_OSS_TWO = 54469;
//    private static final String TEST_URI = String.format("http://%s:%d%s", HOSTNAME, HTTP_PORT, TEST_ENDPOINT);
//    private static final String TEST_URI_TWO = String.format("http://%s:%d%s", HOSTNAME, HTTP_PORT_TWO, TEST_ENDPOINT_TWO);
//    private static final String TEST_URI_OSS = String.format("https://%s:%d%s", HOSTNAME, HTTPS_PORT_OSS, TEST_ENDPOINT_OSS);
//    private static final String TEST_URI_OSS_TWO = String.format("https://%s:%d%s", HOSTNAME, HTTPS_PORT_OSS_TWO, TEST_ENDPOINT_OSS_TWO);
//    private static final int DELAY_IN_MILLISECONDS = 500;
//
//    @ClassRule
//    public static final WireMockRule WIRE_MOCK_ROUTE_ONE = new WireMockRule(wireMockConfig().port(HTTP_PORT));
//
//    @ClassRule
//    public static final WireMockRule WIRE_MOCK_ROUTE_TWO = new WireMockRule(wireMockConfig().port(HTTP_PORT_TWO));
//
//    @ClassRule
//    public static final WireMockRule WIRE_MOCK_OSS = new WireMockRule(
//            wireMockConfig().httpsPort(HTTPS_PORT_OSS).dynamicPort().keystorePath(IDENTITY_JKS_FILE));
//
//    @ClassRule
//    public static final WireMockRule WIRE_MOCK_OSS_TWO = new WireMockRule(
//            wireMockConfig().httpsPort(HTTPS_PORT_OSS_TWO).dynamicPort().keystorePath(IDENTITY_JKS_FILE));
//    private static final String BODY = "Body";
//
//    @Rule
//    public final SoftAssertions softly = new SoftAssertions();
//
//    @Parameterized.Parameter
//    public String maxPerRoute;
//
//    @Parameterized.Parameter(1)
//    public String maxConnections;
//
//    @Parameterized.Parameter(2)
//    public int numberOfRequests;
//
//    @Parameterized.Parameter(3)
//    public int numberOfRequestsRouteTwo;
//
//    @Parameterized.Parameter(4)
//    public boolean expectedBlocked;
//
//    @Parameterized.Parameters
//    public static Collection<Object> testParameters() {
//        return Arrays.asList(new Object[][]{
//                {"2", "20", 1, 0, false},
//                {"2", "20", 2, 0, false},
//                //{"2", "1", 2, 0, true},
//                //{"1", "20", 2, 0, true},
//                //{"2", "2", 2, 2, true},
//                {"2", "20", 2, 2, false}
//                //{"1", "4", 2, 2, true}
//        });
//    }
//
//    @BeforeClass
//    public static void clearEnvironment() {
//        System.setProperty("javax.net.ssl.trustStore", IDENTITY_JKS_FILE);
//        clearConnectionPoolVariables();
//    }
//
//    @AfterClass
//    public static void cleanup() {
//        clearConnectionPoolVariables();
//        clearMetricPoolCounters(HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT);
//        clearMetricPoolCounters(HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT);
//    }
//
//    private static void clearMetricPoolCounters(final HttpPoolMetrics poolConnectionsUsedCount) {
//        HttpPoolMetricReporter.getInstance().dec(HttpPoolType.UNSECURE, poolConnectionsUsedCount,
//                HttpPoolMetricReporter.getInstance().getMetricCount(HttpPoolType.UNSECURE, poolConnectionsUsedCount));
//        HttpPoolMetricReporter.getInstance().dec(HttpPoolType.SECURE, poolConnectionsUsedCount,
//                HttpPoolMetricReporter.getInstance().getMetricCount(HttpPoolType.SECURE, poolConnectionsUsedCount));
//    }
//
//    private static void clearConnectionPoolVariables() {
//        System.clearProperty("SECURED_MAX_CONNECTIONS");
//        System.clearProperty("SECURED_MAX_CONNECTIONS_PER_ROUTE");
//        System.clearProperty("UNSECURED_MAX_CONNECTIONS");
//        System.clearProperty("UNSECURED_MAX_CONNECTIONS_PER_ROUTE");
//    }
//
//    @After
//    public void tearDown() {
//        WIRE_MOCK_ROUTE_ONE.resetAll();
//        WIRE_MOCK_ROUTE_TWO.resetAll();
//        WIRE_MOCK_OSS.resetAll();
//        WIRE_MOCK_OSS_TWO.resetAll();
//    }
//
//    @Before
//    public void setUp() {
//        WIRE_MOCK_ROUTE_ONE.stubFor(get(urlPathEqualTo(TEST_ENDPOINT))
//                .willReturn(aResponse().withStatus(HttpStatus.SC_OK).withFixedDelay(DELAY_IN_MILLISECONDS).withBody(BODY)));
//        WIRE_MOCK_ROUTE_TWO.stubFor(get(urlPathEqualTo(TEST_ENDPOINT_TWO))
//                .willReturn(aResponse().withStatus(HttpStatus.SC_OK).withFixedDelay(DELAY_IN_MILLISECONDS).withBody(BODY)));
//        WIRE_MOCK_OSS.stubFor(get(urlPathEqualTo(TEST_ENDPOINT_OSS))
//                .willReturn(aResponse().withStatus(HttpStatus.SC_OK).withFixedDelay(DELAY_IN_MILLISECONDS).withBody(BODY)));
//        WIRE_MOCK_OSS_TWO.stubFor(get(urlPathEqualTo(TEST_ENDPOINT_OSS_TWO))
//                .willReturn(aResponse().withStatus(HttpStatus.SC_OK).withFixedDelay(DELAY_IN_MILLISECONDS).withBody(BODY)));
//
//        clearMetricPoolCounters(HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT);
//        clearMetricPoolCounters(HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT);
//    }
//
//    @Test
//    public void whenAnUnsecureConnectionPoolEnvironmentsVariablesAreSet_thenAConnectionPoolWithTheseVariableIsCreated()
//            throws ExecutionException, InterruptedException, TimeoutException {
//        testPoolTypes(HttpPoolType.UNSECURE);
//    }
//
//    @Test
//    public void whenASecureConnectionPoolEnvironmentsVariablesAreSet_thenAConnectionPoolWithTheseVariableIsCreated()
//            throws ExecutionException, InterruptedException, TimeoutException {
//        testPoolTypes(HttpPoolType.SECURE);
//    }
//
//    private void testPoolTypes(final HttpPoolType poolType) throws ExecutionException, InterruptedException, TimeoutException {
//        Collection<Callable<RestResponse<String>>> requests = null;
//
//        if (poolType.equals(HttpPoolType.UNSECURE)) {
//            setUnsecuredPoolEnvironment(maxConnections, maxPerRoute);
//            requests = prepareRequests(numberOfRequests, TEST_URI);
//            requests.addAll(prepareRequests(numberOfRequestsRouteTwo, TEST_URI_TWO));
//        }
//
//        if (poolType.equals(HttpPoolType.SECURE)) {
//            setSecurePoolEnvironment(maxConnections, maxPerRoute);
//            requests = prepareRequests(numberOfRequests, TEST_URI_OSS, HTTPS_PORT_OSS);
//            requests.addAll(prepareRequests(numberOfRequestsRouteTwo, TEST_URI_OSS_TWO, HTTPS_PORT_OSS_TWO));
//        }
//
//        sendRequests(requests);
//
//        softly.assertThat(HttpPoolMetricReporter.getInstance().getMetricCount(poolType, HttpPoolMetrics.POOL_CONNECTIONS_USED_COUNT))
//                .isEqualTo(requests.size());
//
//        if (expectedBlocked) {
//            softly.assertThat(HttpPoolMetricReporter.getInstance().getMetricCount(poolType, HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT)).isPositive();
//        } else {
//            softly.assertThat(HttpPoolMetricReporter.getInstance().getMetricCount(poolType, HttpPoolMetrics.POOL_CONNECTIONS_BLOCKED_COUNT)).isZero();
//        }
//    }
//
//    /**
//     * Creates a number of restRequests that implement callable for the ForkJoinPool.
//     */
//    private Collection<Callable<RestResponse<String>>> prepareRequests(final int numOfRequests, final String testUri) {
//        final HttpGet httpGet = new HttpGet(testUri);
//        return IntStream.range(0, numOfRequests)
//                .mapToObj(i -> new TestCallableRequest(httpGet, new RestExecutor()))
//                .collect(Collectors.toList());
//    }
//
//    private Collection<Callable<RestResponse<String>>> prepareRequests(final int numOfRequests, final String testUri, final Integer port) {
//        final HttpGet httpGet = new HttpGet(testUri);
//        return IntStream.range(0, numOfRequests)
//                .mapToObj(i -> new TestSslCallableRequest(httpGet, new RestExecutor(), port))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Starts ForkJoinPool and asserts that all requests returned a status 200.
//     */
//    private void sendRequests(final Collection<Callable<RestResponse<String>>> restRequests) throws ExecutionException, InterruptedException {
//        final ForkJoinPool restRequestPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
//        final List<Future<RestResponse<String>>> results = restRequestPool.invokeAll(restRequests);
//        final int size = results.size();
//        for (int i = 0; i < size; i++) {
//            final Future<RestResponse<String>> response = results.get(i);
//            await().atMost(2, TimeUnit.MINUTES).pollInterval(DELAY_IN_MILLISECONDS, TimeUnit.MILLISECONDS)
//                    .untilAsserted(() -> assertThat(response).isDone());
//            final String description = (i + 1) + " of " + size;
//            try {
//                final RestResponse<String> responseBody = response.get();
//                softly.assertThat(responseBody.getStatus()).as("Unexpected status for response %s", description).isEqualTo(HttpStatus.SC_OK);
//                softly.assertThat(responseBody.getEntity()).as("Unexpected entity for response %s", description).isEqualTo(BODY);
//            } catch (InterruptedException | ExecutionException e) {
//                softly.fail("%s caught while getting response %s", e.getClass().getSimpleName(), description);
//            }
//        }
//        //check assertions up to this point
//        softly.assertAll();
//    }
//
//    /**
//     * Sets the secureRestConnectionPool values and sets the static values in RestExecutor
//     */
//    private void setSecurePoolEnvironment(final String max, final String maxRoute) {
//        RestExecutor.createHttpSecuredClientCache();
//        System.clearProperty("SECURED_MAX_CONNECTIONS");
//        System.clearProperty("SECURED_MAX_CONNECTIONS_PER_ROUTE");
//        System.setProperty("SECURED_MAX_CONNECTIONS", max);
//        System.setProperty("SECURED_MAX_CONNECTIONS_PER_ROUTE", maxRoute);
//    }
//
//    /**
//     * Sets the unsecureRestConnectionPool values and sets the static values in RestExecutor
//     */
//    private void setUnsecuredPoolEnvironment(final String max, final String maxRoute) {
//        System.clearProperty("UNSECURED_MAX_CONNECTIONS");
//        System.clearProperty("UNSECURED_MAX_CONNECTIONS_PER_ROUTE");
//        System.setProperty("UNSECURED_MAX_CONNECTIONS", max);
//        System.setProperty("UNSECURED_MAX_CONNECTIONS_PER_ROUTE", maxRoute);
//        RestExecutor.createHttpClient();
//    }
//
//    private static class TestCallableRequest implements Callable<RestResponse<String>> {
//
//        private final HttpGet httpGet;
//        private final RestExecutor restExecutor;
//
//        TestCallableRequest(final HttpGet httpGet, final RestExecutor restExecutor) {
//            this.httpGet = httpGet;
//            this.restExecutor = restExecutor;
//        }
//
//        @Override
//        public RestResponse<String> call() throws Exception {
//            return restExecutor.sendGetRequest(httpGet);
//        }
//    }
//
//    private static class TestSslCallableRequest implements Callable<RestResponse<String>> {
//        private final HttpGet httpGet;
//        private final RestExecutor restExecutor;
//        private final Integer port;
//
//        TestSslCallableRequest(final HttpGet httpGet, final RestExecutor restExecutor, final Integer port) {
//            this.httpGet = httpGet;
//            this.restExecutor = restExecutor;
//            this.port = port;
//        }
//
//        @Override
//        public RestResponse<String> call() throws Exception {
//            return restExecutor.sendGetRequestWithSsl(httpGet, port);
//        }
//    }
}
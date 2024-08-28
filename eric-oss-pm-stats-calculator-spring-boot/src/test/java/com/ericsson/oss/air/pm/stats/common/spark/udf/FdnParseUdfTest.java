/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.udf;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link FdnParseUdf}.
 */
class FdnParseUdfTest {

    final FdnParseUdf objectUnderTest = new FdnParseUdf();
    private static final String VALID_NODE_FDN = "SubNetwork=ERBS-SUB_16,MeContext=TEST_3,ManagedElement=T0717";
    private static final String VALID_MO_FDN = "ManagedElement=NR0FANodeBRadio00016,GNBCUUPFunction=4,X2UTermination=397";
    private static final String VALID_MO_FDN_EXTENDED = "ManagedElement=NR0F0NodeBRadio00016,Hier2=ABC,GNBCUUPFunction=4,Hier4=XYZ,X2UTermination=397";
    private static final String VALID_NODE_FILTER = "MeContext";
    private static final String VALID_MO_FILTER = "GNBCUUPFunction";
    private static final String ERROR_RESPONSE = "FDN_PARSE_ERROR";

    @ParameterizedTest(name = "[{index}] FDN: ''{0}'', filter: ''{1}''")
    @MethodSource("blankOrImpossibleInputProvider")
    @DisplayName("Blank (null, whitespace) inputs or overly long filter -> error result")
    void whenInputIsBlankOrImpossible_shouldReturnError(final String fdn, final String filterKey) {
        final String errorResult = objectUnderTest.call(fdn, filterKey);

        assertThat(errorResult).isEqualTo(ERROR_RESPONSE);
    }

    static Stream<Arguments> blankOrImpossibleInputProvider() {
        final String impossibleFilterByLength = "x".repeat(VALID_MO_FDN.length());
        return Stream.of(
                Arguments.of(VALID_NODE_FDN, null),
                Arguments.of(VALID_MO_FDN, ""),
                Arguments.of(VALID_MO_FDN, "  "),
                Arguments.of(null, VALID_NODE_FILTER),
                Arguments.of("", VALID_NODE_FILTER),
                Arguments.of("  ", VALID_NODE_FILTER),
                Arguments.of(VALID_MO_FDN, impossibleFilterByLength)
        );
    }

    @Test
    @DisplayName("Invalid filter (non-existing key) -> error result")
    void whenFilterIsInvalidByContent_shouldReturnError() {
        final String invalidFilterByContent = "MORSE";
        final String validNodeForSonar = VALID_NODE_FDN;
        assertThat(validNodeForSonar).doesNotContain(invalidFilterByContent);

        final String errorResult = objectUnderTest.call(validNodeForSonar, invalidFilterByContent);

        assertThat(errorResult).isEqualTo(ERROR_RESPONSE);
    }

    @ParameterizedTest
    @MethodSource("malformedFdnProvider")
    @DisplayName("Malformed FDN (missing or incorrect symbols) -> error result")
    void whenFdnIsMalformed_shouldReturnError(final String fdn) {
        final String errorResult = objectUnderTest.call(fdn, VALID_NODE_FILTER);

        assertThat(errorResult).isEqualTo(ERROR_RESPONSE);
    }

    static Stream<String> malformedFdnProvider() {
        return Stream.of(
                VALID_NODE_FDN.replace(',', ';'),
                VALID_NODE_FDN.replace(",", ",  "),
                VALID_NODE_FDN.replace('=', ':'),
                VALID_NODE_FDN.replace('=', '\u0000'),
                "x".repeat(100)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"subnetwork", "SubNet", "S", "SubNetwork=", ",SubNetwork", "SubNetwork,",
            ",SubNetwork=", " SubNetwork", "SubNetwork ", " SubNetwork ",
            "mecontext", "MeCont", "M", "MeContext=", ",MeContext", "MeContext,",
            ",MeContext=", " MeContext", "MeContext ", " MeContext ",
            "managedelement", "Managed", "E", "ManagedElement=", ",ManagedElement",
            "ManagedElement,", ",ManagedElement=", " ManagedElement", "ManagedElement ", " ManagedElement "})
    @DisplayName("Typo in filter: -> error result")
    void whenFilterContainsTypographicalError_shouldReturnError(final String filterKey) {
        final String errorResult = objectUnderTest.call(VALID_NODE_FDN, filterKey);

        assertThat(errorResult).isEqualTo(ERROR_RESPONSE);
    }

    @Test
    @DisplayName("Valid inputs -> correct result")
    void whenFilterIsLogical_shouldReturnCorrectSegment() {
        final String moFdnCorrectSegment = "ManagedElement=NR0FANodeBRadio00016,GNBCUUPFunction=4";

        final String result = objectUnderTest.call(VALID_MO_FDN, VALID_MO_FILTER);

        assertThat(result).isEqualTo(moFdnCorrectSegment);
    }

    @Test
    @DisplayName("More complexity in the hierarchy, valid inputs -> correct result")
    void whenHierarchyHasMoreLevels_shouldReturnCorrectSegment() {
        final String moFdnCorrectSegment = "ManagedElement=NR0F0NodeBRadio00016,Hier2=ABC,GNBCUUPFunction=4";

        final String result = objectUnderTest.call(VALID_MO_FDN_EXTENDED, VALID_MO_FILTER);

        assertThat(result).isEqualTo(moFdnCorrectSegment);
    }

    @Test
    @DisplayName("Root level filtered -> root level returned")
    void whenFilterIsRootHierarchy_shouldReturnRoot() {
        final String moFdnRootFilter = "ManagedElement";
        final String moFdnRootSegment = "ManagedElement=NR0FANodeBRadio00016";

        final String result = objectUnderTest.call(VALID_MO_FDN, moFdnRootFilter);

        assertThat(result).isEqualTo(moFdnRootSegment);
    }

    @Test
    @DisplayName("Bottom level filtered -> full FDN returned")
    void whenFilterIsBottomHierarchy_shouldReturnFullFdn() {
        final String moFdnBottomFilter = "X2UTermination";

        final String result = objectUnderTest.call(VALID_MO_FDN, moFdnBottomFilter);

        assertThat(result).isEqualTo(VALID_MO_FDN);
    }
}

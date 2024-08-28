/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Provides test data needed to perform KPI calculation testing.
 */
final class TestData {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.UK)
            .withZone(ZoneOffset.UTC);

    private static final String UTC_TIME = "2019-09-05 16:00:00";
    private static final String LOCAL_TIME = "2019-09-05 12:00:00";

    private static final String AGGREGATION_BEGIN_TIME = "2019-09-05 16:00:00";
    private static final String AGGREGATION_END_TIME = "2019-09-05 17:00:00";
    private static final String MIDNIGHT_DAY_ONE = "2019-09-05 00:00:00";
    private static final String MIDNIGHT_DAY_TWO = "2019-09-06 00:00:00";
    private static final String MIDNIGHT_DAY_THREE = "2019-09-07 00:00:00";
    private static final String ONE_AM_DAY_TWO = "2019-09-06 01:00:00";
    private static final String TWO_AM_DAY_TWO = "2019-09-06 02:00:00";
    private static final Timestamp utcTime = createTimestampFromString(UTC_TIME);

    private static final String MULTI_ROP_UTC_TIME_1 = "2019-09-06 01:15:00";
    private static final String MULTI_ROP_UTC_TIME_2 = "2019-09-06 01:00:00";
    private static final String MULTI_ROP_UTC_TIME_3 = "2019-09-05 19:00:00";
    private static final String MULTI_ROP_AGGREGATION_BEGIN_TIME_2 = "2019-09-06 01:00:00";
    private static final String MULTI_ROP_AGGREGATION_BEGIN_TIME_3 = "2019-09-05 19:00:00";

    private static final String MULTI_ROP_AGGREGATION_END_TIME_2 = "2019-09-06 02:00:00";
    private static final String MULTI_ROP_AGGREGATION_END_TIME_3 = "2019-09-05 20:00:00";

    private static final Timestamp utcTimeRop1 = createTimestampFromString(MULTI_ROP_UTC_TIME_1);
    private static final Timestamp utcTimeRop2 = createTimestampFromString(MULTI_ROP_UTC_TIME_2);
    private static final Timestamp utcTimeRop3 = createTimestampFromString(MULTI_ROP_UTC_TIME_3);

    private static final int[] integerArrayValues1 = new int[] { 1, 1, 1, 1, 1 };
    private static final int[] integerArrayValues2 = new int[] { 2, 2, 2, 2, 2 };
    private static final int[] integerArrayValues3 = new int[] { 1 };
    private static final int[] integerArrayValues4 = new int[] { 1, 3, 2, -1 };
    private static final int[] integerArrayValues5 = new int[] { 1, 3, 2, -1, 1 };
    private static final double[] doubleArrayValues1 = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0 };
    private static final double[] doubleArrayValues2 = new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 };
    private static final double[] doubleArrayValues3 = new double[] { 1.0, 3.7, 2.5, 0.8 };
    private static final double[] doubleArrayValues4 = new double[] { 4.2, 1.2 };
    private static final double[] doubleArrayValues5 = new double[] { 1.0, 3.7, 2.5, 0.8, 4.2, 1.2 };

    private TestData() {

    }

    static Object[][] getDimTable0Data() {
        return new Object[][] {
                { 0L, "STRING_VALUE_0", 2, 10000 },
                { 1L, "STRING_VALUE_0", 2, 10000 },
                { 2L, "STRING_VALUE_0", 2, 10000 },
                { 3L, "STRING_VALUE_0", 2, 10000 }
        };
    }

    static Object[][] getDimTable1Data() {
        return new Object[][] {
                { 500L, 100L, 52L, false },
                { 501L, 79L, 129L, false },
                { 502L, 89L, 138L, false },
                { 503L, 72L, 110L, false }
        };
    }

    static Object[][] getDimTable2Data() {
        return new Object[][] {
                { 1L, 1L },
                { 2L, 1L },
                { 3L, 1L },
                { 19L, 4L },
                { 4L, 4L },
                { 9L, 4L },
                { 5L, 2L },
                { 5L, 3L },
                { 6L, 2L },
                { 6L, 3L }
        };
    }

    static Object[][] getFactTable0Data() {
        return new Object[][] {
                { 0L, utcTime, 1, new Timestamp(0) },
                { 1L, utcTime, 1, new Timestamp(0) },
                { 2L, utcTimeRop2, 1, new Timestamp(0) },
                { 3L, utcTimeRop1, 1, new Timestamp(0) },
        };
    }

    static Object[][] getFactTable1Data() {
        return new Object[][] {
                { 500L, 100L, 52L, utcTime, 2, 0.5 },
                { 501L, 79L, 129L, utcTime, 2, 0.5 },
                { 502L, 89L, 138L, utcTimeRop2, 2, 0.5 },
                { 503L, 72L, 110L, utcTimeRop3, 2, 0.5 }
        };
    }

    static Object[][] getFactTable2Data() {
        return new Object[][] {
                { 0L, utcTime, integerArrayValues1, 1, 0.2, doubleArrayValues1, integerArrayValues3, integerArrayValues4,
                        doubleArrayValues4, doubleArrayValues3},
                { 1L, utcTime, integerArrayValues1, 1, 0.2, doubleArrayValues1, integerArrayValues3, integerArrayValues4,
                        doubleArrayValues4, doubleArrayValues3},
                { 2L, utcTimeRop2, integerArrayValues1, 1, 0.2, doubleArrayValues1, integerArrayValues3, integerArrayValues4,
                        doubleArrayValues4, doubleArrayValues3},
                { 3L, utcTimeRop1, integerArrayValues1, 1, 0.2, doubleArrayValues1, integerArrayValues3, integerArrayValues4,
                        doubleArrayValues4, doubleArrayValues3},
        };
    }

    static Object[][] getFactTable3Data() {
        return new Object[][] {
                { 500L, 100L, 52L, utcTime, 1, integerArrayValues1 },
                { 501L, 79L, 129L, utcTime, 1, integerArrayValues1 },
                { 502L, 89L, 138L, utcTimeRop2, 1, integerArrayValues1 },
                { 503L, 72L, 110L, utcTimeRop3, 1, integerArrayValues1 }
        };
    }

    static Object[][] getAggObject1_1440InputData() {
        return new Object[][] {
                { 1L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_ONE),
                        15, 25, integerArrayValues1, 40, 45, 50, 5L, new Timestamp(10000),
                        doubleArrayValues1, "STRING_VALUE_0"},
                { 2L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_ONE),
                        15, 25, integerArrayValues1, 40, 45, 50, 5L, new Timestamp(10000),
                        doubleArrayValues1, "STRING_VALUE_0" },
                { 3L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_ONE),
                        15, 25, integerArrayValues1, 40, 45, 50, 5L, new Timestamp(10000),
                        doubleArrayValues1, "STRING_VALUE_0" },
                { 4L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_ONE),
                        15, 25, integerArrayValues1, 40, 45, 50, 5L, new Timestamp(10000),
                        doubleArrayValues1, "STRING_VALUE_0" },
        };
    }

    static Object[][] getSimpleAggObject_1440InputData() {
        return new Object[][] {
                { 1L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_ONE), 40, 45, 0.0, 0, integerArrayValues1, integerArrayValues1},
                { 2L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_ONE), 40, 45, 0.0, 0, integerArrayValues1, integerArrayValues1},
                { 3L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_ONE), 40, 45, 0.0, 0, integerArrayValues1, integerArrayValues1},
                { 4L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_ONE), 40, 45, 0.0, 0, integerArrayValues1, integerArrayValues1}
        };
    }

    static Object[][] getAggObject2_1440InputData() {
        return new Object[][] {
                { 501L, 79L, 129L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                        false, 1, 1.25 },
                { 502L, 89L, 138L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                        false, 1, 1.25 },
                { 502L, 89L, 138L, createTimestampFromString(MIDNIGHT_DAY_TWO), createTimestampFromString(MIDNIGHT_DAY_THREE),
                        false, 2, 1.0 },
                { 503L, 72L, 110L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                        false, 1, 1.25 },
        };
    }

    static Object[][] getSimpleAggObject2_1440InputData() {
        return new Object[][] {
                { 501L, 79L, 129L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                        1.5 },
                { 502L, 89L, 138L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                        1.5 },
                { 503L, 72L, 110L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                        1.5 },
        };
    }

    static Object[][] getAggObject3_1440InputData() {
        return new Object[][] {
                { 1L, 1L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_ONE), 1 }
        };
    }

    static Object[][] getAggObject1_60InputData() {
        return new Object[][] {
                { 1L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(LOCAL_TIME), 1, 2, 0.25, 0.5, integerArrayValues1,
                        1.0, 1.0, integerArrayValues2, doubleArrayValues5 },
                { 2L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(LOCAL_TIME), 1, 2, 0.25, 0.5, integerArrayValues1,
                        1.0, 1.0, integerArrayValues2, doubleArrayValues5 },
                { 3L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(LOCAL_TIME), 1, 2, 0.25, 0.5, integerArrayValues1,
                        1.0, 1.0, integerArrayValues2, doubleArrayValues5 },
                { 4L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(LOCAL_TIME), 1, 2, 0.25, 0.5, integerArrayValues1,
                        1.0, 1.0, integerArrayValues2, doubleArrayValues5 },
        };
    }

    static Object[][] getAggObject2_60InputData() {
        return new Object[][] {
                { 501L, 79L, 129L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(LOCAL_TIME), 0.0 },
                { 502L, 89L, 138L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(LOCAL_TIME), 0.0 },
                { 503L, 72L, 110L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(LOCAL_TIME), 0.0 }
        };
    }

    /**
     * Issue with timezones in different environments: <a href="https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/IDUN/%5BIDUN-8821%5D+KPI+Service+Repo+migration%3A+Maven+projects#id-[IDUN8821]KPIServiceRepomigration:Mavenprojects-Timezone">Confluance</a>
     */
    static Object[][] getExpectedKpiAggObject1_1440Data() {

        return new Object[][] {
                {0L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                 4, 3, 3, doubleArrayValues2, 1, 1L, epochOffset(0), 2, integerArrayValues1, "STRING_VALUE_0"
                 },
                {1L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                 25, 3, 15, doubleArrayValues2, 41, 5L, epochOffset(0), 47, integerArrayValues2, "STRING_VALUE_0"
                 },
                {2L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                 25, 50, 15, doubleArrayValues1, 40, 5L, epochOffset(10), 45, integerArrayValues1, "STRING_VALUE_0"
                 },
                {2L, createTimestampFromString(MIDNIGHT_DAY_TWO), createTimestampFromString(MIDNIGHT_DAY_THREE),
                 4, 3, 3, doubleArrayValues2, 1, 1L, epochOffset(0), 2, integerArrayValues1, "STRING_VALUE_0"
                 },
                {3L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                 25, 50, 15, doubleArrayValues1, 40, 5L, epochOffset(10), 45, integerArrayValues1, "STRING_VALUE_0"
                 },
                {3L, createTimestampFromString(MIDNIGHT_DAY_TWO), createTimestampFromString(MIDNIGHT_DAY_THREE),
                        4, 3, 3, doubleArrayValues2, 1, 1L, epochOffset(0), 2, integerArrayValues1, "STRING_VALUE_0"
                 },
                {4L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                 25, 50, 15, doubleArrayValues1, 40, 5L, epochOffset(10), 45, integerArrayValues1, "STRING_VALUE_0"
                 } };
    }

    static Object[][] getExpectedSimpleKpiAggObject1_1440Data() {

        return new Object[][] {
                {0L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                        1,  0.0, 2, 1.0, integerArrayValues1, integerArrayValues1},
                {1L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                        41, 0.0, 47, 1.0, integerArrayValues1, integerArrayValues1},
                {2L,  createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                        40, 0.0, 45, 0.0, integerArrayValues1, integerArrayValues1},
                {2L,  createTimestampFromString(MIDNIGHT_DAY_TWO), createTimestampFromString(MIDNIGHT_DAY_THREE),
                        1, 0.0, 2, 1.0, integerArrayValues1, integerArrayValues1},
                {3L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                        40, 0.0, 45, 0.0, integerArrayValues1, integerArrayValues1},
                {3L, createTimestampFromString(MIDNIGHT_DAY_TWO), createTimestampFromString(MIDNIGHT_DAY_THREE),
                        1, 0.0, 2, 1.0, integerArrayValues1, integerArrayValues1},
                {4L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO),
                        40, 0.0, 45, 0.0, integerArrayValues1, integerArrayValues1},
        };
    }

    static Object[][] getExpectedKpiAggObject2_1440Data() {
        return new Object[][] {
                { 500L, 100L, 52L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO), 1.0, false, 2,
                },
                { 501L, 79L, 129L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO), 2.25, false, 3,
                },
                { 502L, 89L, 138L, createTimestampFromString(MIDNIGHT_DAY_TWO), createTimestampFromString(MIDNIGHT_DAY_THREE), 2.0, false, 4,
                },
                { 502L, 89L, 138L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO), 1.25, false, 1,
                },
                { 503L, 72L, 110L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO), 2.25, false, 3,
                },
        };
    }

    static Object[][] getExpectedSimpleKpiAggObject2_1440Data() {
        return new Object[][] {
                { 500L, 100L, 52L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO), 0.5
                },
                { 501L, 79L, 129L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO), 2.0
                },
                { 502L, 89L, 138L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO), 1.5
                },
                { 502L, 89L, 138L, createTimestampFromString(MIDNIGHT_DAY_TWO), createTimestampFromString(MIDNIGHT_DAY_THREE), 0.5
                },
                { 503L, 72L, 110L, createTimestampFromString(MIDNIGHT_DAY_ONE), createTimestampFromString(MIDNIGHT_DAY_TWO), 2.0
                },
        };
    }

    static Object[][] getExpectedKpiAggObject1_60Data() {
        return new Object[][] {
                {       0L,
                        createTimestampFromString(AGGREGATION_BEGIN_TIME),
                        1.0,
                        integerArrayValues5,
                        doubleArrayValues5,
                        integerArrayValues1,
                        1,
                        1,
                        0.1,
                        1.0,
                        0.9,
                        createTimestampFromString(AGGREGATION_END_TIME)
                },
                {       1L,
                        createTimestampFromString(AGGREGATION_BEGIN_TIME),
                        1.0,
                        integerArrayValues5,
                        doubleArrayValues5,
                        integerArrayValues1,
                        1,
                        1,
                        0.1,
                        1.0,
                        0.9,
                        createTimestampFromString(AGGREGATION_END_TIME)
                },
                {       2L,
                        createTimestampFromString(ONE_AM_DAY_TWO),
                        1.0,
                        integerArrayValues5,
                        doubleArrayValues5,
                        integerArrayValues1,
                        1,
                        1,
                        0.1,
                        1.0,
                        0.9,
                        createTimestampFromString(TWO_AM_DAY_TWO)
                },
                {       3L,
                        createTimestampFromString(ONE_AM_DAY_TWO),
                        1.0,
                        integerArrayValues5,
                        doubleArrayValues5,
                        integerArrayValues1,
                        1,
                        1,
                        0.1,
                        1.0,
                        0.9,
                        createTimestampFromString(TWO_AM_DAY_TWO),
                },
        };
    }

    static Object[][] getExpectedKpiAggObject2_60Data() {
        return new Object[][] {
                { 500L, 100L, 52L, createTimestampFromString(AGGREGATION_BEGIN_TIME), createTimestampFromString(AGGREGATION_END_TIME), null },
                { 501L, 79L, 129L, createTimestampFromString(AGGREGATION_BEGIN_TIME), createTimestampFromString(AGGREGATION_END_TIME), null },
                { 502L, 89L, 138L, createTimestampFromString(MULTI_ROP_AGGREGATION_BEGIN_TIME_2), createTimestampFromString(MULTI_ROP_AGGREGATION_END_TIME_2), null },
                { 503L, 72L, 110L, createTimestampFromString(MULTI_ROP_AGGREGATION_BEGIN_TIME_3), createTimestampFromString(MULTI_ROP_AGGREGATION_END_TIME_3), null }
        };
    }

    public static Timestamp createTimestampFromString(final String timestampAsString) {
        final LocalDateTime fromDate = LocalDateTime.parse(timestampAsString, DATE_TIME_FORMATTER);
        return Timestamp.valueOf(fromDate);
    }

    private static Timestamp epochOffset(final int seconds) {
        return Timestamp.valueOf(Instant.ofEpochMilli(Duration.ofSeconds(seconds)
                                                              .toMillis())
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime());
    }
}

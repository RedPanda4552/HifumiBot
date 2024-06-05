package io.github.redpanda4552.HifumiBot.util;

import java.time.Clock;
import java.time.OffsetDateTime;

public class TimeUtils {

    /**
     * Gets the current time, and removes 365 days giving roughly one year in the past.
     * Then round that down to the first day of whatever month it landed in,
     * finally shave off all hours minutes seconds and nanos. The result will always be
     * the first day of the month at midnight UTC.
     * @return
     */
    public static long getEpochSecondLastYear() {
        OffsetDateTime oneYearAgo = OffsetDateTime.now(Clock.systemUTC()).minusDays(365);
        OffsetDateTime adjustedToMonthStart = (oneYearAgo.getDayOfMonth() > 1 ? oneYearAgo.minusDays(oneYearAgo.getDayOfMonth() - 1) : oneYearAgo);
        OffsetDateTime toMidnight = adjustedToMonthStart.minusHours(adjustedToMonthStart.getHour()).minusMinutes(adjustedToMonthStart.getMinute()).minusSeconds(adjustedToMonthStart.getSecond()).minusNanos(adjustedToMonthStart.getNano());
        long epochSeconds = toMidnight.toEpochSecond();
        return epochSeconds;
    }
}

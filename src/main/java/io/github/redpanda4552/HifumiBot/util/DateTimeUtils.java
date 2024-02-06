package io.github.redpanda4552.HifumiBot.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateTimeUtils {

    public static OffsetDateTime longToOffsetDateTime(long l) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(l), ZoneId.of("Z"));
    }
}

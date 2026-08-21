/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.file.attribute.FileTimes;

public final class TimeUtils {
    static final long HUNDRED_NANOS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1L) / 100L;
    static final long WINDOWS_EPOCH_OFFSET = -116444736000000000L;

    @Deprecated
    public static boolean isUnixTime(FileTime time) {
        return FileTimes.isUnixTime(time);
    }

    @Deprecated
    public static boolean isUnixTime(long seconds) {
        return FileTimes.isUnixTime(seconds);
    }

    @Deprecated
    public static Date ntfsTimeToDate(long ntfsTime) {
        return FileTimes.ntfsTimeToDate(ntfsTime);
    }

    @Deprecated
    public static FileTime ntfsTimeToFileTime(long ntfsTime) {
        return FileTimes.ntfsTimeToFileTime(ntfsTime);
    }

    @Deprecated
    public static Date toDate(FileTime fileTime) {
        return FileTimes.toDate(fileTime);
    }

    @Deprecated
    public static FileTime toFileTime(Date date) {
        return FileTimes.toFileTime(date);
    }

    @Deprecated
    public static long toNtfsTime(Date date) {
        return FileTimes.toNtfsTime(date);
    }

    @Deprecated
    public static long toNtfsTime(FileTime fileTime) {
        return FileTimes.toNtfsTime(fileTime);
    }

    @Deprecated
    public static long toNtfsTime(long javaTime) {
        return FileTimes.toNtfsTime(javaTime);
    }

    public static long toUnixTime(FileTime fileTime) {
        return FileTimes.toUnixTime(fileTime);
    }

    public static FileTime truncateToHundredNanos(FileTime fileTime) {
        Instant instant = fileTime.toInstant();
        return FileTime.from(Instant.ofEpochSecond(instant.getEpochSecond(), instant.getNano() / 100 * 100));
    }

    @Deprecated
    public static FileTime unixTimeToFileTime(long time) {
        return FileTimes.fromUnixTime(time);
    }

    private TimeUtils() {
    }
}


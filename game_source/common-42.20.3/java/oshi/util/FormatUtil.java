/*
 * Decompiled with CFR 0.152.
 */
package oshi.util;

import java.math.BigInteger;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class FormatUtil {
    private static final long KIBI = 1024L;
    private static final long MEBI = 0x100000L;
    private static final long GIBI = 0x40000000L;
    private static final long TEBI = 0x10000000000L;
    private static final long PEBI = 0x4000000000000L;
    private static final long EXBI = 0x1000000000000000L;
    private static final long KILO = 1000L;
    private static final long MEGA = 1000000L;
    private static final long GIGA = 1000000000L;
    private static final long TERA = 1000000000000L;
    private static final long PETA = 1000000000000000L;
    private static final long EXA = 1000000000000000000L;
    private static final BigInteger TWOS_COMPLEMENT_REF = BigInteger.ONE.shiftLeft(64);
    public static final String HEX_ERROR = "0x%08X";

    private FormatUtil() {
    }

    public static String formatBytes(long bytes) {
        if (bytes == 1L) {
            return String.format(Locale.ROOT, "%d byte", bytes);
        }
        if (bytes < 1024L) {
            return String.format(Locale.ROOT, "%d bytes", bytes);
        }
        if (bytes < 0x100000L) {
            return FormatUtil.formatUnits(bytes, 1024L, "KiB");
        }
        if (bytes < 0x40000000L) {
            return FormatUtil.formatUnits(bytes, 0x100000L, "MiB");
        }
        if (bytes < 0x10000000000L) {
            return FormatUtil.formatUnits(bytes, 0x40000000L, "GiB");
        }
        if (bytes < 0x4000000000000L) {
            return FormatUtil.formatUnits(bytes, 0x10000000000L, "TiB");
        }
        if (bytes < 0x1000000000000000L) {
            return FormatUtil.formatUnits(bytes, 0x4000000000000L, "PiB");
        }
        return FormatUtil.formatUnits(bytes, 0x1000000000000000L, "EiB");
    }

    private static String formatUnits(long value, long prefix, String unit) {
        if (value % prefix == 0L) {
            return String.format(Locale.ROOT, "%d %s", value / prefix, unit);
        }
        return String.format(Locale.ROOT, "%.1f %s", (double)value / (double)prefix, unit);
    }

    public static String formatBytesDecimal(long bytes) {
        if (bytes == 1L) {
            return String.format(Locale.ROOT, "%d byte", bytes);
        }
        if (bytes < 1000L) {
            return String.format(Locale.ROOT, "%d bytes", bytes);
        }
        return FormatUtil.formatValue(bytes, "B");
    }

    public static String formatHertz(long hertz) {
        return FormatUtil.formatValue(hertz, "Hz");
    }

    public static String formatValue(long value, String unit) {
        if (value < 1000L) {
            return String.format(Locale.ROOT, "%d %s", value, unit).trim();
        }
        if (value < 1000000L) {
            return FormatUtil.formatUnits(value, 1000L, "K" + unit);
        }
        if (value < 1000000000L) {
            return FormatUtil.formatUnits(value, 1000000L, "M" + unit);
        }
        if (value < 1000000000000L) {
            return FormatUtil.formatUnits(value, 1000000000L, "G" + unit);
        }
        if (value < 1000000000000000L) {
            return FormatUtil.formatUnits(value, 1000000000000L, "T" + unit);
        }
        if (value < 1000000000000000000L) {
            return FormatUtil.formatUnits(value, 1000000000000000L, "P" + unit);
        }
        return FormatUtil.formatUnits(value, 1000000000000000000L, "E" + unit);
    }

    public static String formatElapsedSecs(long secs) {
        long eTime = secs;
        long days = TimeUnit.SECONDS.toDays(eTime);
        long hr = TimeUnit.SECONDS.toHours(eTime -= TimeUnit.DAYS.toSeconds(days));
        long min = TimeUnit.SECONDS.toMinutes(eTime -= TimeUnit.HOURS.toSeconds(hr));
        long sec = eTime -= TimeUnit.MINUTES.toSeconds(min);
        return String.format(Locale.ROOT, "%d days, %02d:%02d:%02d", days, hr, min, sec);
    }

    public static long getUnsignedInt(int x) {
        return (long)x & 0xFFFFFFFFL;
    }

    public static String toUnsignedString(int i) {
        if (i >= 0) {
            return Integer.toString(i);
        }
        return Long.toString(FormatUtil.getUnsignedInt(i));
    }

    public static String toUnsignedString(long l) {
        if (l >= 0L) {
            return Long.toString(l);
        }
        return BigInteger.valueOf(l).add(TWOS_COMPLEMENT_REF).toString();
    }

    public static String formatError(int errorCode) {
        return String.format(Locale.ROOT, HEX_ERROR, errorCode);
    }

    public static int roundToInt(double x) {
        return (int)Math.round(x);
    }
}


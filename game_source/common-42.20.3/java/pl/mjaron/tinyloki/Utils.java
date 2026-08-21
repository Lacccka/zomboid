/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
    private Utils() {
    }

    public static String stackTraceString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clampToInt(long value) {
        return (int)Utils.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static void escapeJsonString(StringBuilder b, CharSequence text) {
        int length = text.length();
        block10: for (int i = 0; i < length; ++i) {
            char c = text.charAt(i);
            switch (c) {
                case '\"': {
                    b.append("\\\"");
                    continue block10;
                }
                case '\\': {
                    b.append("\\\\");
                    continue block10;
                }
                case '/': {
                    b.append("\\/");
                    continue block10;
                }
                case '\b': {
                    b.append("\\b");
                    continue block10;
                }
                case '\f': {
                    b.append("\\f");
                    continue block10;
                }
                case '\n': {
                    b.append("\\n");
                    continue block10;
                }
                case '\r': {
                    b.append("\\r");
                    continue block10;
                }
                case '\t': {
                    b.append("\\t");
                    continue block10;
                }
                default: {
                    if (c > '\u001f') {
                        b.append(c);
                        continue block10;
                    }
                    b.append("\\u");
                    String hex = "000" + Integer.toHexString(c);
                    b.append(hex.substring(hex.length() - 4));
                }
            }
        }
    }

    public static boolean isAsciiCapitalLetter(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    public static boolean isAsciiLowercaseLetter(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    public static boolean isAsciiLetter(char ch) {
        return Utils.isAsciiCapitalLetter(ch) || Utils.isAsciiLowercaseLetter(ch);
    }

    public static boolean isAsciiDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public static boolean isAsciiLetterOrDigit(char ch) {
        return Utils.isAsciiLetter(ch) || Utils.isAsciiDigit(ch);
    }

    public static class MonotonicClock {
        public static long MILLISECONDS_FACTOR = 1000000L;

        private MonotonicClock() {
        }

        public static long timePoint() {
            return System.nanoTime();
        }

        public static long timePoint(long milliseconds) {
            return System.nanoTime() + milliseconds * MILLISECONDS_FACTOR;
        }

        public static boolean waitUntil(Object object, long timePoint) throws InterruptedException {
            long diff = timePoint - MonotonicClock.timePoint();
            long diffMilliseconds = diff / MILLISECONDS_FACTOR;
            if (diffMilliseconds > 0L) {
                object.wait(diffMilliseconds);
                return false;
            }
            return true;
        }
    }

    public static class Nanoseconds {
        public static final long MILLISECOND = 1000000L;
        public static final long SECOND = 1000000000L;

        public static long fromSeconds(long what) {
            return what * 1000000000L;
        }

        public static long currentTime() {
            return System.currentTimeMillis() * 1000000L;
        }
    }
}


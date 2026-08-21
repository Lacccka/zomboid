/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.time;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.time.TimeZones;

abstract class AbstractFormatCache<F extends Format> {
    static final int NONE = -1;
    private static final ConcurrentMap<ArrayKey, String> cDateTimeInstanceCache = new ConcurrentHashMap<ArrayKey, String>(7);
    private final ConcurrentMap<ArrayKey, F> cInstanceCache = new ConcurrentHashMap<ArrayKey, F>(7);

    AbstractFormatCache() {
    }

    static String getPatternForStyle(Integer dateStyle, Integer timeStyle, Locale locale) {
        Locale safeLocale = LocaleUtils.toLocale(locale);
        ArrayKey key = new ArrayKey(dateStyle, timeStyle, safeLocale);
        return cDateTimeInstanceCache.computeIfAbsent(key, k -> {
            try {
                DateFormat formatter = dateStyle == null ? DateFormat.getTimeInstance(timeStyle, safeLocale) : (timeStyle == null ? DateFormat.getDateInstance(dateStyle, safeLocale) : DateFormat.getDateTimeInstance(dateStyle, timeStyle, safeLocale));
                return ((SimpleDateFormat)formatter).toPattern();
            }
            catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + safeLocale);
            }
        });
    }

    protected abstract F createInstance(String var1, TimeZone var2, Locale var3);

    F getDateInstance(int dateStyle, TimeZone timeZone, Locale locale) {
        return this.getDateTimeInstance((Integer)dateStyle, null, timeZone, locale);
    }

    F getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone, Locale locale) {
        return this.getDateTimeInstance((Integer)dateStyle, (Integer)timeStyle, timeZone, locale);
    }

    private F getDateTimeInstance(Integer dateStyle, Integer timeStyle, TimeZone timeZone, Locale locale) {
        locale = LocaleUtils.toLocale(locale);
        String pattern = AbstractFormatCache.getPatternForStyle(dateStyle, timeStyle, locale);
        return this.getInstance(pattern, timeZone, locale);
    }

    public F getInstance() {
        return this.getDateTimeInstance(3, 3, TimeZone.getDefault(), Locale.getDefault());
    }

    public F getInstance(String pattern, TimeZone timeZone, Locale locale) {
        Objects.requireNonNull(pattern, "pattern");
        TimeZone actualTimeZone = TimeZones.toTimeZone(timeZone);
        Locale actualLocale = LocaleUtils.toLocale(locale);
        ArrayKey key = new ArrayKey(pattern, actualTimeZone, actualLocale);
        return (F)this.cInstanceCache.computeIfAbsent(key, k -> this.createInstance(pattern, actualTimeZone, actualLocale));
    }

    F getTimeInstance(int timeStyle, TimeZone timeZone, Locale locale) {
        return this.getDateTimeInstance(null, (Integer)timeStyle, timeZone, locale);
    }

    private static final class ArrayKey {
        private final Object[] keys;
        private final int hashCode;

        private static int computeHashCode(Object[] keys2) {
            int prime = 31;
            int result = 1;
            result = 31 * result + Arrays.hashCode(keys2);
            return result;
        }

        ArrayKey(Object ... keys2) {
            this.keys = keys2;
            this.hashCode = ArrayKey.computeHashCode(keys2);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            ArrayKey other = (ArrayKey)obj;
            return Arrays.deepEquals(this.keys, other.keys);
        }

        public int hashCode() {
            return this.hashCode;
        }
    }
}


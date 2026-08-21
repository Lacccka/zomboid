/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.brotli;

import org.apache.commons.compress.utils.OsgiUtils;

public class BrotliUtils {
    private static volatile CachedAvailability cachedBrotliAvailability = CachedAvailability.DONT_CACHE;

    static CachedAvailability getCachedBrotliAvailability() {
        return cachedBrotliAvailability;
    }

    private static boolean internalIsBrotliCompressionAvailable() {
        try {
            Class.forName("org.brotli.dec.BrotliInputStream");
            return true;
        }
        catch (Exception | NoClassDefFoundError error) {
            return false;
        }
    }

    public static boolean isBrotliCompressionAvailable() {
        CachedAvailability cachedResult = cachedBrotliAvailability;
        if (cachedResult != CachedAvailability.DONT_CACHE) {
            return cachedResult == CachedAvailability.CACHED_AVAILABLE;
        }
        return BrotliUtils.internalIsBrotliCompressionAvailable();
    }

    public static void setCacheBrotliAvailablity(boolean doCache) {
        if (!doCache) {
            cachedBrotliAvailability = CachedAvailability.DONT_CACHE;
        } else if (cachedBrotliAvailability == CachedAvailability.DONT_CACHE) {
            boolean hasBrotli = BrotliUtils.internalIsBrotliCompressionAvailable();
            cachedBrotliAvailability = hasBrotli ? CachedAvailability.CACHED_AVAILABLE : CachedAvailability.CACHED_UNAVAILABLE;
        }
    }

    private BrotliUtils() {
    }

    static {
        BrotliUtils.setCacheBrotliAvailablity(!OsgiUtils.isRunningInOsgiEnvironment());
    }

    static enum CachedAvailability {
        DONT_CACHE,
        CACHED_AVAILABLE,
        CACHED_UNAVAILABLE;

    }
}


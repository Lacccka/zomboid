/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.lzma;

import java.util.HashMap;
import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.utils.OsgiUtils;

public class LZMAUtils {
    private static final FileNameUtil fileNameUtil;
    private static final byte[] HEADER_MAGIC;
    private static volatile CachedAvailability cachedLZMAAvailability;

    static CachedAvailability getCachedLZMAAvailability() {
        return cachedLZMAAvailability;
    }

    @Deprecated
    public static String getCompressedFilename(String fileName) {
        return fileNameUtil.getCompressedFileName(fileName);
    }

    public static String getCompressedFileName(String fileName) {
        return fileNameUtil.getCompressedFileName(fileName);
    }

    @Deprecated
    public static String getUncompressedFilename(String fileName) {
        return fileNameUtil.getUncompressedFileName(fileName);
    }

    public static String getUncompressedFileName(String fileName) {
        return fileNameUtil.getUncompressedFileName(fileName);
    }

    private static boolean internalIsLZMACompressionAvailable() {
        try {
            LZMACompressorInputStream.matches(null, 0);
            return true;
        }
        catch (NoClassDefFoundError error) {
            return false;
        }
    }

    @Deprecated
    public static boolean isCompressedFilename(String fileName) {
        return fileNameUtil.isCompressedFileName(fileName);
    }

    public static boolean isCompressedFileName(String fileName) {
        return fileNameUtil.isCompressedFileName(fileName);
    }

    public static boolean isLZMACompressionAvailable() {
        CachedAvailability cachedResult = cachedLZMAAvailability;
        if (cachedResult != CachedAvailability.DONT_CACHE) {
            return cachedResult == CachedAvailability.CACHED_AVAILABLE;
        }
        return LZMAUtils.internalIsLZMACompressionAvailable();
    }

    public static boolean matches(byte[] signature, int length) {
        if (length < HEADER_MAGIC.length) {
            return false;
        }
        for (int i = 0; i < HEADER_MAGIC.length; ++i) {
            if (signature[i] == HEADER_MAGIC[i]) continue;
            return false;
        }
        return true;
    }

    public static void setCacheLZMAAvailablity(boolean doCache) {
        if (!doCache) {
            cachedLZMAAvailability = CachedAvailability.DONT_CACHE;
        } else if (cachedLZMAAvailability == CachedAvailability.DONT_CACHE) {
            boolean hasLzma = LZMAUtils.internalIsLZMACompressionAvailable();
            cachedLZMAAvailability = hasLzma ? CachedAvailability.CACHED_AVAILABLE : CachedAvailability.CACHED_UNAVAILABLE;
        }
    }

    private LZMAUtils() {
    }

    static {
        HEADER_MAGIC = new byte[]{93, 0, 0};
        HashMap<String, String> uncompressSuffix = new HashMap<String, String>();
        uncompressSuffix.put(".lzma", "");
        uncompressSuffix.put("-lzma", "");
        fileNameUtil = new FileNameUtil(uncompressSuffix, ".lzma");
        cachedLZMAAvailability = CachedAvailability.DONT_CACHE;
        LZMAUtils.setCacheLZMAAvailablity(!OsgiUtils.isRunningInOsgiEnvironment());
    }

    static enum CachedAvailability {
        DONT_CACHE,
        CACHED_AVAILABLE,
        CACHED_UNAVAILABLE;

    }
}


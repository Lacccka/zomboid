/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FileNameUtil {
    private final Map<String, String> compressSuffix = new HashMap<String, String>();
    private final Map<String, String> uncompressSuffix;
    private final int longestCompressedSuffix;
    private final int shortestCompressedSuffix;
    private final int longestUncompressedSuffix;
    private final int shortestUncompressedSuffix;
    private final String defaultExtension;

    public FileNameUtil(Map<String, String> uncompressSuffix, String defaultExtension) {
        this.uncompressSuffix = Collections.unmodifiableMap(uncompressSuffix);
        int lc = Integer.MIN_VALUE;
        int sc = Integer.MAX_VALUE;
        int lu = Integer.MIN_VALUE;
        int su = Integer.MAX_VALUE;
        for (Map.Entry<String, String> ent : uncompressSuffix.entrySet()) {
            String u;
            int ul;
            int cl = ent.getKey().length();
            if (cl > lc) {
                lc = cl;
            }
            if (cl < sc) {
                sc = cl;
            }
            if ((ul = (u = ent.getValue()).length()) <= 0) continue;
            this.compressSuffix.computeIfAbsent(u, k -> (String)ent.getKey());
            if (ul > lu) {
                lu = ul;
            }
            if (ul >= su) continue;
            su = ul;
        }
        this.longestCompressedSuffix = lc;
        this.longestUncompressedSuffix = lu;
        this.shortestCompressedSuffix = sc;
        this.shortestUncompressedSuffix = su;
        this.defaultExtension = defaultExtension;
    }

    @Deprecated
    public String getCompressedFilename(String fileName) {
        return this.getCompressedFileName(fileName);
    }

    public String getCompressedFileName(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        int n = lower.length();
        for (int i = this.shortestUncompressedSuffix; i <= this.longestUncompressedSuffix && i < n; ++i) {
            String suffix = this.compressSuffix.get(lower.substring(n - i));
            if (suffix == null) continue;
            return fileName.substring(0, n - i) + suffix;
        }
        return fileName + this.defaultExtension;
    }

    @Deprecated
    public String getUncompressedFilename(String fileName) {
        return this.getUncompressedFileName(fileName);
    }

    public String getUncompressedFileName(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        int n = lower.length();
        for (int i = this.shortestCompressedSuffix; i <= this.longestCompressedSuffix && i < n; ++i) {
            String suffix = this.uncompressSuffix.get(lower.substring(n - i));
            if (suffix == null) continue;
            return fileName.substring(0, n - i) + suffix;
        }
        return fileName;
    }

    @Deprecated
    public boolean isCompressedFilename(String fileName) {
        return this.isCompressedFileName(fileName);
    }

    public boolean isCompressedFileName(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        int n = lower.length();
        for (int i = this.shortestCompressedSuffix; i <= this.longestCompressedSuffix && i < n; ++i) {
            if (!this.uncompressSuffix.containsKey(lower.substring(n - i))) continue;
            return true;
        }
        return false;
    }
}


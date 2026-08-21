/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.bzip2;

import java.util.LinkedHashMap;
import org.apache.commons.compress.compressors.FileNameUtil;

public abstract class BZip2Utils {
    private static final FileNameUtil fileNameUtil;

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

    @Deprecated
    public static boolean isCompressedFilename(String fileName) {
        return fileNameUtil.isCompressedFileName(fileName);
    }

    public static boolean isCompressedFileName(String fileName) {
        return fileNameUtil.isCompressedFileName(fileName);
    }

    private BZip2Utils() {
    }

    static {
        LinkedHashMap<String, String> uncompressSuffix = new LinkedHashMap<String, String>();
        uncompressSuffix.put(".tar.bz2", ".tar");
        uncompressSuffix.put(".tbz2", ".tar");
        uncompressSuffix.put(".tbz", ".tar");
        uncompressSuffix.put(".bz2", "");
        uncompressSuffix.put(".bz", "");
        fileNameUtil = new FileNameUtil(uncompressSuffix, ".bz2");
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.gzip;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import org.apache.commons.compress.compressors.FileNameUtil;

public class GzipUtils {
    private static final FileNameUtil fileNameUtil;
    static final Charset GZIP_ENCODING;

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

    private GzipUtils() {
    }

    static {
        LinkedHashMap<String, String> uncompressSuffix = new LinkedHashMap<String, String>();
        uncompressSuffix.put(".tgz", ".tar");
        uncompressSuffix.put(".taz", ".tar");
        uncompressSuffix.put(".svgz", ".svg");
        uncompressSuffix.put(".cpgz", ".cpio");
        uncompressSuffix.put(".wmz", ".wmf");
        uncompressSuffix.put(".emz", ".emf");
        uncompressSuffix.put(".gz", "");
        uncompressSuffix.put(".z", "");
        uncompressSuffix.put("-gz", "");
        uncompressSuffix.put("-z", "");
        uncompressSuffix.put("_z", "");
        fileNameUtil = new FileNameUtil(uncompressSuffix, ".gz");
        GZIP_ENCODING = StandardCharsets.ISO_8859_1;
    }
}


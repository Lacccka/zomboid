/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.io.File;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;

public class FileNameUtils {
    public static String getBaseName(Path path) {
        if (path == null) {
            return null;
        }
        Path fileName = path.getFileName();
        return fileName != null ? FilenameUtils.removeExtension(fileName.toString()) : null;
    }

    @Deprecated
    public static String getBaseName(String fileName) {
        if (fileName == null) {
            return null;
        }
        return FilenameUtils.removeExtension(new File(fileName).getName());
    }

    public static String getExtension(Path path) {
        if (path == null) {
            return null;
        }
        Path fileName = path.getFileName();
        return fileName != null ? FilenameUtils.getExtension(fileName.toString()) : null;
    }

    @Deprecated
    public static String getExtension(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }
}


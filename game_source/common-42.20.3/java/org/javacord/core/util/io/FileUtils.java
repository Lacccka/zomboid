/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.io;

import java.io.File;

public class FileUtils {
    private FileUtils() {
        throw new UnsupportedOperationException("You cannot create an instance of this class");
    }

    public static String getExtension(File file) {
        return FileUtils.getExtension(file.getName());
    }

    public static String getExtension(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "png";
    }
}


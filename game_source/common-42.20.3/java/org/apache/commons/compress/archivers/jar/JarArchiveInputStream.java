/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.jar;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

public class JarArchiveInputStream
extends ZipArchiveInputStream {
    public static boolean matches(byte[] signature, int length) {
        return ZipArchiveInputStream.matches(signature, length);
    }

    public JarArchiveInputStream(InputStream inputStream2) {
        super(inputStream2);
    }

    public JarArchiveInputStream(InputStream inputStream2, String encoding) {
        super(inputStream2, encoding);
    }

    @Override
    public JarArchiveEntry getNextEntry() throws IOException {
        return this.getNextJarEntry();
    }

    @Deprecated
    public JarArchiveEntry getNextJarEntry() throws IOException {
        ZipArchiveEntry entry = this.getNextZipEntry();
        return entry == null ? null : new JarArchiveEntry(entry);
    }
}


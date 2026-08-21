/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.pack200;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import org.apache.commons.compress.compressors.pack200.AbstractStreamBridge;

final class TempFileCachingStreamBridge
extends AbstractStreamBridge {
    private final Path path = Files.createTempFile("commons-compress", "packtemp", new FileAttribute[0]);

    TempFileCachingStreamBridge() throws IOException {
        this.path.toFile().deleteOnExit();
        this.out = Files.newOutputStream(this.path, new OpenOption[0]);
    }

    @Override
    InputStream createInputStream() throws IOException {
        this.out.close();
        return new FilterInputStream(Files.newInputStream(this.path, new OpenOption[0])){

            @Override
            public void close() throws IOException {
                try {
                    super.close();
                }
                finally {
                    try {
                        Files.deleteIfExists(TempFileCachingStreamBridge.this.path);
                    }
                    catch (IOException iOException) {}
                }
            }
        };
    }
}


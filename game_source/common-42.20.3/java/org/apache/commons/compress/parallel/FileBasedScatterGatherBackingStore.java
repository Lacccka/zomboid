/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.parallel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import org.apache.commons.compress.parallel.ScatterGatherBackingStore;

public class FileBasedScatterGatherBackingStore
implements ScatterGatherBackingStore {
    private final Path target;
    private final OutputStream outputStream;
    private boolean closed;

    public FileBasedScatterGatherBackingStore(File target) throws FileNotFoundException {
        this(target.toPath());
    }

    public FileBasedScatterGatherBackingStore(Path target) throws FileNotFoundException {
        this.target = target;
        try {
            this.outputStream = Files.newOutputStream(target, new OpenOption[0]);
        }
        catch (FileNotFoundException ex) {
            throw ex;
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.closeForWriting();
        }
        finally {
            Files.deleteIfExists(this.target);
        }
    }

    @Override
    public void closeForWriting() throws IOException {
        if (!this.closed) {
            this.outputStream.close();
            this.closed = true;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(this.target, new OpenOption[0]);
    }

    @Override
    public void writeOut(byte[] data, int offset, int length) throws IOException {
        this.outputStream.write(data, offset, length);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import org.apache.commons.compress.archivers.ArchiveEntry;

public abstract class ArchiveOutputStream<E extends ArchiveEntry>
extends FilterOutputStream {
    static final int BYTE_MASK = 255;
    private final byte[] oneByte = new byte[1];
    private long bytesWritten;
    private boolean closed;
    private boolean finished;

    public ArchiveOutputStream() {
        super(null);
    }

    public ArchiveOutputStream(OutputStream out) {
        super(out);
    }

    public boolean canWriteEntryData(ArchiveEntry archiveEntry) {
        return true;
    }

    protected void checkFinished() throws IOException {
        if (this.isFinished()) {
            throw new IOException("Stream has already been finished.");
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.closed = true;
    }

    public abstract void closeArchiveEntry() throws IOException;

    protected void count(int written) {
        this.count((long)written);
    }

    protected void count(long written) {
        if (written != -1L) {
            this.bytesWritten += written;
        }
    }

    public abstract E createArchiveEntry(File var1, String var2) throws IOException;

    public E createArchiveEntry(Path inputPath, String entryName, LinkOption ... options) throws IOException {
        return this.createArchiveEntry(inputPath.toFile(), entryName);
    }

    public void finish() throws IOException {
        this.finished = true;
    }

    public long getBytesWritten() {
        return this.bytesWritten;
    }

    @Deprecated
    public int getCount() {
        return (int)this.bytesWritten;
    }

    protected boolean isClosed() {
        return this.closed;
    }

    protected boolean isFinished() {
        return this.finished;
    }

    public abstract void putArchiveEntry(E var1) throws IOException;

    @Override
    public void write(int b) throws IOException {
        this.oneByte[0] = (byte)(b & 0xFF);
        this.write(this.oneByte, 0, 1);
    }

    protected void checkOpen() throws IOException {
        if (this.isClosed()) {
            throw new IOException("Stream closed");
        }
    }
}


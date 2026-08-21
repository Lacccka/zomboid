/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Objects;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOIterator;
import org.apache.commons.io.input.NullInputStream;

public abstract class ArchiveInputStream<E extends ArchiveEntry>
extends FilterInputStream {
    private static final int BYTE_MASK = 255;
    private final byte[] single = new byte[1];
    private long bytesRead;
    private Charset charset;

    public ArchiveInputStream() {
        this((InputStream)NullInputStream.INSTANCE, Charset.defaultCharset());
    }

    private ArchiveInputStream(InputStream inputStream2, Charset charset) {
        super(inputStream2);
        this.charset = Charsets.toCharset(charset);
    }

    protected ArchiveInputStream(InputStream inputStream2, String charsetName) {
        this(inputStream2, Charsets.toCharset(charsetName));
    }

    public boolean canReadEntryData(ArchiveEntry archiveEntry) {
        return true;
    }

    protected void count(int read) {
        this.count((long)read);
    }

    protected void count(long read) {
        if (read != -1L) {
            this.bytesRead += read;
        }
    }

    public void forEach(IOConsumer<? super E> action) throws IOException {
        this.iterator().forEachRemaining(Objects.requireNonNull(action));
    }

    public long getBytesRead() {
        return this.bytesRead;
    }

    public Charset getCharset() {
        return this.charset;
    }

    @Deprecated
    public int getCount() {
        return (int)this.bytesRead;
    }

    public abstract E getNextEntry() throws IOException;

    public IOIterator<E> iterator() {
        return new ArchiveEntryIOIterator();
    }

    @Override
    public synchronized void mark(int readlimit) {
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    protected void pushedBackBytes(long pushedBack) {
        this.bytesRead -= pushedBack;
    }

    @Override
    public int read() throws IOException {
        int num = this.read(this.single, 0, 1);
        return num == -1 ? -1 : this.single[0] & 0xFF;
    }

    @Override
    public synchronized void reset() throws IOException {
    }

    class ArchiveEntryIOIterator
    implements IOIterator<E> {
        private E next;

        ArchiveEntryIOIterator() {
        }

        @Override
        public boolean hasNext() throws IOException {
            if (this.next == null) {
                this.next = ArchiveInputStream.this.getNextEntry();
            }
            return this.next != null;
        }

        @Override
        public synchronized E next() throws IOException {
            if (this.next != null) {
                Object e = this.next;
                this.next = null;
                return e;
            }
            return ArchiveInputStream.this.getNextEntry();
        }

        @Override
        public Iterator<E> unwrap() {
            return null;
        }
    }
}


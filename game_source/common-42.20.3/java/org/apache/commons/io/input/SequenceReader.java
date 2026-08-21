/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import org.apache.commons.io.function.Uncheck;

public class SequenceReader
extends Reader {
    private Reader reader;
    private final Iterator<? extends Reader> readers;

    public SequenceReader(Iterable<? extends Reader> readers) {
        this.readers = Objects.requireNonNull(readers, "readers").iterator();
        this.reader = Uncheck.get(this::nextReader);
    }

    public SequenceReader(Reader ... readers) {
        this(Arrays.asList(readers));
    }

    @Override
    public void close() throws IOException {
        while (this.nextReader() != null) {
        }
    }

    private Reader nextReader() throws IOException {
        if (this.reader != null) {
            this.reader.close();
        }
        this.reader = this.readers.hasNext() ? this.readers.next() : null;
        return this.reader;
    }

    @Override
    public int read() throws IOException {
        int c = -1;
        while (this.reader != null && (c = this.reader.read()) == -1) {
            this.nextReader();
        }
        return c;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        Objects.requireNonNull(cbuf, "cbuf");
        if (len < 0 || off < 0 || off + len > cbuf.length) {
            throw new IndexOutOfBoundsException("Array Size=" + cbuf.length + ", offset=" + off + ", length=" + len);
        }
        int count = 0;
        while (this.reader != null) {
            int readLen = this.reader.read(cbuf, off, len);
            if (readLen == -1) {
                this.nextReader();
                continue;
            }
            count += readLen;
            off += readLen;
            if ((len -= readLen) > 0) continue;
            break;
        }
        if (count > 0) {
            return count;
        }
        return -1;
    }
}


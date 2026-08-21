/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.apache.commons.io.IOUtils;

public class LineIterator
implements Iterator<String>,
Closeable {
    private final BufferedReader bufferedReader;
    private String cachedLine;
    private boolean finished;

    @Deprecated
    public static void closeQuietly(LineIterator iterator2) {
        IOUtils.closeQuietly((Closeable)iterator2);
    }

    public LineIterator(Reader reader) {
        Objects.requireNonNull(reader, "reader");
        this.bufferedReader = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
    }

    @Override
    public void close() throws IOException {
        this.finished = true;
        this.cachedLine = null;
        IOUtils.close((Closeable)this.bufferedReader);
    }

    @Override
    public boolean hasNext() {
        if (this.cachedLine != null) {
            return true;
        }
        if (this.finished) {
            return false;
        }
        try {
            String line;
            do {
                if ((line = this.bufferedReader.readLine()) != null) continue;
                this.finished = true;
                return false;
            } while (!this.isValidLine(line));
            this.cachedLine = line;
            return true;
        }
        catch (IOException ioe) {
            IOUtils.closeQuietly((Closeable)this, ioe::addSuppressed);
            throw new IllegalStateException(ioe);
        }
    }

    protected boolean isValidLine(String line) {
        return true;
    }

    @Override
    public String next() {
        return this.nextLine();
    }

    public String nextLine() {
        if (!this.hasNext()) {
            throw new NoSuchElementException("No more lines");
        }
        String currentLine = this.cachedLine;
        this.cachedLine = null;
        return currentLine;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove not supported");
    }
}


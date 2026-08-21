/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.input.UnsupportedOperationExceptions;

public class WindowsLineEndingInputStream
extends InputStream {
    private boolean atEos;
    private boolean atSlashCr;
    private boolean atSlashLf;
    private final InputStream in;
    private boolean injectSlashLf;
    private final boolean lineFeedAtEndOfFile;

    public WindowsLineEndingInputStream(InputStream in, boolean ensureLineFeedAtEndOfFile) {
        this.in = in;
        this.lineFeedAtEndOfFile = ensureLineFeedAtEndOfFile;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.in.close();
    }

    private int handleEos() {
        if (!this.lineFeedAtEndOfFile) {
            return -1;
        }
        if (!this.atSlashLf && !this.atSlashCr) {
            this.atSlashCr = true;
            return 13;
        }
        if (!this.atSlashLf) {
            this.atSlashCr = false;
            this.atSlashLf = true;
            return 10;
        }
        return -1;
    }

    @Override
    public synchronized void mark(int readLimit) {
        throw UnsupportedOperationExceptions.mark();
    }

    @Override
    public int read() throws IOException {
        if (this.atEos) {
            return this.handleEos();
        }
        if (this.injectSlashLf) {
            this.injectSlashLf = false;
            return 10;
        }
        boolean prevWasSlashR = this.atSlashCr;
        int target = this.readWithUpdate();
        if (this.atEos) {
            return this.handleEos();
        }
        if (target == 10 && !prevWasSlashR) {
            this.injectSlashLf = true;
            return 13;
        }
        return target;
    }

    private int readWithUpdate() throws IOException {
        int target = this.in.read();
        boolean bl = this.atEos = target == -1;
        if (this.atEos) {
            return target;
        }
        this.atSlashCr = target == 13;
        this.atSlashLf = target == 10;
        return target;
    }
}


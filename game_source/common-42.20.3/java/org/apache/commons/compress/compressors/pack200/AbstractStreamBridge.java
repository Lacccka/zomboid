/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.pack200;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

abstract class AbstractStreamBridge
extends FilterOutputStream {
    private InputStream inputStream;
    private final Object inputStreamLock = new Object();

    protected AbstractStreamBridge() {
        this(null);
    }

    protected AbstractStreamBridge(OutputStream outputStream2) {
        super(outputStream2);
    }

    abstract InputStream createInputStream() throws IOException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    InputStream getInputStream() throws IOException {
        Object object = this.inputStreamLock;
        synchronized (object) {
            if (this.inputStream == null) {
                this.inputStream = this.createInputStream();
            }
        }
        return this.inputStream;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void stop() throws IOException {
        this.close();
        Object object = this.inputStreamLock;
        synchronized (object) {
            if (this.inputStream != null) {
                this.inputStream.close();
                this.inputStream = null;
            }
        }
    }
}


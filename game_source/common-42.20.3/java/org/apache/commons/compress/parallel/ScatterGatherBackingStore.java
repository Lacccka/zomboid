/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.parallel;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface ScatterGatherBackingStore
extends Closeable {
    public void closeForWriting() throws IOException;

    public InputStream getInputStream() throws IOException;

    public void writeOut(byte[] var1, int var2, int var3) throws IOException;
}


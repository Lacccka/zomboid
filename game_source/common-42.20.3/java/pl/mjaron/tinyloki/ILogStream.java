/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.Labels;

public interface ILogStream {
    public static final long TIMESTAMP_NONE = -1L;

    public void log(long var1, String var3, Labels var4);

    default public void log(long timestampNs, String line) {
        this.log(timestampNs, line, null);
    }

    default public void log(String line) {
        this.log(-1L, line, null);
    }

    default public void log(String line, Labels structuredMetadata) {
        this.log(-1L, line, structuredMetadata);
    }

    public void release();
}


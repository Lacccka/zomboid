/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ArrayDecoders;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CheckReturnValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ExtensionRegistryLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Reader;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Writer;
import java.io.IOException;

@CheckReturnValue
interface Schema<T> {
    public void writeTo(T var1, Writer var2) throws IOException;

    public void mergeFrom(T var1, Reader var2, ExtensionRegistryLite var3) throws IOException;

    public void mergeFrom(T var1, byte[] var2, int var3, int var4, ArrayDecoders.Registers var5) throws IOException;

    public void makeImmutable(T var1);

    public boolean isInitialized(T var1);

    public T newInstance();

    public boolean equals(T var1, T var2);

    public int hashCode(T var1);

    public void mergeFrom(T var1, T var2);

    public int getSerializedSize(T var1);
}


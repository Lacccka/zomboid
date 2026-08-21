/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CheckReturnValue;
import java.util.List;

@CheckReturnValue
interface ListFieldSchema {
    public <L> List<L> mutableListAt(Object var1, long var2);

    public void makeImmutableListAt(Object var1, long var2);

    public <L> void mergeListsAt(Object var1, Object var2, long var3);
}


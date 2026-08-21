/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Message;
import java.util.List;

public abstract class MapFieldReflectionAccessor {
    abstract List<Message> getList();

    abstract List<Message> getMutableList();

    abstract Message getMapEntryMessageDefaultInstance();
}


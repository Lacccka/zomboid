/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Value;
import java.util.Map;

public interface StructOrBuilder
extends MessageOrBuilder {
    public int getFieldsCount();

    public boolean containsFields(String var1);

    @Deprecated
    public Map<String, Value> getFields();

    public Map<String, Value> getFieldsMap();

    public Value getFieldsOrDefault(String var1, Value var2);

    public Value getFieldsOrThrow(String var1);
}


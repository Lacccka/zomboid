/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Value;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ValueOrBuilder;
import java.util.List;

public interface ListValueOrBuilder
extends MessageOrBuilder {
    public List<Value> getValuesList();

    public Value getValues(int var1);

    public int getValuesCount();

    public List<? extends ValueOrBuilder> getValuesOrBuilderList();

    public ValueOrBuilder getValuesOrBuilder(int var1);
}


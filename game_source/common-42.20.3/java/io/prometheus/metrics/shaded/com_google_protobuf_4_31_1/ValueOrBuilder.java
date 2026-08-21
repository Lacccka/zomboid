/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ByteString;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ListValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ListValueOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.NullValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Struct;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.StructOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Value;

public interface ValueOrBuilder
extends MessageOrBuilder {
    public boolean hasNullValue();

    public int getNullValueValue();

    public NullValue getNullValue();

    public boolean hasNumberValue();

    public double getNumberValue();

    public boolean hasStringValue();

    public String getStringValue();

    public ByteString getStringValueBytes();

    public boolean hasBoolValue();

    public boolean getBoolValue();

    public boolean hasStructValue();

    public Struct getStructValue();

    public StructOrBuilder getStructValueOrBuilder();

    public boolean hasListValue();

    public ListValue getListValue();

    public ListValueOrBuilder getListValueOrBuilder();

    public Value.KindCase getKindCase();
}


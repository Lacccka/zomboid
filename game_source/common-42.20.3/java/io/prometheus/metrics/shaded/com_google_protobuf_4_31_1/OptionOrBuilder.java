/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Any;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.AnyOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ByteString;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;

public interface OptionOrBuilder
extends MessageOrBuilder {
    public String getName();

    public ByteString getNameBytes();

    public boolean hasValue();

    public Any getValue();

    public AnyOrBuilder getValueOrBuilder();
}


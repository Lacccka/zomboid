/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ByteString;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import java.util.List;

public interface FieldMaskOrBuilder
extends MessageOrBuilder {
    public List<String> getPathsList();

    public int getPathsCount();

    public String getPaths(int var1);

    public ByteString getPathsBytes(int var1);
}


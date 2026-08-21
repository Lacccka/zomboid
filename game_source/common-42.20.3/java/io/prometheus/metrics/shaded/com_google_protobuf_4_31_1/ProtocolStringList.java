/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ByteString;
import java.util.List;

public interface ProtocolStringList
extends List<String> {
    public List<ByteString> asByteStringList();
}


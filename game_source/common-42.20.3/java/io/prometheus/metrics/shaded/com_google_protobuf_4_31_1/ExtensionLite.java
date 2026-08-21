/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.WireFormat;

public abstract class ExtensionLite<ContainingType extends MessageLite, Type> {
    public abstract int getNumber();

    public abstract WireFormat.FieldType getLiteType();

    public abstract boolean isRepeated();

    public abstract Type getDefaultValue();

    public abstract MessageLite getMessageDefaultInstance();

    boolean isLite() {
        return true;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Descriptors;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ExtensionLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Message;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageLite;

public abstract class Extension<ContainingType extends MessageLite, Type>
extends ExtensionLite<ContainingType, Type> {
    @Override
    public abstract Message getMessageDefaultInstance();

    public abstract Descriptors.FieldDescriptor getDescriptor();

    @Override
    final boolean isLite() {
        return false;
    }

    protected abstract ExtensionType getExtensionType();

    public MessageType getMessageType() {
        return MessageType.PROTO2;
    }

    protected abstract Object fromReflectionType(Object var1);

    protected abstract Object singularFromReflectionType(Object var1);

    protected abstract Object toReflectionType(Object var1);

    protected abstract Object singularToReflectionType(Object var1);

    public static enum MessageType {
        PROTO1,
        PROTO2;

    }

    protected static enum ExtensionType {
        IMMUTABLE,
        MUTABLE,
        PROTO1;

    }
}


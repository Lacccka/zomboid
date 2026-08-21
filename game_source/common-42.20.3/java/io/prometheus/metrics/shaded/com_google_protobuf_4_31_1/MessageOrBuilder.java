/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CheckReturnValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Descriptors;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Message;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageLiteOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.UnknownFieldSet;
import java.util.List;
import java.util.Map;

@CheckReturnValue
public interface MessageOrBuilder
extends MessageLiteOrBuilder {
    @Override
    public Message getDefaultInstanceForType();

    public List<String> findInitializationErrors();

    public String getInitializationErrorString();

    public Descriptors.Descriptor getDescriptorForType();

    public Map<Descriptors.FieldDescriptor, Object> getAllFields();

    public boolean hasOneof(Descriptors.OneofDescriptor var1);

    public Descriptors.FieldDescriptor getOneofFieldDescriptor(Descriptors.OneofDescriptor var1);

    public boolean hasField(Descriptors.FieldDescriptor var1);

    public Object getField(Descriptors.FieldDescriptor var1);

    public int getRepeatedFieldCount(Descriptors.FieldDescriptor var1);

    public Object getRepeatedField(Descriptors.FieldDescriptor var1, int var2);

    public UnknownFieldSet getUnknownFields();
}


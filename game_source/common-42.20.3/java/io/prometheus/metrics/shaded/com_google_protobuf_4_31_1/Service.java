/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Descriptors;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Message;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.RpcCallback;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.RpcController;

public interface Service {
    public Descriptors.ServiceDescriptor getDescriptorForType();

    public void callMethod(Descriptors.MethodDescriptor var1, RpcController var2, Message var3, RpcCallback<Message> var4);

    public Message getRequestPrototype(Descriptors.MethodDescriptor var1);

    public Message getResponsePrototype(Descriptors.MethodDescriptor var1);
}


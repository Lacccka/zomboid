/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Descriptors;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Message;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.RpcController;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ServiceException;

public interface BlockingService {
    public Descriptors.ServiceDescriptor getDescriptorForType();

    public Message callBlockingMethod(Descriptors.MethodDescriptor var1, RpcController var2, Message var3) throws ServiceException;

    public Message getRequestPrototype(Descriptors.MethodDescriptor var1);

    public Message getResponsePrototype(Descriptors.MethodDescriptor var1);
}


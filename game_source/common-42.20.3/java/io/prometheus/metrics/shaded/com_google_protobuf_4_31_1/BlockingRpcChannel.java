/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Descriptors;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Message;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.RpcController;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ServiceException;

public interface BlockingRpcChannel {
    public Message callBlockingMethod(Descriptors.MethodDescriptor var1, RpcController var2, Message var3, Message var4) throws ServiceException;
}


/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.RpcCallback;

public interface RpcController {
    public void reset();

    public boolean failed();

    public String errorText();

    public void startCancel();

    public void setFailed(String var1);

    public boolean isCanceled();

    public void notifyOnCancel(RpcCallback<Object> var1);
}


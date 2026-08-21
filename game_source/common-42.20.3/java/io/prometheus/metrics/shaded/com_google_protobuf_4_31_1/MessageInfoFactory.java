/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CheckReturnValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageInfo;

@CheckReturnValue
interface MessageInfoFactory {
    public boolean isSupported(Class<?> var1);

    public MessageInfo messageInfoFor(Class<?> var1);
}


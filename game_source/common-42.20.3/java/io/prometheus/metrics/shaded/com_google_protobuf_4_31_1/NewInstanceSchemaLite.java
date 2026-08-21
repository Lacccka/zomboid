/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CheckReturnValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.GeneratedMessageLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.NewInstanceSchema;

@CheckReturnValue
final class NewInstanceSchemaLite
implements NewInstanceSchema {
    NewInstanceSchemaLite() {
    }

    @Override
    public Object newInstance(Object defaultInstance) {
        return ((GeneratedMessageLite)defaultInstance).newMutableInstance();
    }
}


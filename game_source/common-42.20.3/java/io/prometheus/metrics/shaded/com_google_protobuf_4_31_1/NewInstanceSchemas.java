/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CheckReturnValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.NewInstanceSchema;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.NewInstanceSchemaLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Protobuf;

@CheckReturnValue
final class NewInstanceSchemas {
    private static final NewInstanceSchema FULL_SCHEMA = NewInstanceSchemas.loadSchemaForFullRuntime();
    private static final NewInstanceSchema LITE_SCHEMA = new NewInstanceSchemaLite();

    static NewInstanceSchema full() {
        return FULL_SCHEMA;
    }

    static NewInstanceSchema lite() {
        return LITE_SCHEMA;
    }

    private static NewInstanceSchema loadSchemaForFullRuntime() {
        if (Protobuf.assumeLiteRuntime) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName("io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.NewInstanceSchemaFull");
            return (NewInstanceSchema)clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (Exception e) {
            return null;
        }
    }

    private NewInstanceSchemas() {
    }
}


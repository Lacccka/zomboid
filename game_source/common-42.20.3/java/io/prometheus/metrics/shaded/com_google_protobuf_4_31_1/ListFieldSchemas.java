/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CheckReturnValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ListFieldSchema;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ListFieldSchemaLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Protobuf;

@CheckReturnValue
final class ListFieldSchemas {
    private static final ListFieldSchema FULL_SCHEMA = ListFieldSchemas.loadSchemaForFullRuntime();
    private static final ListFieldSchema LITE_SCHEMA = new ListFieldSchemaLite();

    static ListFieldSchema full() {
        return FULL_SCHEMA;
    }

    static ListFieldSchema lite() {
        return LITE_SCHEMA;
    }

    private static ListFieldSchema loadSchemaForFullRuntime() {
        if (Protobuf.assumeLiteRuntime) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName("io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ListFieldSchemaFull");
            return (ListFieldSchema)clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (Exception e) {
            return null;
        }
    }

    private ListFieldSchemas() {
    }
}


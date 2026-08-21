/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CheckReturnValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MapFieldSchema;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MapFieldSchemaLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Protobuf;

@CheckReturnValue
final class MapFieldSchemas {
    private static final MapFieldSchema FULL_SCHEMA = MapFieldSchemas.loadSchemaForFullRuntime();
    private static final MapFieldSchema LITE_SCHEMA = new MapFieldSchemaLite();

    static MapFieldSchema full() {
        return FULL_SCHEMA;
    }

    static MapFieldSchema lite() {
        return LITE_SCHEMA;
    }

    private static MapFieldSchema loadSchemaForFullRuntime() {
        if (Protobuf.assumeLiteRuntime) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName("io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MapFieldSchemaFull");
            return (MapFieldSchema)clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (Exception e) {
            return null;
        }
    }

    private MapFieldSchemas() {
    }
}


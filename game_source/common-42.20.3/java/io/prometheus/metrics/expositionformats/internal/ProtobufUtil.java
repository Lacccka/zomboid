/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.expositionformats.internal;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.TextFormat;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Timestamp;

public class ProtobufUtil {
    static Timestamp timestampFromMillis(long timestampMillis) {
        return Timestamp.newBuilder().setSeconds(timestampMillis / 1000L).setNanos((int)(timestampMillis % 1000L * 1000000L)).build();
    }

    public static String shortDebugString(MessageOrBuilder protobufData) {
        return TextFormat.printer().emittingSingleLine(true).printToString(protobufData);
    }
}


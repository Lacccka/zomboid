/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ProtobufToStringOutput;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.TextFormat;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.UnknownFieldSet;

public final class LegacyUnredactedTextFormat {
    private LegacyUnredactedTextFormat() {
    }

    public static String legacyUnredactedMultilineString(MessageOrBuilder message) {
        return TextFormat.printer().printToString(message, TextFormat.Printer.FieldReporterLevel.LEGACY_MULTILINE);
    }

    public static String legacyUnredactedMultilineString(UnknownFieldSet fields) {
        return TextFormat.printer().printToString(fields);
    }

    public static String legacyUnredactedSingleLineString(MessageOrBuilder message) {
        return TextFormat.printer().emittingSingleLine(true).printToString(message, TextFormat.Printer.FieldReporterLevel.LEGACY_SINGLE_LINE);
    }

    public static String legacyUnredactedSingleLineString(UnknownFieldSet fields) {
        return TextFormat.printer().emittingSingleLine(true).printToString(fields);
    }

    public static String legacyUnredactedToString(Object object) {
        String[] result = new String[1];
        ProtobufToStringOutput.callWithTextFormat(() -> {
            result[0] = object.toString();
        });
        return result[0];
    }

    public static String legacyUnredactedStringValueOf(Object object) {
        return object == null ? String.valueOf(object) : LegacyUnredactedTextFormat.legacyUnredactedToString(object);
    }
}


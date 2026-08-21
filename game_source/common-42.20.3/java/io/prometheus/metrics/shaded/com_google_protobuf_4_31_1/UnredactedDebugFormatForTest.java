/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.LegacyUnredactedTextFormat;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.TextFormat;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.UnknownFieldSet;

public final class UnredactedDebugFormatForTest {
    private UnredactedDebugFormatForTest() {
    }

    public static String unredactedMultilineString(MessageOrBuilder message) {
        return TextFormat.printer().printToString(message, TextFormat.Printer.FieldReporterLevel.TEXT_GENERATOR);
    }

    public static String unredactedMultilineString(UnknownFieldSet fields) {
        return TextFormat.printer().printToString(fields);
    }

    public static String unredactedSingleLineString(MessageOrBuilder message) {
        return TextFormat.printer().emittingSingleLine(true).printToString(message, TextFormat.Printer.FieldReporterLevel.TEXT_GENERATOR);
    }

    public static String unredactedSingleLineString(UnknownFieldSet fields) {
        return TextFormat.printer().emittingSingleLine(true).printToString(fields);
    }

    public static String unredactedToString(Object object) {
        return LegacyUnredactedTextFormat.legacyUnredactedToString(object);
    }

    public static String unredactedStringValueOf(Object object) {
        return LegacyUnredactedTextFormat.legacyUnredactedStringValueOf(object);
    }
}


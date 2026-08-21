/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Descriptors;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.TextFormat;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.UnknownFieldSet;

public final class DebugFormat {
    private final boolean isSingleLine;

    private DebugFormat(boolean singleLine) {
        this.isSingleLine = singleLine;
    }

    public static DebugFormat singleLine() {
        return new DebugFormat(true);
    }

    public static DebugFormat multiline() {
        return new DebugFormat(false);
    }

    public String toString(MessageOrBuilder message) {
        if (!this.isSingleLine) {
            return TextFormat.debugFormatPrinter().printToString(message, TextFormat.Printer.FieldReporterLevel.DEBUG_MULTILINE);
        }
        return TextFormat.debugFormatPrinter().emittingSingleLine(this.isSingleLine).printToString(message, TextFormat.Printer.FieldReporterLevel.DEBUG_SINGLE_LINE);
    }

    public String toString(Descriptors.FieldDescriptor field, Object value) {
        if (!this.isSingleLine) {
            return TextFormat.debugFormatPrinter().printFieldToString(field, value);
        }
        return TextFormat.debugFormatPrinter().emittingSingleLine(this.isSingleLine).printFieldToString(field, value);
    }

    public String toString(UnknownFieldSet fields) {
        if (!this.isSingleLine) {
            return TextFormat.debugFormatPrinter().printToString(fields);
        }
        return TextFormat.debugFormatPrinter().emittingSingleLine(this.isSingleLine).printToString(fields);
    }

    public Object lazyToString(MessageOrBuilder message) {
        return new LazyDebugOutput(message, this);
    }

    public Object lazyToString(UnknownFieldSet fields) {
        return new LazyDebugOutput(fields, this);
    }

    private static class LazyDebugOutput {
        private final MessageOrBuilder message;
        private final UnknownFieldSet fields;
        private final DebugFormat format;

        LazyDebugOutput(MessageOrBuilder message, DebugFormat format) {
            this.message = message;
            this.fields = null;
            this.format = format;
        }

        LazyDebugOutput(UnknownFieldSet fields, DebugFormat format) {
            this.message = null;
            this.fields = fields;
            this.format = format;
        }

        public String toString() {
            if (this.message != null) {
                return this.format.toString(this.message);
            }
            return this.format.toString(this.fields);
        }
    }
}


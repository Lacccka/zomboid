/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ByteString;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CheckReturnValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.GeneratedMessageLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Reader;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.UnknownFieldSchema;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.UnknownFieldSetLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.WireFormat;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Writer;
import java.io.IOException;

@CheckReturnValue
class UnknownFieldSetLiteSchema
extends UnknownFieldSchema<UnknownFieldSetLite, UnknownFieldSetLite> {
    UnknownFieldSetLiteSchema() {
    }

    @Override
    boolean shouldDiscardUnknownFields(Reader reader) {
        return false;
    }

    @Override
    UnknownFieldSetLite newBuilder() {
        return UnknownFieldSetLite.newInstance();
    }

    @Override
    void addVarint(UnknownFieldSetLite fields, int number, long value) {
        fields.storeField(WireFormat.makeTag(number, 0), value);
    }

    @Override
    void addFixed32(UnknownFieldSetLite fields, int number, int value) {
        fields.storeField(WireFormat.makeTag(number, 5), value);
    }

    @Override
    void addFixed64(UnknownFieldSetLite fields, int number, long value) {
        fields.storeField(WireFormat.makeTag(number, 1), value);
    }

    @Override
    void addLengthDelimited(UnknownFieldSetLite fields, int number, ByteString value) {
        fields.storeField(WireFormat.makeTag(number, 2), value);
    }

    @Override
    void addGroup(UnknownFieldSetLite fields, int number, UnknownFieldSetLite subFieldSet) {
        fields.storeField(WireFormat.makeTag(number, 3), subFieldSet);
    }

    @Override
    UnknownFieldSetLite toImmutable(UnknownFieldSetLite fields) {
        fields.makeImmutable();
        return fields;
    }

    @Override
    void setToMessage(Object message, UnknownFieldSetLite fields) {
        ((GeneratedMessageLite)message).unknownFields = fields;
    }

    @Override
    UnknownFieldSetLite getFromMessage(Object message) {
        return ((GeneratedMessageLite)message).unknownFields;
    }

    @Override
    UnknownFieldSetLite getBuilderFromMessage(Object message) {
        UnknownFieldSetLite unknownFields = this.getFromMessage(message);
        if (unknownFields == UnknownFieldSetLite.getDefaultInstance()) {
            unknownFields = UnknownFieldSetLite.newInstance();
            this.setToMessage(message, unknownFields);
        }
        return unknownFields;
    }

    @Override
    void setBuilderToMessage(Object message, UnknownFieldSetLite fields) {
        this.setToMessage(message, fields);
    }

    @Override
    void makeImmutable(Object message) {
        this.getFromMessage(message).makeImmutable();
    }

    @Override
    void writeTo(UnknownFieldSetLite fields, Writer writer) throws IOException {
        fields.writeTo(writer);
    }

    @Override
    void writeAsMessageSetTo(UnknownFieldSetLite fields, Writer writer) throws IOException {
        fields.writeAsMessageSetTo(writer);
    }

    @Override
    UnknownFieldSetLite merge(UnknownFieldSetLite target, UnknownFieldSetLite source2) {
        if (UnknownFieldSetLite.getDefaultInstance().equals(source2)) {
            return target;
        }
        if (UnknownFieldSetLite.getDefaultInstance().equals(target)) {
            return UnknownFieldSetLite.mutableCopyOf(target, source2);
        }
        return target.mergeFrom(source2);
    }

    @Override
    int getSerializedSize(UnknownFieldSetLite unknowns) {
        return unknowns.getSerializedSize();
    }

    @Override
    int getSerializedSizeAsMessageSet(UnknownFieldSetLite unknowns) {
        return unknowns.getSerializedSizeAsMessageSet();
    }
}


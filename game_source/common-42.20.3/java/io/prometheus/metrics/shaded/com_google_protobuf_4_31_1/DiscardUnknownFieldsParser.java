/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.AbstractParser;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CodedInputStream;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ExtensionRegistryLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.InvalidProtocolBufferException;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Message;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Parser;

public final class DiscardUnknownFieldsParser {
    public static final <T extends Message> Parser<T> wrap(final Parser<T> parser) {
        return new AbstractParser<T>(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public T parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                try {
                    input.discardUnknownFields();
                    Message message = (Message)parser.parsePartialFrom(input, extensionRegistry);
                    return message;
                }
                finally {
                    input.unsetDiscardUnknownFields();
                }
            }
        };
    }

    private DiscardUnknownFieldsParser() {
    }
}


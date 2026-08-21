/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CheckReturnValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ExtensionSchemas;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.GeneratedMessageInfoFactory;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.GeneratedMessageLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Internal;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ListFieldSchemas;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MapFieldSchemas;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageInfo;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageInfoFactory;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageSchema;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageSetSchema;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.NewInstanceSchemas;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Protobuf;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Schema;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.SchemaFactory;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.SchemaUtil;

@CheckReturnValue
final class ManifestSchemaFactory
implements SchemaFactory {
    private final MessageInfoFactory messageInfoFactory;
    private static final MessageInfoFactory EMPTY_FACTORY = new MessageInfoFactory(){

        @Override
        public boolean isSupported(Class<?> clazz) {
            return false;
        }

        @Override
        public MessageInfo messageInfoFor(Class<?> clazz) {
            throw new IllegalStateException("This should never be called.");
        }
    };

    public ManifestSchemaFactory() {
        this(ManifestSchemaFactory.getDefaultMessageInfoFactory());
    }

    private ManifestSchemaFactory(MessageInfoFactory messageInfoFactory) {
        this.messageInfoFactory = Internal.checkNotNull(messageInfoFactory, "messageInfoFactory");
    }

    @Override
    public <T> Schema<T> createSchema(Class<T> messageType) {
        SchemaUtil.requireGeneratedMessage(messageType);
        MessageInfo messageInfo = this.messageInfoFactory.messageInfoFor(messageType);
        if (messageInfo.isMessageSetWireFormat()) {
            return ManifestSchemaFactory.useLiteRuntime(messageType) ? MessageSetSchema.newSchema(SchemaUtil.unknownFieldSetLiteSchema(), ExtensionSchemas.lite(), messageInfo.getDefaultInstance()) : MessageSetSchema.newSchema(SchemaUtil.unknownFieldSetFullSchema(), ExtensionSchemas.full(), messageInfo.getDefaultInstance());
        }
        return ManifestSchemaFactory.newSchema(messageType, messageInfo);
    }

    private static <T> Schema<T> newSchema(Class<T> messageType, MessageInfo messageInfo) {
        return ManifestSchemaFactory.useLiteRuntime(messageType) ? MessageSchema.newSchema(messageType, messageInfo, NewInstanceSchemas.lite(), ListFieldSchemas.lite(), SchemaUtil.unknownFieldSetLiteSchema(), ManifestSchemaFactory.allowExtensions(messageInfo) ? ExtensionSchemas.lite() : null, MapFieldSchemas.lite()) : MessageSchema.newSchema(messageType, messageInfo, NewInstanceSchemas.full(), ListFieldSchemas.full(), SchemaUtil.unknownFieldSetFullSchema(), ManifestSchemaFactory.allowExtensions(messageInfo) ? ExtensionSchemas.full() : null, MapFieldSchemas.full());
    }

    private static boolean allowExtensions(MessageInfo messageInfo) {
        switch (messageInfo.getSyntax()) {
            case PROTO3: {
                return false;
            }
        }
        return true;
    }

    private static MessageInfoFactory getDefaultMessageInfoFactory() {
        return new CompositeMessageInfoFactory(GeneratedMessageInfoFactory.getInstance(), ManifestSchemaFactory.getDescriptorMessageInfoFactory());
    }

    private static MessageInfoFactory getDescriptorMessageInfoFactory() {
        if (Protobuf.assumeLiteRuntime) {
            return EMPTY_FACTORY;
        }
        try {
            Class<?> clazz = Class.forName("io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.DescriptorMessageInfoFactory");
            return (MessageInfoFactory)clazz.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
        }
        catch (Exception e) {
            return EMPTY_FACTORY;
        }
    }

    private static boolean useLiteRuntime(Class<?> messageType) {
        return Protobuf.assumeLiteRuntime || GeneratedMessageLite.class.isAssignableFrom(messageType);
    }

    private static class CompositeMessageInfoFactory
    implements MessageInfoFactory {
        private MessageInfoFactory[] factories;

        CompositeMessageInfoFactory(MessageInfoFactory ... factories) {
            this.factories = factories;
        }

        @Override
        public boolean isSupported(Class<?> clazz) {
            for (MessageInfoFactory factory2 : this.factories) {
                if (!factory2.isSupported(clazz)) continue;
                return true;
            }
            return false;
        }

        @Override
        public MessageInfo messageInfoFor(Class<?> clazz) {
            for (MessageInfoFactory factory2 : this.factories) {
                if (!factory2.isSupported(clazz)) continue;
                return factory2.messageInfoFor(clazz);
            }
            throw new UnsupportedOperationException("No factory is available for message type: " + clazz.getName());
        }
    }
}


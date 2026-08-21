/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Descriptors;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Internal;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ProtocolMessageEnum;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.RuntimeVersion;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.TypeProto;

public enum Syntax implements ProtocolMessageEnum
{
    SYNTAX_PROTO2(0),
    SYNTAX_PROTO3(1),
    SYNTAX_EDITIONS(2),
    UNRECOGNIZED(-1);

    public static final int SYNTAX_PROTO2_VALUE = 0;
    public static final int SYNTAX_PROTO3_VALUE = 1;
    public static final int SYNTAX_EDITIONS_VALUE = 2;
    private static final Internal.EnumLiteMap<Syntax> internalValueMap;
    private static final Syntax[] VALUES;
    private final int value;

    @Override
    public final int getNumber() {
        if (this == UNRECOGNIZED) {
            throw new IllegalArgumentException("Can't get the number of an unknown enum value.");
        }
        return this.value;
    }

    @Deprecated
    public static Syntax valueOf(int value) {
        return Syntax.forNumber(value);
    }

    public static Syntax forNumber(int value) {
        switch (value) {
            case 0: {
                return SYNTAX_PROTO2;
            }
            case 1: {
                return SYNTAX_PROTO3;
            }
            case 2: {
                return SYNTAX_EDITIONS;
            }
        }
        return null;
    }

    public static Internal.EnumLiteMap<Syntax> internalGetValueMap() {
        return internalValueMap;
    }

    @Override
    public final Descriptors.EnumValueDescriptor getValueDescriptor() {
        if (this == UNRECOGNIZED) {
            throw new IllegalStateException("Can't get the descriptor of an unrecognized enum value.");
        }
        return Syntax.getDescriptor().getValues().get(this.ordinal());
    }

    @Override
    public final Descriptors.EnumDescriptor getDescriptorForType() {
        return Syntax.getDescriptor();
    }

    public static Descriptors.EnumDescriptor getDescriptor() {
        return TypeProto.getDescriptor().getEnumTypes().get(0);
    }

    public static Syntax valueOf(Descriptors.EnumValueDescriptor desc) {
        if (desc.getType() != Syntax.getDescriptor()) {
            throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
        }
        if (desc.getIndex() == -1) {
            return UNRECOGNIZED;
        }
        return VALUES[desc.getIndex()];
    }

    private Syntax(int value) {
        this.value = value;
    }

    static {
        RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", Syntax.class.getName());
        internalValueMap = new Internal.EnumLiteMap<Syntax>(){

            @Override
            public Syntax findValueByNumber(int number) {
                return Syntax.forNumber(number);
            }
        };
        VALUES = Syntax.values();
    }
}


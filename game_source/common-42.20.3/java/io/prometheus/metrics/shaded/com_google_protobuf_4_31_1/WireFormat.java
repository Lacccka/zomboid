/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ByteString;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CodedInputStream;
import java.io.IOException;

public final class WireFormat {
    static final int FIXED32_SIZE = 4;
    static final int FIXED64_SIZE = 8;
    static final int MAX_VARINT32_SIZE = 5;
    static final int MAX_VARINT64_SIZE = 10;
    static final int MAX_VARINT_SIZE = 10;
    public static final int WIRETYPE_VARINT = 0;
    public static final int WIRETYPE_FIXED64 = 1;
    public static final int WIRETYPE_LENGTH_DELIMITED = 2;
    public static final int WIRETYPE_START_GROUP = 3;
    public static final int WIRETYPE_END_GROUP = 4;
    public static final int WIRETYPE_FIXED32 = 5;
    static final int TAG_TYPE_BITS = 3;
    static final int TAG_TYPE_MASK = 7;
    static final int MESSAGE_SET_ITEM = 1;
    static final int MESSAGE_SET_TYPE_ID = 2;
    static final int MESSAGE_SET_MESSAGE = 3;
    static final int MESSAGE_SET_ITEM_TAG = WireFormat.makeTag(1, 3);
    static final int MESSAGE_SET_ITEM_END_TAG = WireFormat.makeTag(1, 4);
    static final int MESSAGE_SET_TYPE_ID_TAG = WireFormat.makeTag(2, 0);
    static final int MESSAGE_SET_MESSAGE_TAG = WireFormat.makeTag(3, 2);

    private WireFormat() {
    }

    public static int getTagWireType(int tag) {
        return tag & 7;
    }

    public static int getTagFieldNumber(int tag) {
        return tag >>> 3;
    }

    static int makeTag(int fieldNumber, int wireType) {
        return fieldNumber << 3 | wireType;
    }

    static Object readPrimitiveField(CodedInputStream input, FieldType type, Utf8Validation utf8Validation) throws IOException {
        switch (type.ordinal()) {
            case 0: {
                return input.readDouble();
            }
            case 1: {
                return Float.valueOf(input.readFloat());
            }
            case 2: {
                return input.readInt64();
            }
            case 3: {
                return input.readUInt64();
            }
            case 4: {
                return input.readInt32();
            }
            case 5: {
                return input.readFixed64();
            }
            case 6: {
                return input.readFixed32();
            }
            case 7: {
                return input.readBool();
            }
            case 11: {
                return input.readBytes();
            }
            case 12: {
                return input.readUInt32();
            }
            case 14: {
                return input.readSFixed32();
            }
            case 15: {
                return input.readSFixed64();
            }
            case 16: {
                return input.readSInt32();
            }
            case 17: {
                return input.readSInt64();
            }
            case 8: {
                return utf8Validation.readString(input);
            }
            case 9: {
                throw new IllegalArgumentException("readPrimitiveField() cannot handle nested groups.");
            }
            case 10: {
                throw new IllegalArgumentException("readPrimitiveField() cannot handle embedded messages.");
            }
            case 13: {
                throw new IllegalArgumentException("readPrimitiveField() cannot handle enums.");
            }
        }
        throw new RuntimeException("There is no way to get here, but the compiler thinks otherwise.");
    }

    public static enum FieldType {
        DOUBLE(JavaType.DOUBLE, 1),
        FLOAT(JavaType.FLOAT, 5),
        INT64(JavaType.LONG, 0),
        UINT64(JavaType.LONG, 0),
        INT32(JavaType.INT, 0),
        FIXED64(JavaType.LONG, 1),
        FIXED32(JavaType.INT, 5),
        BOOL(JavaType.BOOLEAN, 0),
        STRING(JavaType.STRING, 2){

            @Override
            public boolean isPackable() {
                return false;
            }
        }
        ,
        GROUP(JavaType.MESSAGE, 3){

            @Override
            public boolean isPackable() {
                return false;
            }
        }
        ,
        MESSAGE(JavaType.MESSAGE, 2){

            @Override
            public boolean isPackable() {
                return false;
            }
        }
        ,
        BYTES(JavaType.BYTE_STRING, 2){

            @Override
            public boolean isPackable() {
                return false;
            }
        }
        ,
        UINT32(JavaType.INT, 0),
        ENUM(JavaType.ENUM, 0),
        SFIXED32(JavaType.INT, 5),
        SFIXED64(JavaType.LONG, 1),
        SINT32(JavaType.INT, 0),
        SINT64(JavaType.LONG, 0);

        private final JavaType javaType;
        private final int wireType;

        private FieldType(JavaType javaType, int wireType) {
            this.javaType = javaType;
            this.wireType = wireType;
        }

        public JavaType getJavaType() {
            return this.javaType;
        }

        public int getWireType() {
            return this.wireType;
        }

        public boolean isPackable() {
            return true;
        }
    }

    static enum Utf8Validation {
        LOOSE{

            @Override
            Object readString(CodedInputStream input) throws IOException {
                return input.readString();
            }
        }
        ,
        STRICT{

            @Override
            Object readString(CodedInputStream input) throws IOException {
                return input.readStringRequireUtf8();
            }
        }
        ,
        LAZY{

            @Override
            Object readString(CodedInputStream input) throws IOException {
                return input.readBytes();
            }
        };


        abstract Object readString(CodedInputStream var1) throws IOException;
    }

    public static enum JavaType {
        INT(0),
        LONG(0L),
        FLOAT(Float.valueOf(0.0f)),
        DOUBLE(0.0),
        BOOLEAN(false),
        STRING(""),
        BYTE_STRING(ByteString.EMPTY),
        ENUM(null),
        MESSAGE(null);

        private final Object defaultDefault;

        private JavaType(Object defaultDefault) {
            this.defaultDefault = defaultDefault;
        }

        Object getDefaultDefault() {
            return this.defaultDefault;
        }
    }
}


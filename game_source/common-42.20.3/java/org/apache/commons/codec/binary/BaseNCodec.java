/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec.binary;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.CodecPolicy;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.StringUtils;

public abstract class BaseNCodec
implements BinaryEncoder,
BinaryDecoder {
    static final int EOF = -1;
    public static final int MIME_CHUNK_SIZE = 76;
    public static final int PEM_CHUNK_SIZE = 64;
    private static final int DEFAULT_BUFFER_RESIZE_FACTOR = 2;
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 0x7FFFFFF7;
    protected static final int MASK_8BITS = 255;
    protected static final byte PAD_DEFAULT = 61;
    protected static final CodecPolicy DECODING_POLICY_DEFAULT = CodecPolicy.LENIENT;
    static final byte[] CHUNK_SEPARATOR = new byte[]{13, 10};
    @Deprecated
    protected final byte PAD = (byte)61;
    protected final byte pad;
    private final int unencodedBlockSize;
    private final int encodedBlockSize;
    protected final int lineLength;
    private final int chunkSeparatorLength;
    private final CodecPolicy decodingPolicy;

    private static int createPositiveCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError("Unable to allocate array size: " + ((long)minCapacity & 0xFFFFFFFFL));
        }
        return Math.max(minCapacity, 0x7FFFFFF7);
    }

    public static byte[] getChunkSeparator() {
        return (byte[])CHUNK_SEPARATOR.clone();
    }

    @Deprecated
    protected static boolean isWhiteSpace(byte byteToCheck) {
        return Character.isWhitespace(byteToCheck);
    }

    private static byte[] resizeBuffer(Context context, int minCapacity) {
        int oldCapacity = context.buffer.length;
        int newCapacity = oldCapacity * 2;
        if (Integer.compareUnsigned(newCapacity, minCapacity) < 0) {
            newCapacity = minCapacity;
        }
        if (Integer.compareUnsigned(newCapacity, 0x7FFFFFF7) > 0) {
            newCapacity = BaseNCodec.createPositiveCapacity(minCapacity);
        }
        byte[] b = Arrays.copyOf(context.buffer, newCapacity);
        context.buffer = b;
        return b;
    }

    static int toLength(byte[] array) {
        return array == null ? 0 : array.length;
    }

    protected BaseNCodec(int unencodedBlockSize, int encodedBlockSize, int lineLength, int chunkSeparatorLength) {
        this(unencodedBlockSize, encodedBlockSize, lineLength, chunkSeparatorLength, 61);
    }

    protected BaseNCodec(int unencodedBlockSize, int encodedBlockSize, int lineLength, int chunkSeparatorLength, byte pad) {
        this(unencodedBlockSize, encodedBlockSize, lineLength, chunkSeparatorLength, pad, DECODING_POLICY_DEFAULT);
    }

    protected BaseNCodec(int unencodedBlockSize, int encodedBlockSize, int lineLength, int chunkSeparatorLength, byte pad, CodecPolicy decodingPolicy) {
        this.unencodedBlockSize = unencodedBlockSize;
        this.encodedBlockSize = encodedBlockSize;
        boolean useChunking = lineLength > 0 && chunkSeparatorLength > 0;
        this.lineLength = useChunking ? lineLength / encodedBlockSize * encodedBlockSize : 0;
        this.chunkSeparatorLength = chunkSeparatorLength;
        this.pad = pad;
        this.decodingPolicy = Objects.requireNonNull(decodingPolicy, "codecPolicy");
    }

    int available(Context context) {
        return this.hasData(context) ? context.pos - context.readPos : 0;
    }

    protected boolean containsAlphabetOrPad(byte[] arrayOctet) {
        if (arrayOctet != null) {
            for (byte element : arrayOctet) {
                if (this.pad != element && !this.isInAlphabet(element)) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public byte[] decode(byte[] pArray) {
        if (BinaryCodec.isEmpty(pArray)) {
            return pArray;
        }
        Context context = new Context();
        this.decode(pArray, 0, pArray.length, context);
        this.decode(pArray, 0, -1, context);
        byte[] result = new byte[context.pos];
        this.readResults(result, 0, result.length, context);
        return result;
    }

    abstract void decode(byte[] var1, int var2, int var3, Context var4);

    @Override
    public Object decode(Object obj) throws DecoderException {
        if (obj instanceof byte[]) {
            return this.decode((byte[])obj);
        }
        if (obj instanceof String) {
            return this.decode((String)obj);
        }
        throw new DecoderException("Parameter supplied to Base-N decode is not a byte[] or a String");
    }

    public byte[] decode(String pArray) {
        return this.decode(StringUtils.getBytesUtf8(pArray));
    }

    @Override
    public byte[] encode(byte[] pArray) {
        if (BinaryCodec.isEmpty(pArray)) {
            return pArray;
        }
        return this.encode(pArray, 0, pArray.length);
    }

    public byte[] encode(byte[] pArray, int offset, int length) {
        if (BinaryCodec.isEmpty(pArray)) {
            return pArray;
        }
        Context context = new Context();
        this.encode(pArray, offset, length, context);
        this.encode(pArray, offset, -1, context);
        byte[] buf = new byte[context.pos - context.readPos];
        this.readResults(buf, 0, buf.length, context);
        return buf;
    }

    abstract void encode(byte[] var1, int var2, int var3, Context var4);

    @Override
    public Object encode(Object obj) throws EncoderException {
        if (!(obj instanceof byte[])) {
            throw new EncoderException("Parameter supplied to Base-N encode is not a byte[]");
        }
        return this.encode((byte[])obj);
    }

    public String encodeAsString(byte[] pArray) {
        return StringUtils.newStringUtf8(this.encode(pArray));
    }

    public String encodeToString(byte[] pArray) {
        return StringUtils.newStringUtf8(this.encode(pArray));
    }

    protected byte[] ensureBufferSize(int size, Context context) {
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, this.getDefaultBufferSize())];
            context.pos = 0;
            context.readPos = 0;
        } else if (context.pos + size - context.buffer.length > 0) {
            return BaseNCodec.resizeBuffer(context, context.pos + size);
        }
        return context.buffer;
    }

    public CodecPolicy getCodecPolicy() {
        return this.decodingPolicy;
    }

    protected int getDefaultBufferSize() {
        return 8192;
    }

    public long getEncodedLength(byte[] pArray) {
        long len = (long)((pArray.length + this.unencodedBlockSize - 1) / this.unencodedBlockSize) * (long)this.encodedBlockSize;
        if (this.lineLength > 0) {
            len += (len + (long)this.lineLength - 1L) / (long)this.lineLength * (long)this.chunkSeparatorLength;
        }
        return len;
    }

    boolean hasData(Context context) {
        return context.pos > context.readPos;
    }

    protected abstract boolean isInAlphabet(byte var1);

    public boolean isInAlphabet(byte[] arrayOctet, boolean allowWSPad) {
        for (byte octet : arrayOctet) {
            if (this.isInAlphabet(octet) || allowWSPad && (octet == this.pad || Character.isWhitespace(octet))) continue;
            return false;
        }
        return true;
    }

    public boolean isInAlphabet(String basen) {
        return this.isInAlphabet(StringUtils.getBytesUtf8(basen), true);
    }

    public boolean isStrictDecoding() {
        return this.decodingPolicy == CodecPolicy.STRICT;
    }

    int readResults(byte[] b, int bPos, int bAvail, Context context) {
        if (this.hasData(context)) {
            int len = Math.min(this.available(context), bAvail);
            System.arraycopy(context.buffer, context.readPos, b, bPos, len);
            context.readPos += len;
            if (!this.hasData(context)) {
                context.readPos = 0;
                context.pos = 0;
            }
            return len;
        }
        return context.eof ? -1 : 0;
    }

    static class Context {
        int ibitWorkArea;
        long lbitWorkArea;
        byte[] buffer;
        int pos;
        int readPos;
        boolean eof;
        int currentLinePos;
        int modulus;

        Context() {
        }

        public String toString() {
            return String.format("%s[buffer=%s, currentLinePos=%s, eof=%s, ibitWorkArea=%s, lbitWorkArea=%s, modulus=%s, pos=%s, readPos=%s]", this.getClass().getSimpleName(), Arrays.toString(this.buffer), this.currentLinePos, this.eof, this.ibitWorkArea, this.lbitWorkArea, this.modulus, this.pos, this.readPos);
        }
    }

    public static abstract class AbstractBuilder<T, B extends AbstractBuilder<T, B>>
    implements Supplier<T> {
        private CodecPolicy decodingPolicy = DECODING_POLICY_DEFAULT;
        private int lineLength;
        private byte[] lineSeparator = CHUNK_SEPARATOR;
        private final byte[] defaultEncodeTable;
        private byte[] encodeTable;
        private byte padding = (byte)61;

        AbstractBuilder(byte[] defaultEncodeTable) {
            this.defaultEncodeTable = defaultEncodeTable;
            this.encodeTable = defaultEncodeTable;
        }

        B asThis() {
            return (B)this;
        }

        CodecPolicy getDecodingPolicy() {
            return this.decodingPolicy;
        }

        byte[] getEncodeTable() {
            return this.encodeTable;
        }

        int getLineLength() {
            return this.lineLength;
        }

        byte[] getLineSeparator() {
            return this.lineSeparator;
        }

        byte getPadding() {
            return this.padding;
        }

        public B setDecodingPolicy(CodecPolicy decodingPolicy) {
            this.decodingPolicy = decodingPolicy != null ? decodingPolicy : DECODING_POLICY_DEFAULT;
            return this.asThis();
        }

        public B setEncodeTable(byte ... encodeTable) {
            this.encodeTable = encodeTable != null ? encodeTable : this.defaultEncodeTable;
            return this.asThis();
        }

        public B setLineLength(int lineLength) {
            this.lineLength = Math.max(0, lineLength);
            return this.asThis();
        }

        public B setLineSeparator(byte ... lineSeparator) {
            this.lineSeparator = lineSeparator != null ? lineSeparator : CHUNK_SEPARATOR;
            return this.asThis();
        }

        public B setPadding(byte padding) {
            this.padding = padding;
            return this.asThis();
        }
    }
}


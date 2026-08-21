/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.compress.compressors.gzip.GzipUtils;

public class GzipCompressorOutputStream
extends CompressorOutputStream<OutputStream> {
    private static final int FNAME = 8;
    private static final int FCOMMENT = 16;
    private final Deflater deflater;
    private final byte[] deflateBuffer;
    private boolean closed;
    private final CRC32 crc = new CRC32();

    public GzipCompressorOutputStream(OutputStream out) throws IOException {
        this(out, new GzipParameters());
    }

    public GzipCompressorOutputStream(OutputStream out, GzipParameters parameters) throws IOException {
        super(out);
        this.deflater = new Deflater(parameters.getCompressionLevel(), true);
        this.deflater.setStrategy(parameters.getDeflateStrategy());
        this.deflateBuffer = new byte[parameters.getBufferSize()];
        this.writeHeader(parameters);
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            try {
                this.finish();
            }
            finally {
                this.deflater.end();
                this.out.close();
                this.closed = true;
            }
        }
    }

    private void deflate() throws IOException {
        int length = this.deflater.deflate(this.deflateBuffer, 0, this.deflateBuffer.length);
        if (length > 0) {
            this.out.write(this.deflateBuffer, 0, length);
        }
    }

    public void finish() throws IOException {
        if (!this.deflater.finished()) {
            this.deflater.finish();
            while (!this.deflater.finished()) {
                this.deflate();
            }
            this.writeTrailer();
        }
    }

    private byte[] getBytes(String string) throws IOException {
        if (GzipUtils.GZIP_ENCODING.newEncoder().canEncode(string)) {
            return string.getBytes(GzipUtils.GZIP_ENCODING);
        }
        try {
            return new URI(null, null, string, null).toASCIIString().getBytes(StandardCharsets.US_ASCII);
        }
        catch (URISyntaxException e) {
            throw new IOException(string, e);
        }
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        this.write(buffer, 0, buffer.length);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        if (this.deflater.finished()) {
            throw new IOException("Cannot write more data, the end of the compressed data stream has been reached");
        }
        if (length > 0) {
            this.deflater.setInput(buffer, offset, length);
            while (!this.deflater.needsInput()) {
                this.deflate();
            }
            this.crc.update(buffer, offset, length);
        }
    }

    @Override
    public void write(int b) throws IOException {
        this.write(new byte[]{(byte)(b & 0xFF)}, 0, 1);
    }

    private void writeHeader(GzipParameters parameters) throws IOException {
        String fileName = parameters.getFileName();
        String comment = parameters.getComment();
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)-29921);
        buffer.put((byte)8);
        buffer.put((byte)((fileName != null ? 8 : 0) | (comment != null ? 16 : 0)));
        buffer.putInt((int)(parameters.getModificationTime() / 1000L));
        int compressionLevel = parameters.getCompressionLevel();
        if (compressionLevel == 9) {
            buffer.put((byte)2);
        } else if (compressionLevel == 1) {
            buffer.put((byte)4);
        } else {
            buffer.put((byte)0);
        }
        buffer.put((byte)parameters.getOperatingSystem());
        this.out.write(buffer.array());
        if (fileName != null) {
            this.out.write(this.getBytes(fileName));
            this.out.write(0);
        }
        if (comment != null) {
            this.out.write(this.getBytes(comment));
            this.out.write(0);
        }
    }

    private void writeTrailer() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt((int)this.crc.getValue());
        buffer.putInt(this.deflater.getTotalIn());
        this.out.write(buffer.array());
    }
}


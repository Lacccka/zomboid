/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import org.apache.commons.io.FileUtils;

public final class IOUtils {
    public static final LinkOption[] EMPTY_LINK_OPTIONS = new LinkOption[0];

    @Deprecated
    public static void closeQuietly(Closeable c) {
        org.apache.commons.io.IOUtils.closeQuietly(c);
    }

    @Deprecated
    public static void copy(File sourceFile, OutputStream outputStream2) throws IOException {
        FileUtils.copyFile(sourceFile, outputStream2);
    }

    @Deprecated
    public static long copy(InputStream input, OutputStream output) throws IOException {
        return org.apache.commons.io.IOUtils.copy(input, output);
    }

    @Deprecated
    public static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        return org.apache.commons.io.IOUtils.copy(input, output, bufferSize);
    }

    @Deprecated
    public static long copyRange(InputStream input, long len, OutputStream output) throws IOException {
        return org.apache.commons.io.IOUtils.copyLarge(input, output, 0L, len);
    }

    @Deprecated
    public static long copyRange(InputStream input, long length, OutputStream output, int bufferSize) throws IOException {
        long count;
        if (bufferSize < 1) {
            throw new IllegalArgumentException("bufferSize must be bigger than 0");
        }
        byte[] buffer = new byte[(int)Math.min((long)bufferSize, Math.max(0L, length))];
        int n = 0;
        for (count = 0L; count < length && -1 != (n = input.read(buffer, 0, (int)Math.min(length - count, (long)buffer.length))); count += (long)n) {
            if (output == null) continue;
            output.write(buffer, 0, n);
        }
        return count;
    }

    @Deprecated
    public static int read(File file, byte[] array) throws IOException {
        try (InputStream inputStream2 = Files.newInputStream(file.toPath(), new OpenOption[0]);){
            int n = IOUtils.readFully(inputStream2, array, 0, array.length);
            return n;
        }
    }

    public static int readFully(InputStream input, byte[] array) throws IOException {
        return IOUtils.readFully(input, array, 0, array.length);
    }

    public static int readFully(InputStream input, byte[] array, int offset, int length) throws IOException {
        if (length < 0 || offset < 0 || length + offset > array.length || length + offset < 0) {
            throw new IndexOutOfBoundsException();
        }
        return org.apache.commons.io.IOUtils.read(input, array, offset, length);
    }

    public static void readFully(ReadableByteChannel channel, ByteBuffer byteBuffer) throws IOException {
        int expectedLength = byteBuffer.remaining();
        int read = org.apache.commons.io.IOUtils.read(channel, byteBuffer);
        if (read < expectedLength) {
            throw new EOFException();
        }
    }

    public static byte[] readRange(InputStream input, int length) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        org.apache.commons.io.IOUtils.copyLarge(input, output, 0L, (long)length);
        return output.toByteArray();
    }

    public static byte[] readRange(ReadableByteChannel input, int length) throws IOException {
        int readCount;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteBuffer b = ByteBuffer.allocate(Math.min(length, 8192));
        for (int read = 0; read < length; read += readCount) {
            b.limit(Math.min(length - read, b.capacity()));
            readCount = input.read(b);
            if (readCount <= 0) break;
            output.write(b.array(), 0, readCount);
            b.rewind();
        }
        return output.toByteArray();
    }

    public static long skip(InputStream input, long toSkip) throws IOException {
        return org.apache.commons.io.IOUtils.skip(input, toSkip, org.apache.commons.io.IOUtils::byteArray);
    }

    @Deprecated
    public static byte[] toByteArray(InputStream input) throws IOException {
        return org.apache.commons.io.IOUtils.toByteArray(input);
    }

    private IOUtils() {
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.util.io.StreamOverflowException;

public final class Streams {
    private static int BUFFER_SIZE = 4096;

    public static void drain(InputStream inputStream2) throws IOException {
        byte[] byArray = new byte[BUFFER_SIZE];
        while (inputStream2.read(byArray, 0, byArray.length) >= 0) {
        }
    }

    public static byte[] readAll(InputStream inputStream2) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Streams.pipeAll(inputStream2, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] readAllLimited(InputStream inputStream2, int n) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Streams.pipeAllLimited(inputStream2, n, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static int readFully(InputStream inputStream2, byte[] byArray) throws IOException {
        return Streams.readFully(inputStream2, byArray, 0, byArray.length);
    }

    public static int readFully(InputStream inputStream2, byte[] byArray, int n, int n2) throws IOException {
        int n3;
        int n4;
        for (n3 = 0; n3 < n2 && (n4 = inputStream2.read(byArray, n + n3, n2 - n3)) >= 0; n3 += n4) {
        }
        return n3;
    }

    public static void pipeAll(InputStream inputStream2, OutputStream outputStream2) throws IOException {
        int n;
        byte[] byArray = new byte[BUFFER_SIZE];
        while ((n = inputStream2.read(byArray, 0, byArray.length)) >= 0) {
            outputStream2.write(byArray, 0, n);
        }
    }

    public static long pipeAllLimited(InputStream inputStream2, long l, OutputStream outputStream2) throws IOException {
        int n;
        long l2 = 0L;
        byte[] byArray = new byte[BUFFER_SIZE];
        while ((n = inputStream2.read(byArray, 0, byArray.length)) >= 0) {
            if (l - l2 < (long)n) {
                throw new StreamOverflowException("Data Overflow");
            }
            l2 += (long)n;
            outputStream2.write(byArray, 0, n);
        }
        return l2;
    }

    public static void writeBufTo(ByteArrayOutputStream byteArrayOutputStream, OutputStream outputStream2) throws IOException {
        byteArrayOutputStream.writeTo(outputStream2);
    }
}


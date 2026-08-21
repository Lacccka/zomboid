/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.bsd;

import com.sun.jna.Native;
import com.sun.jna.platform.bsd.ExtAttr;
import com.sun.jna.platform.unix.LibCAPI;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExtAttrUtil {
    public static List<String> list(String path) throws IOException {
        long bufferLength = ExtAttr.INSTANCE.extattr_list_file(path, 1, null, new LibCAPI.size_t(0L)).longValue();
        if (bufferLength < 0L) {
            throw new IOException("errno: " + Native.getLastError());
        }
        if (bufferLength == 0L) {
            return Collections.emptyList();
        }
        ByteBuffer buffer = ByteBuffer.allocate((int)bufferLength);
        long valueLength = ExtAttr.INSTANCE.extattr_list_file(path, 1, buffer, new LibCAPI.size_t(bufferLength)).longValue();
        if (valueLength < 0L) {
            throw new IOException("errno: " + Native.getLastError());
        }
        return ExtAttrUtil.decodeStringList(buffer);
    }

    public static ByteBuffer get(String path, String name) throws IOException {
        long bufferLength = ExtAttr.INSTANCE.extattr_get_file(path, 1, name, null, new LibCAPI.size_t(0L)).longValue();
        if (bufferLength < 0L) {
            throw new IOException("errno: " + Native.getLastError());
        }
        if (bufferLength == 0L) {
            return ByteBuffer.allocate(0);
        }
        ByteBuffer buffer = ByteBuffer.allocate((int)bufferLength);
        long valueLength = ExtAttr.INSTANCE.extattr_get_file(path, 1, name, buffer, new LibCAPI.size_t(bufferLength)).longValue();
        if (valueLength < 0L) {
            throw new IOException("errno: " + Native.getLastError());
        }
        return buffer;
    }

    public static void set(String path, String name, ByteBuffer value) throws IOException {
        long r = ExtAttr.INSTANCE.extattr_set_file(path, 1, name, value, new LibCAPI.size_t((long)value.remaining())).longValue();
        if (r < 0L) {
            throw new IOException("errno: " + Native.getLastError());
        }
    }

    public static void delete(String path, String name) throws IOException {
        int r = ExtAttr.INSTANCE.extattr_delete_file(path, 1, name);
        if (r < 0) {
            throw new IOException("errno: " + Native.getLastError());
        }
    }

    private static List<String> decodeStringList(ByteBuffer buffer) {
        ArrayList<String> list = new ArrayList<String>();
        while (buffer.hasRemaining()) {
            int length = buffer.get() & 0xFF;
            byte[] value = new byte[length];
            buffer.get(value);
            try {
                list.add(new String(value, "UTF-8"));
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }
}


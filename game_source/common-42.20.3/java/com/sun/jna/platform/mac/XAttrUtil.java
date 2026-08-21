/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.mac;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.mac.XAttr;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class XAttrUtil {
    public static List<String> listXAttr(String path) {
        long bufferLength = XAttr.INSTANCE.listxattr(path, null, 0L, 0);
        if (bufferLength < 0L) {
            return null;
        }
        if (bufferLength == 0L) {
            return new ArrayList<String>(0);
        }
        Memory valueBuffer = new Memory(bufferLength);
        long valueLength = XAttr.INSTANCE.listxattr(path, valueBuffer, bufferLength, 0);
        if (valueLength < 0L) {
            return null;
        }
        return XAttrUtil.decodeStringSequence(valueBuffer.getByteBuffer(0L, valueLength));
    }

    public static String getXAttr(String path, String name) {
        long bufferLength = XAttr.INSTANCE.getxattr(path, name, null, 0L, 0, 0);
        if (bufferLength < 0L) {
            return null;
        }
        if (bufferLength == 0L) {
            return "";
        }
        Memory valueBuffer = new Memory(bufferLength);
        valueBuffer.clear();
        long valueLength = XAttr.INSTANCE.getxattr(path, name, valueBuffer, bufferLength, 0, 0);
        if (valueLength < 0L) {
            return null;
        }
        return Native.toString(valueBuffer.getByteArray(0L, (int)bufferLength), "UTF-8");
    }

    public static int setXAttr(String path, String name, String value) {
        Memory valueBuffer = XAttrUtil.encodeString(value);
        return XAttr.INSTANCE.setxattr(path, name, valueBuffer, valueBuffer.size(), 0, 0);
    }

    public static int removeXAttr(String path, String name) {
        return XAttr.INSTANCE.removexattr(path, name, 0);
    }

    protected static Memory encodeString(String s) {
        byte[] bb = s.getBytes(Charset.forName("UTF-8"));
        Memory valueBuffer = new Memory(bb.length);
        valueBuffer.write(0L, bb, 0, bb.length);
        return valueBuffer;
    }

    protected static String decodeString(ByteBuffer bb) {
        return Charset.forName("UTF-8").decode(bb).toString();
    }

    protected static List<String> decodeStringSequence(ByteBuffer bb) {
        ArrayList<String> names = new ArrayList<String>();
        bb.mark();
        while (bb.hasRemaining()) {
            if (bb.get() != 0) continue;
            ByteBuffer nameBuffer = (ByteBuffer)bb.duplicate().limit(bb.position() - 1).reset();
            if (nameBuffer.hasRemaining()) {
                names.add(XAttrUtil.decodeString(nameBuffer));
            }
            bb.mark();
        }
        return names;
    }
}


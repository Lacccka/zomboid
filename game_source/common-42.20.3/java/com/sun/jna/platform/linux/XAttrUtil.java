/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.linux;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.linux.XAttr;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedHashSet;

public abstract class XAttrUtil {
    private XAttrUtil() {
    }

    public static void setXAttr(String path, String name, String value) throws IOException {
        XAttrUtil.setXAttr(path, name, value, Native.getDefaultStringEncoding());
    }

    public static void setXAttr(String path, String name, String value, String encoding) throws IOException {
        XAttrUtil.setXAttr(path, name, value.getBytes(encoding));
    }

    public static void setXAttr(String path, String name, byte[] value) throws IOException {
        int retval = XAttr.INSTANCE.setxattr(path, name, value, new XAttr.size_t((long)value.length), 0);
        if (retval != 0) {
            int eno = Native.getLastError();
            throw new IOException("errno: " + eno);
        }
    }

    public static void lSetXAttr(String path, String name, String value) throws IOException {
        XAttrUtil.lSetXAttr(path, name, value, Native.getDefaultStringEncoding());
    }

    public static void lSetXAttr(String path, String name, String value, String encoding) throws IOException {
        XAttrUtil.lSetXAttr(path, name, value.getBytes(encoding));
    }

    public static void lSetXAttr(String path, String name, byte[] value) throws IOException {
        int retval = XAttr.INSTANCE.lsetxattr(path, name, value, new XAttr.size_t((long)value.length), 0);
        if (retval != 0) {
            int eno = Native.getLastError();
            throw new IOException("errno: " + eno);
        }
    }

    public static void fSetXAttr(int fd, String name, String value) throws IOException {
        XAttrUtil.fSetXAttr(fd, name, value, Native.getDefaultStringEncoding());
    }

    public static void fSetXAttr(int fd, String name, String value, String encoding) throws IOException {
        XAttrUtil.fSetXAttr(fd, name, value.getBytes(encoding));
    }

    public static void fSetXAttr(int fd, String name, byte[] value) throws IOException {
        int retval = XAttr.INSTANCE.fsetxattr(fd, name, value, new XAttr.size_t((long)value.length), 0);
        if (retval != 0) {
            int eno = Native.getLastError();
            throw new IOException("errno: " + eno);
        }
    }

    public static String getXAttr(String path, String name) throws IOException {
        return XAttrUtil.getXAttr(path, name, Native.getDefaultStringEncoding());
    }

    public static String getXAttr(String path, String name, String encoding) throws IOException {
        byte[] valueMem = XAttrUtil.getXAttrBytes(path, name);
        return new String(valueMem, Charset.forName(encoding));
    }

    public static byte[] getXAttrBytes(String path, String name) throws IOException {
        byte[] valueMem;
        XAttr.ssize_t retval;
        int eno = 0;
        do {
            if ((retval = XAttr.INSTANCE.getxattr(path, name, (byte[])null, XAttr.size_t.ZERO)).longValue() < 0L) {
                eno = Native.getLastError();
                throw new IOException("errno: " + eno);
            }
            valueMem = new byte[retval.intValue()];
            if ((retval = XAttr.INSTANCE.getxattr(path, name, valueMem, new XAttr.size_t((long)valueMem.length))).longValue() >= 0L || (eno = Native.getLastError()) == 34) continue;
            throw new IOException("errno: " + eno);
        } while (retval.longValue() < 0L && eno == 34);
        return valueMem;
    }

    public static Memory getXAttrAsMemory(String path, String name) throws IOException {
        Memory valueMem;
        XAttr.ssize_t retval;
        int eno = 0;
        do {
            if ((retval = XAttr.INSTANCE.getxattr(path, name, (Pointer)null, XAttr.size_t.ZERO)).longValue() < 0L) {
                eno = Native.getLastError();
                throw new IOException("errno: " + eno);
            }
            if (retval.longValue() == 0L) {
                return null;
            }
            valueMem = new Memory(retval.longValue());
            if ((retval = XAttr.INSTANCE.getxattr(path, name, valueMem, new XAttr.size_t(valueMem.size()))).longValue() >= 0L || (eno = Native.getLastError()) == 34) continue;
            throw new IOException("errno: " + eno);
        } while (retval.longValue() < 0L && eno == 34);
        return valueMem;
    }

    public static String lGetXAttr(String path, String name) throws IOException {
        return XAttrUtil.lGetXAttr(path, name, Native.getDefaultStringEncoding());
    }

    public static String lGetXAttr(String path, String name, String encoding) throws IOException {
        byte[] valueMem = XAttrUtil.lGetXAttrBytes(path, name);
        return new String(valueMem, Charset.forName(encoding));
    }

    public static byte[] lGetXAttrBytes(String path, String name) throws IOException {
        byte[] valueMem;
        XAttr.ssize_t retval;
        int eno = 0;
        do {
            if ((retval = XAttr.INSTANCE.lgetxattr(path, name, (byte[])null, XAttr.size_t.ZERO)).longValue() < 0L) {
                eno = Native.getLastError();
                throw new IOException("errno: " + eno);
            }
            valueMem = new byte[retval.intValue()];
            if ((retval = XAttr.INSTANCE.lgetxattr(path, name, valueMem, new XAttr.size_t((long)valueMem.length))).longValue() >= 0L || (eno = Native.getLastError()) == 34) continue;
            throw new IOException("errno: " + eno);
        } while (retval.longValue() < 0L && eno == 34);
        return valueMem;
    }

    public static Memory lGetXAttrAsMemory(String path, String name) throws IOException {
        Memory valueMem;
        XAttr.ssize_t retval;
        int eno = 0;
        do {
            if ((retval = XAttr.INSTANCE.lgetxattr(path, name, (Pointer)null, XAttr.size_t.ZERO)).longValue() < 0L) {
                eno = Native.getLastError();
                throw new IOException("errno: " + eno);
            }
            if (retval.longValue() == 0L) {
                return null;
            }
            valueMem = new Memory(retval.longValue());
            if ((retval = XAttr.INSTANCE.lgetxattr(path, name, valueMem, new XAttr.size_t(valueMem.size()))).longValue() >= 0L || (eno = Native.getLastError()) == 34) continue;
            throw new IOException("errno: " + eno);
        } while (retval.longValue() < 0L && eno == 34);
        return valueMem;
    }

    public static String fGetXAttr(int fd, String name) throws IOException {
        return XAttrUtil.fGetXAttr(fd, name, Native.getDefaultStringEncoding());
    }

    public static String fGetXAttr(int fd, String name, String encoding) throws IOException {
        byte[] valueMem = XAttrUtil.fGetXAttrBytes(fd, name);
        return new String(valueMem, Charset.forName(encoding));
    }

    public static byte[] fGetXAttrBytes(int fd, String name) throws IOException {
        byte[] valueMem;
        XAttr.ssize_t retval;
        int eno = 0;
        do {
            if ((retval = XAttr.INSTANCE.fgetxattr(fd, name, (byte[])null, XAttr.size_t.ZERO)).longValue() < 0L) {
                eno = Native.getLastError();
                throw new IOException("errno: " + eno);
            }
            valueMem = new byte[retval.intValue()];
            if ((retval = XAttr.INSTANCE.fgetxattr(fd, name, valueMem, new XAttr.size_t((long)valueMem.length))).longValue() >= 0L || (eno = Native.getLastError()) == 34) continue;
            throw new IOException("errno: " + eno);
        } while (retval.longValue() < 0L && eno == 34);
        return valueMem;
    }

    public static Memory fGetXAttrAsMemory(int fd, String name) throws IOException {
        Memory valueMem;
        XAttr.ssize_t retval;
        int eno = 0;
        do {
            if ((retval = XAttr.INSTANCE.fgetxattr(fd, name, (Pointer)null, XAttr.size_t.ZERO)).longValue() < 0L) {
                eno = Native.getLastError();
                throw new IOException("errno: " + eno);
            }
            if (retval.longValue() == 0L) {
                return null;
            }
            valueMem = new Memory(retval.longValue());
            if ((retval = XAttr.INSTANCE.fgetxattr(fd, name, valueMem, new XAttr.size_t(valueMem.size()))).longValue() >= 0L || (eno = Native.getLastError()) == 34) continue;
            throw new IOException("errno: " + eno);
        } while (retval.longValue() < 0L && eno == 34);
        return valueMem;
    }

    public static Collection<String> listXAttr(String path) throws IOException {
        return XAttrUtil.listXAttr(path, Native.getDefaultStringEncoding());
    }

    public static Collection<String> listXAttr(String path, String encoding) throws IOException {
        byte[] listMem;
        XAttr.ssize_t retval;
        int eno = 0;
        do {
            if ((retval = XAttr.INSTANCE.listxattr(path, (byte[])null, XAttr.size_t.ZERO)).longValue() < 0L) {
                eno = Native.getLastError();
                throw new IOException("errno: " + eno);
            }
            listMem = new byte[retval.intValue()];
            if ((retval = XAttr.INSTANCE.listxattr(path, listMem, new XAttr.size_t((long)listMem.length))).longValue() >= 0L || (eno = Native.getLastError()) == 34) continue;
            throw new IOException("errno: " + eno);
        } while (retval.longValue() < 0L && eno == 34);
        return XAttrUtil.splitBufferToStrings(listMem, encoding);
    }

    public static Collection<String> lListXAttr(String path) throws IOException {
        return XAttrUtil.lListXAttr(path, Native.getDefaultStringEncoding());
    }

    public static Collection<String> lListXAttr(String path, String encoding) throws IOException {
        byte[] listMem;
        XAttr.ssize_t retval;
        int eno = 0;
        do {
            if ((retval = XAttr.INSTANCE.llistxattr(path, (byte[])null, XAttr.size_t.ZERO)).longValue() < 0L) {
                eno = Native.getLastError();
                throw new IOException("errno: " + eno);
            }
            listMem = new byte[retval.intValue()];
            if ((retval = XAttr.INSTANCE.llistxattr(path, listMem, new XAttr.size_t((long)listMem.length))).longValue() >= 0L || (eno = Native.getLastError()) == 34) continue;
            throw new IOException("errno: " + eno);
        } while (retval.longValue() < 0L && eno == 34);
        return XAttrUtil.splitBufferToStrings(listMem, encoding);
    }

    public static Collection<String> fListXAttr(int fd) throws IOException {
        return XAttrUtil.fListXAttr(fd, Native.getDefaultStringEncoding());
    }

    public static Collection<String> fListXAttr(int fd, String encoding) throws IOException {
        byte[] listMem;
        XAttr.ssize_t retval;
        int eno = 0;
        do {
            if ((retval = XAttr.INSTANCE.flistxattr(fd, (byte[])null, XAttr.size_t.ZERO)).longValue() < 0L) {
                eno = Native.getLastError();
                throw new IOException("errno: " + eno);
            }
            listMem = new byte[retval.intValue()];
            if ((retval = XAttr.INSTANCE.flistxattr(fd, listMem, new XAttr.size_t((long)listMem.length))).longValue() >= 0L || (eno = Native.getLastError()) == 34) continue;
            throw new IOException("errno: " + eno);
        } while (retval.longValue() < 0L && eno == 34);
        return XAttrUtil.splitBufferToStrings(listMem, encoding);
    }

    public static void removeXAttr(String path, String name) throws IOException {
        int retval = XAttr.INSTANCE.removexattr(path, name);
        if (retval != 0) {
            int eno = Native.getLastError();
            throw new IOException("errno: " + eno);
        }
    }

    public static void lRemoveXAttr(String path, String name) throws IOException {
        int retval = XAttr.INSTANCE.lremovexattr(path, name);
        if (retval != 0) {
            int eno = Native.getLastError();
            throw new IOException("errno: " + eno);
        }
    }

    public static void fRemoveXAttr(int fd, String name) throws IOException {
        int retval = XAttr.INSTANCE.fremovexattr(fd, name);
        if (retval != 0) {
            int eno = Native.getLastError();
            throw new IOException("errno: " + eno);
        }
    }

    private static Collection<String> splitBufferToStrings(byte[] valueMem, String encoding) throws IOException {
        Charset charset = Charset.forName(encoding);
        LinkedHashSet<String> attributesList = new LinkedHashSet<String>(1);
        int offset = 0;
        for (int i = 0; i < valueMem.length; ++i) {
            if (valueMem[i] != 0) continue;
            String name = new String(valueMem, offset, i - offset, charset);
            attributesList.add(name);
            offset = i + 1;
        }
        return attributesList;
    }
}


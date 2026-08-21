/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.linux;

import com.sun.jna.IntegerType;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface XAttr
extends Library {
    public static final XAttr INSTANCE = Native.load(XAttr.class);
    public static final int XATTR_CREATE = 1;
    public static final int XATTR_REPLACE = 2;
    public static final int EPERM = 1;
    public static final int E2BIG = 7;
    public static final int EEXIST = 17;
    public static final int ENOSPC = 28;
    public static final int ERANGE = 34;
    public static final int ENODATA = 61;
    public static final int ENOATTR = 61;
    public static final int ENOTSUP = 95;
    public static final int EDQUOT = 122;

    public int setxattr(String var1, String var2, Pointer var3, size_t var4, int var5);

    public int setxattr(String var1, String var2, byte[] var3, size_t var4, int var5);

    public int lsetxattr(String var1, String var2, Pointer var3, size_t var4, int var5);

    public int lsetxattr(String var1, String var2, byte[] var3, size_t var4, int var5);

    public int fsetxattr(int var1, String var2, Pointer var3, size_t var4, int var5);

    public int fsetxattr(int var1, String var2, byte[] var3, size_t var4, int var5);

    public ssize_t getxattr(String var1, String var2, Pointer var3, size_t var4);

    public ssize_t getxattr(String var1, String var2, byte[] var3, size_t var4);

    public ssize_t lgetxattr(String var1, String var2, Pointer var3, size_t var4);

    public ssize_t lgetxattr(String var1, String var2, byte[] var3, size_t var4);

    public ssize_t fgetxattr(int var1, String var2, Pointer var3, size_t var4);

    public ssize_t fgetxattr(int var1, String var2, byte[] var3, size_t var4);

    public ssize_t listxattr(String var1, Pointer var2, size_t var3);

    public ssize_t listxattr(String var1, byte[] var2, size_t var3);

    public ssize_t llistxattr(String var1, Pointer var2, size_t var3);

    public ssize_t llistxattr(String var1, byte[] var2, size_t var3);

    public ssize_t flistxattr(int var1, Pointer var2, size_t var3);

    public ssize_t flistxattr(int var1, byte[] var2, size_t var3);

    public int removexattr(String var1, String var2);

    public int lremovexattr(String var1, String var2);

    public int fremovexattr(int var1, String var2);

    public static class ssize_t
    extends IntegerType {
        public static final ssize_t ZERO = new ssize_t();
        private static final long serialVersionUID = 1L;

        public ssize_t() {
            this(0L);
        }

        public ssize_t(long value) {
            super(Native.SIZE_T_SIZE, value, false);
        }
    }

    public static class size_t
    extends IntegerType {
        public static final size_t ZERO = new size_t();
        private static final long serialVersionUID = 1L;

        public size_t() {
            this(0L);
        }

        public size_t(long value) {
            super(Native.SIZE_T_SIZE, value, true);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.linux;

public interface Fcntl {
    public static final int O_RDONLY = 0;
    public static final int O_WRONLY = 1;
    public static final int O_RDWR = 2;
    public static final int O_CREAT = 64;
    public static final int O_EXCL = 128;
    public static final int O_TRUNC = 512;
    public static final int O_APPEND = 1024;
    public static final int O_NONBLOCK = 2048;
    public static final int O_DSYNC = 4096;
    public static final int O_FASYNC = 8192;
    public static final int O_DIRECT = 16384;
    public static final int O_LARGEFILE = 32768;
    public static final int O_DIRECTORY = 65536;
    public static final int O_NOFOLLOW = 131072;
    public static final int O_NOATIME = 262144;
    public static final int O_CLOEXEC = 524288;
    public static final int __O_SYNC = 0x100000;
    public static final int O_PATH = 0x200000;
    public static final int __O_TMPFILE = 0x400000;
    public static final int O_SYNC = 0x101000;
    public static final int O_TMPFILE = 0x410000;
    public static final int O_NDELAY = 2048;
    public static final int S_IRUSR = 256;
    public static final int S_IWUSR = 128;
    public static final int S_IXUSR = 64;
    public static final int S_IRWXU = 448;
    public static final int S_IRGRP = 32;
    public static final int S_IWGRP = 16;
    public static final int S_IXGRP = 8;
    public static final int S_IRWXG = 56;
    public static final int S_IROTH = 4;
    public static final int S_IWOTH = 2;
    public static final int S_IXOTH = 1;
    public static final int S_IRWXO = 7;
    public static final int S_ISUID = 2048;
    public static final int S_ISGID = 1024;
    public static final int S_ISVTX = 512;
}


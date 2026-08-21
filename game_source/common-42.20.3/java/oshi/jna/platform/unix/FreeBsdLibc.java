/*
 * Decompiled with CFR 0.152.
 */
package oshi.jna.platform.unix;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.NativeLongByReference;
import oshi.jna.platform.unix.CLibrary;

public interface FreeBsdLibc
extends CLibrary {
    public static final FreeBsdLibc INSTANCE = Native.load("libc", FreeBsdLibc.class);
    public static final int UTX_USERSIZE = 32;
    public static final int UTX_LINESIZE = 16;
    public static final int UTX_IDSIZE = 8;
    public static final int UTX_HOSTSIZE = 128;
    public static final int UINT64_SIZE = Native.getNativeSize(Long.TYPE);
    public static final int INT_SIZE = Native.getNativeSize(Integer.TYPE);
    public static final int CPUSTATES = 5;
    public static final int CP_USER = 0;
    public static final int CP_NICE = 1;
    public static final int CP_SYS = 2;
    public static final int CP_INTR = 3;
    public static final int CP_IDLE = 4;

    public FreeBsdUtmpx getutxent();

    public int thr_self(NativeLongByReference var1);

    @Structure.FieldOrder(value={"cpu_ticks"})
    public static class CpTime
    extends Structure
    implements AutoCloseable {
        public long[] cpu_ticks = new long[5];

        @Override
        public void close() {
            Pointer p = this.getPointer();
            if (p instanceof Memory) {
                ((Memory)p).close();
            }
        }
    }

    @Structure.FieldOrder(value={"tv_sec", "tv_usec"})
    public static class Timeval
    extends Structure {
        public long tv_sec;
        public long tv_usec;
    }

    @Structure.FieldOrder(value={"ut_type", "ut_tv", "ut_id", "ut_pid", "ut_user", "ut_line", "ut_host", "ut_spare"})
    public static class FreeBsdUtmpx
    extends Structure {
        public short ut_type;
        public Timeval ut_tv;
        public byte[] ut_id = new byte[8];
        public int ut_pid;
        public byte[] ut_user = new byte[32];
        public byte[] ut_line = new byte[16];
        public byte[] ut_host = new byte[128];
        public byte[] ut_spare = new byte[64];
    }
}


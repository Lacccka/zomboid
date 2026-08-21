/*
 * Decompiled with CFR 0.152.
 */
package oshi.jna.platform.unix;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.unix.LibCAPI;
import com.sun.jna.ptr.PointerByReference;
import oshi.util.Util;

public interface CLibrary
extends LibCAPI,
Library {
    public static final int AI_CANONNAME = 2;
    public static final int UT_LINESIZE = 32;
    public static final int UT_NAMESIZE = 32;
    public static final int UT_HOSTSIZE = 256;
    public static final int LOGIN_PROCESS = 6;
    public static final int USER_PROCESS = 7;

    public int getpid();

    public int getaddrinfo(String var1, String var2, Addrinfo var3, PointerByReference var4);

    public void freeaddrinfo(Pointer var1);

    public String gai_strerror(int var1);

    public void setutxent();

    public void endutxent();

    public int sysctl(int[] var1, int var2, Pointer var3, LibCAPI.size_t.ByReference var4, Pointer var5, LibCAPI.size_t var6);

    public int sysctlbyname(String var1, Pointer var2, LibCAPI.size_t.ByReference var3, Pointer var4, LibCAPI.size_t var5);

    public int sysctlnametomib(String var1, Pointer var2, LibCAPI.size_t.ByReference var3);

    public int open(String var1, int var2);

    public LibCAPI.ssize_t pread(int var1, Pointer var2, LibCAPI.size_t var3, NativeLong var4);

    public static class BsdIp6stat {
        public long ip6s_total;
        public long ip6s_localout;
    }

    public static class BsdIpstat {
        public int ips_total;
        public int ips_badsum;
        public int ips_tooshort;
        public int ips_toosmall;
        public int ips_badhlen;
        public int ips_badlen;
        public int ips_delivered;
    }

    public static class BsdUdpstat {
        public int udps_ipackets;
        public int udps_hdrops;
        public int udps_badsum;
        public int udps_badlen;
        public int udps_opackets;
        public int udps_noportmcast;
        public int udps_rcv6_swcsum;
        public int udps_snd6_swcsum;
    }

    public static class BsdTcpstat {
        public int tcps_connattempt;
        public int tcps_accepts;
        public int tcps_drops;
        public int tcps_conndrops;
        public int tcps_sndpack;
        public int tcps_sndrexmitpack;
        public int tcps_rcvpack;
        public int tcps_rcvbadsum;
        public int tcps_rcvbadoff;
        public int tcps_rcvmemdrop;
        public int tcps_rcvshort;
    }

    @Structure.FieldOrder(value={"ai_flags", "ai_family", "ai_socktype", "ai_protocol", "ai_addrlen", "ai_addr", "ai_canonname", "ai_next"})
    public static class Addrinfo
    extends Structure
    implements AutoCloseable {
        public int ai_flags;
        public int ai_family;
        public int ai_socktype;
        public int ai_protocol;
        public int ai_addrlen;
        public Sockaddr.ByReference ai_addr;
        public String ai_canonname;
        public ByReference ai_next;

        public Addrinfo() {
        }

        public Addrinfo(Pointer p) {
            super(p);
            this.read();
        }

        @Override
        public void close() {
            Util.freeMemory(this.getPointer());
        }

        public static class ByReference
        extends Addrinfo
        implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder(value={"sa_family", "sa_data"})
    public static class Sockaddr
    extends Structure {
        public short sa_family;
        public byte[] sa_data = new byte[14];

        public static class ByReference
        extends Sockaddr
        implements Structure.ByReference {
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.mac.net;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.unix.LibCAPI;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.Immutable;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;

@ThreadSafe
public final class NetStat {
    private static final Logger LOG = LoggerFactory.getLogger(NetStat.class);
    private static final int CTL_NET = 4;
    private static final int PF_ROUTE = 17;
    private static final int NET_RT_IFLIST2 = 6;
    private static final int RTM_IFINFO2 = 18;

    private NetStat() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Map<Integer, IFdata> queryIFdata(int index) {
        HashMap<Integer, IFdata> data = new HashMap<Integer, IFdata>();
        int[] mib = new int[]{4, 17, 0, 0, 6, 0};
        try (ByRef.CloseableSizeTByReference len = new ByRef.CloseableSizeTByReference();){
            if (0 != SystemB.INSTANCE.sysctl(mib, 6, null, len, null, LibCAPI.size_t.ZERO)) {
                LOG.error("Didn't get buffer length for IFLIST2");
                HashMap<Integer, IFdata> hashMap = data;
                return hashMap;
            }
            try (Memory buf = new Memory(len.longValue());){
                block19: {
                    if (0 != SystemB.INSTANCE.sysctl(mib, 6, (Pointer)buf, len, null, LibCAPI.size_t.ZERO)) {
                        LOG.error("Didn't get buffer for IFLIST2");
                        HashMap<Integer, IFdata> hashMap = data;
                        return hashMap;
                    }
                    long now = System.currentTimeMillis();
                    int lim = (int)(buf.size() - (long)new SystemB.IFmsgHdr().size());
                    int offset = 0;
                    while (offset < lim) {
                        Pointer p = buf.share(offset);
                        SystemB.IFmsgHdr ifm = new SystemB.IFmsgHdr(p);
                        ifm.read();
                        offset += ifm.ifm_msglen;
                        if (ifm.ifm_type != 18) continue;
                        SystemB.IFmsgHdr2 if2m = new SystemB.IFmsgHdr2(p);
                        if2m.read();
                        if (index >= 0 && index != if2m.ifm_index) continue;
                        data.put(Integer.valueOf(if2m.ifm_index), new IFdata(0xFF & if2m.ifm_data.ifi_type, if2m.ifm_data.ifi_opackets, if2m.ifm_data.ifi_ipackets, if2m.ifm_data.ifi_obytes, if2m.ifm_data.ifi_ibytes, if2m.ifm_data.ifi_oerrors, if2m.ifm_data.ifi_ierrors, if2m.ifm_data.ifi_collisions, if2m.ifm_data.ifi_iqdrops, if2m.ifm_data.ifi_baudrate, now));
                        if (index < 0) {
                            continue;
                        }
                        break block19;
                    }
                    return data;
                }
                HashMap<Integer, IFdata> hashMap = data;
                return hashMap;
            }
        }
    }

    @Immutable
    public static class IFdata {
        private final int ifType;
        private final long oPackets;
        private final long iPackets;
        private final long oBytes;
        private final long iBytes;
        private final long oErrors;
        private final long iErrors;
        private final long collisions;
        private final long iDrops;
        private final long speed;
        private final long timeStamp;

        IFdata(int ifType, long oPackets, long iPackets, long oBytes, long iBytes, long oErrors, long iErrors, long collisions, long iDrops, long speed, long timeStamp) {
            this.ifType = ifType;
            this.oPackets = oPackets & 0xFFFFFFFFL;
            this.iPackets = iPackets & 0xFFFFFFFFL;
            this.oBytes = oBytes & 0xFFFFFFFFL;
            this.iBytes = iBytes & 0xFFFFFFFFL;
            this.oErrors = oErrors & 0xFFFFFFFFL;
            this.iErrors = iErrors & 0xFFFFFFFFL;
            this.collisions = collisions & 0xFFFFFFFFL;
            this.iDrops = iDrops & 0xFFFFFFFFL;
            this.speed = speed & 0xFFFFFFFFL;
            this.timeStamp = timeStamp;
        }

        public int getIfType() {
            return this.ifType;
        }

        public long getOPackets() {
            return this.oPackets;
        }

        public long getIPackets() {
            return this.iPackets;
        }

        public long getOBytes() {
            return this.oBytes;
        }

        public long getIBytes() {
            return this.iBytes;
        }

        public long getOErrors() {
            return this.oErrors;
        }

        public long getIErrors() {
            return this.iErrors;
        }

        public long getCollisions() {
            return this.collisions;
        }

        public long getIDrops() {
            return this.iDrops;
        }

        public long getSpeed() {
            return this.speed;
        }

        public long getTimeStamp() {
            return this.timeStamp;
        }
    }
}


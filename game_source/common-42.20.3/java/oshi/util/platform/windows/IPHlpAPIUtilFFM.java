/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.windows;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.ffm.windows.IPHlpAPIFFM;
import oshi.ffm.windows.Win32Exception;
import oshi.ffm.windows.WindowsForeignFunctions;
import oshi.software.os.InternetProtocolStats;
import oshi.util.ParseUtil;

public final class IPHlpAPIUtilFFM {
    private static final Logger LOG = LoggerFactory.getLogger(IPHlpAPIUtilFFM.class);

    private IPHlpAPIUtilFFM() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static String[] getDnsServers() {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment sizeSegment = arena.allocate(ValueLayout.JAVA_INT);
            int ret = IPHlpAPIFFM.GetNetworkParams(MemorySegment.NULL, sizeSegment);
            if (ret != 111) {
                LOG.error("Failed to get network parameters size. Error: {}", (Object)ret);
                String[] stringArray = new String[]{};
                return stringArray;
            }
            int size = sizeSegment.get(ValueLayout.JAVA_INT, 0L);
            MemorySegment buffer = arena.allocate(size);
            ret = IPHlpAPIFFM.GetNetworkParams(buffer, sizeSegment);
            if (ret != 0) {
                LOG.error("Failed to get network parameters. Error: {}", (Object)ret);
                String[] stringArray = new String[]{};
                return stringArray;
            }
            MemorySegment fixedInfo = buffer.asSlice(0L, IPHlpAPIFFM.FIXED_INFO_LAYOUT.byteSize());
            ArrayList<String> dnsServers = new ArrayList<String>();
            MemorySegment dnsServerList = fixedInfo.asSlice(IPHlpAPIFFM.FIXED_INFO_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("DnsServerList")), IPHlpAPIFFM.IP_ADDR_STRING_LAYOUT.byteSize());
            while (dnsServerList != MemorySegment.NULL) {
                MemorySegment ipStringSeg = dnsServerList.asSlice(IPHlpAPIFFM.IP_ADDR_STRING_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("IpAddress")), 16L);
                String ipString = WindowsForeignFunctions.readAnsiString(ipStringSeg, 16);
                if (!ipString.isEmpty()) {
                    dnsServers.add(ipString);
                }
                if ((dnsServerList = dnsServerList.get(ValueLayout.ADDRESS, IPHlpAPIFFM.IP_ADDR_STRING_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Next")))).address() != 0L) continue;
            }
            String[] stringArray = dnsServers.toArray(new String[0]);
            return stringArray;
        }
        catch (Throwable t) {
            LOG.error("GetNetworkParams failed: {}", (Object)t.getMessage());
            return new String[0];
        }
    }

    public static InternetProtocolStats.TcpStats getTcpStats(int family) {
        Arena arena = Arena.ofConfined();
        try {
            MemorySegment stats = arena.allocate(IPHlpAPIFFM.MIB_TCPSTATS_LAYOUT);
            int rc = IPHlpAPIFFM.GetTcpStatisticsEx(stats, family);
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
            long connectionsEstablished = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwCurrEstab")));
            long connectionsActive = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwActiveOpens")));
            long connectionsPassive = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwPassiveOpens")));
            long connectionFailures = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwAttemptFails")));
            long connectionsReset = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwEstabResets")));
            long segmentsSent = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwOutSegs")));
            long segmentsReceived = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwInSegs")));
            long segmentsRetransmitted = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwRetransSegs")));
            long inErrors = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwInErrs")));
            long outResets = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwOutRsts")));
            InternetProtocolStats.TcpStats tcpStats = new InternetProtocolStats.TcpStats(connectionsEstablished, connectionsActive, connectionsPassive, connectionFailures, connectionsReset, segmentsSent, segmentsReceived, segmentsRetransmitted, inErrors, outResets);
            if (arena != null) {
                arena.close();
            }
            return tcpStats;
        }
        catch (Throwable throwable) {
            try {
                if (arena != null) {
                    try {
                        arena.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Throwable t) {
                LOG.debug("getTcpStats failed: {}", (Object)t.getMessage());
                return null;
            }
        }
    }

    public static InternetProtocolStats.UdpStats getUdpStats(int family) {
        Arena arena = Arena.ofConfined();
        try {
            MemorySegment stats = arena.allocate(IPHlpAPIFFM.MIB_UDPSTATS_LAYOUT);
            int rc = IPHlpAPIFFM.GetUdpStatisticsEx(stats, family);
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
            int outDatagrams = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_UDPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwOutDatagrams")));
            int inDatagrams = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_UDPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwInDatagrams")));
            int noPorts = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_UDPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwNoPorts")));
            int inErrors = stats.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_UDPSTATS_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwInErrors")));
            InternetProtocolStats.UdpStats udpStats = new InternetProtocolStats.UdpStats(outDatagrams, inDatagrams, noPorts, inErrors);
            if (arena != null) {
                arena.close();
            }
            return udpStats;
        }
        catch (Throwable throwable) {
            try {
                if (arena != null) {
                    try {
                        arena.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Throwable t) {
                LOG.debug("GetUdpStats failed: {}", (Object)t.getMessage());
                return null;
            }
        }
    }

    public static List<InternetProtocolStats.IPConnection> queryTCPv4Connections() {
        ArrayList<InternetProtocolStats.IPConnection> conns = new ArrayList<InternetProtocolStats.IPConnection>();
        Arena arena = Arena.ofConfined();
        try {
            MemorySegment sizeSegment = arena.allocate(ValueLayout.JAVA_INT);
            int ret = IPHlpAPIFFM.GetExtendedTcpTable(MemorySegment.NULL, sizeSegment, 0, 2, 5, 0);
            if (ret != 122 && ret != 0) {
                throw new Win32Exception(ret);
            }
            int size = sizeSegment.get(ValueLayout.JAVA_INT, 0L);
            MemorySegment buffer = arena.allocate(size);
            ret = IPHlpAPIFFM.GetExtendedTcpTable(buffer, sizeSegment, 0, 2, 5, 0);
            if (ret != 0) {
                throw new Win32Exception(ret);
            }
            int numEntries = buffer.get(ValueLayout.JAVA_INT, 0L);
            long entryOffset = ValueLayout.JAVA_INT.byteSize();
            for (int i = 0; i < numEntries; ++i) {
                MemorySegment rowSeg = buffer.asSlice(entryOffset + (long)i * IPHlpAPIFFM.MIB_TCPROW_OWNER_PID_LAYOUT.byteSize(), IPHlpAPIFFM.MIB_TCPROW_OWNER_PID_LAYOUT.byteSize());
                int localAddr = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwLocalAddr")));
                int localPortBE = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwLocalPort")));
                int remoteAddr = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwRemoteAddr")));
                int remotePortBE = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwRemotePort")));
                int state = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwState")));
                int pid = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCPROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwOwningPid")));
                conns.add(new InternetProtocolStats.IPConnection("tcp4", ParseUtil.parseIntToIP(localAddr), ParseUtil.bigEndian16ToLittleEndian(localPortBE), ParseUtil.parseIntToIP(remoteAddr), ParseUtil.bigEndian16ToLittleEndian(remotePortBE), IPHlpAPIUtilFFM.stateLookup(state), 0, 0, pid));
            }
            ArrayList<InternetProtocolStats.IPConnection> arrayList = conns;
            if (arena != null) {
                arena.close();
            }
            return arrayList;
        }
        catch (Throwable throwable) {
            try {
                if (arena != null) {
                    try {
                        arena.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Throwable t) {
                LOG.debug("queryTCPv4Connections failed: {}", (Object)t.getMessage());
                return Collections.emptyList();
            }
        }
    }

    public static List<InternetProtocolStats.IPConnection> queryTCPv6Connections() {
        ArrayList<InternetProtocolStats.IPConnection> conns = new ArrayList<InternetProtocolStats.IPConnection>();
        Arena arena = Arena.ofConfined();
        try {
            MemorySegment sizeSegment = arena.allocate(ValueLayout.JAVA_INT);
            int ret = IPHlpAPIFFM.GetExtendedTcpTable(MemorySegment.NULL, sizeSegment, 0, 23, 5, 0);
            if (ret != 122 && ret != 0) {
                throw new Win32Exception(ret);
            }
            int size = sizeSegment.get(ValueLayout.JAVA_INT, 0L);
            MemorySegment buffer = arena.allocate(size);
            ret = IPHlpAPIFFM.GetExtendedTcpTable(buffer, sizeSegment, 0, 2, 5, 0);
            if (ret != 0) {
                throw new Win32Exception(ret);
            }
            int numEntries = buffer.get(ValueLayout.JAVA_INT, 0L);
            long entryOffset = ValueLayout.JAVA_INT.byteSize();
            for (int i = 0; i < numEntries; ++i) {
                MemorySegment rowSeg = buffer.asSlice(entryOffset + (long)i * IPHlpAPIFFM.MIB_TCP6ROW_OWNER_PID_LAYOUT.byteSize(), IPHlpAPIFFM.MIB_TCP6ROW_OWNER_PID_LAYOUT.byteSize());
                byte[] localAddr = new byte[16];
                MemorySegment localAddrSeg = rowSeg.asSlice(IPHlpAPIFFM.MIB_TCP6ROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("ucLocalAddr")), 16L);
                for (int b = 0; b < 16; ++b) {
                    localAddr[b] = localAddrSeg.get(ValueLayout.JAVA_BYTE, (long)b);
                }
                int localPortBE = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCP6ROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwLocalPort")));
                byte[] remoteAddr = new byte[16];
                MemorySegment remoteAddrSeg = rowSeg.asSlice(IPHlpAPIFFM.MIB_TCP6ROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("ucRemoteAddr")), 16L);
                for (int b = 0; b < 16; ++b) {
                    remoteAddr[b] = remoteAddrSeg.get(ValueLayout.JAVA_BYTE, (long)b);
                }
                int remotePortBE = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCP6ROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwRemotePort")));
                int state = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCP6ROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwState")));
                int pid = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_TCP6ROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwOwningPid")));
                conns.add(new InternetProtocolStats.IPConnection("tcp6", localAddr, ParseUtil.bigEndian16ToLittleEndian(localPortBE), remoteAddr, ParseUtil.bigEndian16ToLittleEndian(remotePortBE), IPHlpAPIUtilFFM.stateLookup(state), 0, 0, pid));
            }
            ArrayList<InternetProtocolStats.IPConnection> arrayList = conns;
            if (arena != null) {
                arena.close();
            }
            return arrayList;
        }
        catch (Throwable throwable) {
            try {
                if (arena != null) {
                    try {
                        arena.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Throwable t) {
                LOG.debug("queryTCPv6Connections failed: {}", (Object)t.getMessage());
                return Collections.emptyList();
            }
        }
    }

    public static List<InternetProtocolStats.IPConnection> queryUDPv4Connections() {
        ArrayList<InternetProtocolStats.IPConnection> conns = new ArrayList<InternetProtocolStats.IPConnection>();
        Arena arena = Arena.ofConfined();
        try {
            MemorySegment sizeSegment = arena.allocate(ValueLayout.JAVA_INT);
            int ret = IPHlpAPIFFM.GetExtendedUdpTable(MemorySegment.NULL, sizeSegment, 0, 2, 1, 0);
            if (ret != 122 && ret != 0) {
                throw new Win32Exception(ret);
            }
            int size = sizeSegment.get(ValueLayout.JAVA_INT, 0L);
            MemorySegment buffer = arena.allocate(size);
            ret = IPHlpAPIFFM.GetExtendedUdpTable(buffer, sizeSegment, 0, 2, 1, 0);
            if (ret != 0) {
                throw new Win32Exception(ret);
            }
            int numEntries = buffer.get(ValueLayout.JAVA_INT, 0L);
            long entryOffset = ValueLayout.JAVA_INT.byteSize();
            for (int i = 0; i < numEntries; ++i) {
                MemorySegment rowSeg = buffer.asSlice(entryOffset + (long)i * IPHlpAPIFFM.MIB_UDPROW_OWNER_PID_LAYOUT.byteSize(), IPHlpAPIFFM.MIB_UDPROW_OWNER_PID_LAYOUT.byteSize());
                int localAddr = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_UDPROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwLocalAddr")));
                int localPortBE = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_UDPROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwLocalPort")));
                int pid = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_UDPROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwOwningPid")));
                conns.add(new InternetProtocolStats.IPConnection("udp4", ParseUtil.parseIntToIP(localAddr), ParseUtil.bigEndian16ToLittleEndian(localPortBE), new byte[0], 0, InternetProtocolStats.TcpState.NONE, 0, 0, pid));
            }
            ArrayList<InternetProtocolStats.IPConnection> arrayList = conns;
            if (arena != null) {
                arena.close();
            }
            return arrayList;
        }
        catch (Throwable throwable) {
            try {
                if (arena != null) {
                    try {
                        arena.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Throwable t) {
                LOG.debug("queryUDPv4Connections failed: {}", (Object)t.getMessage());
                return Collections.emptyList();
            }
        }
    }

    public static List<InternetProtocolStats.IPConnection> queryUDPv6Connections() {
        ArrayList<InternetProtocolStats.IPConnection> conns = new ArrayList<InternetProtocolStats.IPConnection>();
        Arena arena = Arena.ofConfined();
        try {
            MemorySegment sizeSegment = arena.allocate(ValueLayout.JAVA_INT);
            int ret = IPHlpAPIFFM.GetExtendedUdpTable(MemorySegment.NULL, sizeSegment, 0, 23, 1, 0);
            if (ret != 122 && ret != 0) {
                throw new Win32Exception(ret);
            }
            int size = sizeSegment.get(ValueLayout.JAVA_INT, 0L);
            MemorySegment buffer = arena.allocate(size);
            ret = IPHlpAPIFFM.GetExtendedUdpTable(buffer, sizeSegment, 0, 2, 1, 0);
            if (ret != 0) {
                throw new Win32Exception(ret);
            }
            int numEntries = buffer.get(ValueLayout.JAVA_INT, 0L);
            long entryOffset = ValueLayout.JAVA_INT.byteSize();
            for (int i = 0; i < numEntries; ++i) {
                MemorySegment rowSeg = buffer.asSlice(entryOffset + (long)i * IPHlpAPIFFM.MIB_UDP6ROW_OWNER_PID_LAYOUT.byteSize(), IPHlpAPIFFM.MIB_UDP6ROW_OWNER_PID_LAYOUT.byteSize());
                byte[] localAddr = new byte[16];
                MemorySegment localAddrSeg = rowSeg.asSlice(IPHlpAPIFFM.MIB_TCP6ROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("ucLocalAddr")), 16L);
                for (int b = 0; b < 16; ++b) {
                    localAddr[b] = localAddrSeg.get(ValueLayout.JAVA_BYTE, (long)b);
                }
                int localPortBE = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_UDP6ROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwLocalPort")));
                int pid = rowSeg.get(ValueLayout.JAVA_INT, IPHlpAPIFFM.MIB_UDP6ROW_OWNER_PID_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("dwOwningPid")));
                conns.add(new InternetProtocolStats.IPConnection("udp6", localAddr, ParseUtil.bigEndian16ToLittleEndian(localPortBE), new byte[0], 0, InternetProtocolStats.TcpState.NONE, 0, 0, pid));
            }
            ArrayList<InternetProtocolStats.IPConnection> arrayList = conns;
            if (arena != null) {
                arena.close();
            }
            return arrayList;
        }
        catch (Throwable throwable) {
            try {
                if (arena != null) {
                    try {
                        arena.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Throwable t) {
                LOG.debug("queryUDPv6Connections failed: {}", (Object)t.getMessage());
                return Collections.emptyList();
            }
        }
    }

    private static InternetProtocolStats.TcpState stateLookup(int state) {
        switch (state) {
            case 1: 
            case 12: {
                return InternetProtocolStats.TcpState.CLOSED;
            }
            case 2: {
                return InternetProtocolStats.TcpState.LISTEN;
            }
            case 3: {
                return InternetProtocolStats.TcpState.SYN_SENT;
            }
            case 4: {
                return InternetProtocolStats.TcpState.SYN_RECV;
            }
            case 5: {
                return InternetProtocolStats.TcpState.ESTABLISHED;
            }
            case 6: {
                return InternetProtocolStats.TcpState.FIN_WAIT_1;
            }
            case 7: {
                return InternetProtocolStats.TcpState.FIN_WAIT_2;
            }
            case 8: {
                return InternetProtocolStats.TcpState.CLOSE_WAIT;
            }
            case 9: {
                return InternetProtocolStats.TcpState.CLOSING;
            }
            case 10: {
                return InternetProtocolStats.TcpState.LAST_ACK;
            }
            case 11: {
                return InternetProtocolStats.TcpState.TIME_WAIT;
            }
        }
        return InternetProtocolStats.TcpState.UNKNOWN;
    }
}


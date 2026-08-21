/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.windows;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import oshi.ffm.windows.WindowsForeignFunctions;

public class IPHlpAPIFFM
extends WindowsForeignFunctions {
    public static final int AF_INET = 2;
    public static final int AF_INET6 = 23;
    public static final int TCP_TABLE_OWNER_PID_ALL = 5;
    public static final int UDP_TABLE_OWNER_PID = 1;
    private static final SymbolLookup IPHlpAPI = IPHlpAPIFFM.lib("IPHlpAPI");
    private static final MethodHandle GetExtendedTcpTable = IPHlpAPIFFM.downcall(IPHlpAPI, "GetExtendedTcpTable", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
    public static final StructLayout MIB_TCPROW_OWNER_PID_LAYOUT = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("dwState"), ValueLayout.JAVA_INT.withName("dwLocalAddr"), ValueLayout.JAVA_INT.withName("dwLocalPort"), ValueLayout.JAVA_INT.withName("dwRemoteAddr"), ValueLayout.JAVA_INT.withName("dwRemotePort"), ValueLayout.JAVA_INT.withName("dwOwningPid"));
    public static final StructLayout MIB_TCP6ROW_OWNER_PID_LAYOUT = MemoryLayout.structLayout(MemoryLayout.sequenceLayout(16L, ValueLayout.JAVA_BYTE).withName("ucLocalAddr"), ValueLayout.JAVA_INT.withName("dwLocalScopeId"), ValueLayout.JAVA_INT.withName("dwLocalPort"), MemoryLayout.sequenceLayout(16L, ValueLayout.JAVA_BYTE).withName("ucRemoteAddr"), ValueLayout.JAVA_INT.withName("dwRemoteScopeId"), ValueLayout.JAVA_INT.withName("dwRemotePort"), ValueLayout.JAVA_INT.withName("dwState"), ValueLayout.JAVA_INT.withName("dwOwningPid"));
    private static final MethodHandle GetExtendedUdpTable = IPHlpAPIFFM.downcall(IPHlpAPI, "GetExtendedUdpTable", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
    public static final StructLayout MIB_UDPROW_OWNER_PID_LAYOUT = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("dwLocalAddr"), ValueLayout.JAVA_INT.withName("dwLocalPort"), ValueLayout.JAVA_INT.withName("dwOwningPid"));
    public static final StructLayout MIB_UDP6ROW_OWNER_PID_LAYOUT = MemoryLayout.structLayout(MemoryLayout.sequenceLayout(16L, ValueLayout.JAVA_BYTE).withName("ucLocalAddr"), ValueLayout.JAVA_INT.withName("dwLocalScopeId"), ValueLayout.JAVA_INT.withName("dwLocalPort"), ValueLayout.JAVA_INT.withName("dwOwningPid"));
    private static final MethodHandle GetNetworkParams = IPHlpAPIFFM.downcall(IPHlpAPI, "GetNetworkParams", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    public static final StructLayout IP_ADDRESS_STRING_LAYOUT = MemoryLayout.structLayout(MemoryLayout.sequenceLayout(16L, ValueLayout.JAVA_BYTE).withName("String"));
    public static final StructLayout IP_ADDR_STRING_LAYOUT = MemoryLayout.structLayout(ValueLayout.ADDRESS.withName("Next"), IP_ADDRESS_STRING_LAYOUT.withName("IpAddress"), IP_ADDRESS_STRING_LAYOUT.withName("IpMask"), ValueLayout.JAVA_INT.withName("Context"));
    public static final StructLayout FIXED_INFO_LAYOUT = MemoryLayout.structLayout(MemoryLayout.sequenceLayout(132L, ValueLayout.JAVA_BYTE).withName("HostName"), MemoryLayout.sequenceLayout(132L, ValueLayout.JAVA_BYTE).withName("DomainName"), ValueLayout.ADDRESS.withName("CurrentDnsServer"), IP_ADDR_STRING_LAYOUT.withName("DnsServerList"), ValueLayout.JAVA_INT.withName("NodeType"), MemoryLayout.sequenceLayout(260L, ValueLayout.JAVA_BYTE).withName("ScopeId"), ValueLayout.JAVA_INT.withName("EnableRouting"), ValueLayout.JAVA_INT.withName("EnableProxy"), ValueLayout.JAVA_INT.withName("EnableDns"));
    private static final MethodHandle GetTcpStatisticsEx = IPHlpAPIFFM.downcall(IPHlpAPI, "GetTcpStatisticsEx", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
    public static final StructLayout MIB_TCPSTATS_LAYOUT = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("dwRtoAlgorithm"), ValueLayout.JAVA_INT.withName("dwRtoMin"), ValueLayout.JAVA_INT.withName("dwRtoMax"), ValueLayout.JAVA_INT.withName("dwMaxConn"), ValueLayout.JAVA_INT.withName("dwActiveOpens"), ValueLayout.JAVA_INT.withName("dwPassiveOpens"), ValueLayout.JAVA_INT.withName("dwAttemptFails"), ValueLayout.JAVA_INT.withName("dwEstabResets"), ValueLayout.JAVA_INT.withName("dwCurrEstab"), ValueLayout.JAVA_INT.withName("dwInSegs"), ValueLayout.JAVA_INT.withName("dwOutSegs"), ValueLayout.JAVA_INT.withName("dwRetransSegs"), ValueLayout.JAVA_INT.withName("dwInErrs"), ValueLayout.JAVA_INT.withName("dwOutRsts"), ValueLayout.JAVA_INT.withName("dwNumConns"));
    private static final MethodHandle GetUdpStatisticsEx = IPHlpAPIFFM.downcall(IPHlpAPI, "GetUdpStatisticsEx", ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
    public static final StructLayout MIB_UDPSTATS_LAYOUT = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("dwInDatagrams"), ValueLayout.JAVA_INT.withName("dwNoPorts"), ValueLayout.JAVA_INT.withName("dwInErrors"), ValueLayout.JAVA_INT.withName("dwOutDatagrams"), ValueLayout.JAVA_INT.withName("dwNumAddrs"));

    public static int GetExtendedTcpTable(MemorySegment tcpTable, MemorySegment tableSize, int bOrder, int ulAf, int tableClass, int reserved) throws Throwable {
        return GetExtendedTcpTable.invokeExact(tcpTable, tableSize, bOrder, ulAf, tableClass, reserved);
    }

    public static int GetExtendedUdpTable(MemorySegment udpTable, MemorySegment tableSize, int bOrder, int ulAf, int tableClass, int reserved) throws Throwable {
        return GetExtendedUdpTable.invokeExact(udpTable, tableSize, bOrder, ulAf, tableClass, reserved);
    }

    public static int GetNetworkParams(MemorySegment fixedInfo, MemorySegment bufferSize) throws Throwable {
        return GetNetworkParams.invokeExact(fixedInfo, bufferSize);
    }

    public static int GetTcpStatisticsEx(MemorySegment stats, int family) throws Throwable {
        return GetTcpStatisticsEx.invokeExact(stats, family);
    }

    public static int GetUdpStatisticsEx(MemorySegment stats, int family) throws Throwable {
        return GetUdpStatisticsEx.invokeExact(stats, family);
    }
}


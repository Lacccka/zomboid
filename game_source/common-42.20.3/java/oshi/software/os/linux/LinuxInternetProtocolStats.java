/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.linux;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.linux.proc.ProcessStat;
import oshi.software.common.AbstractInternetProtocolStats;
import oshi.software.os.InternetProtocolStats;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.platform.linux.ProcPath;
import oshi.util.tuples.Pair;

@ThreadSafe
public class LinuxInternetProtocolStats
extends AbstractInternetProtocolStats {
    private final String tcpColon = "Tcp:";
    private final String udpColon = "Udp:";
    private final String udp6 = "Udp6";

    @Override
    public InternetProtocolStats.TcpStats getTCPv4Stats() {
        byte[] fileBytes = FileUtil.readAllBytes(ProcPath.SNMP, true);
        List<String> lines = ParseUtil.parseByteArrayToStrings(fileBytes);
        EnumMap<TcpStat, Long> tcpData = new EnumMap<TcpStat, Long>(TcpStat.class);
        for (int line = 0; line < lines.size() - 1; line += 2) {
            if (!lines.get(line).startsWith("Tcp:") || !lines.get(line + 1).startsWith("Tcp:")) continue;
            Map<TcpStat, String> parsedData = ParseUtil.stringToEnumMap(TcpStat.class, lines.get(line + 1).substring("Tcp:".length()).trim(), ' ');
            for (Map.Entry<TcpStat, String> entry : parsedData.entrySet()) {
                tcpData.put(entry.getKey(), ParseUtil.parseLongOrDefault(entry.getValue(), 0L));
            }
            break;
        }
        return new InternetProtocolStats.TcpStats(tcpData.getOrDefault((Object)TcpStat.CurrEstab, 0L), tcpData.getOrDefault((Object)TcpStat.ActiveOpens, 0L), tcpData.getOrDefault((Object)TcpStat.PassiveOpens, 0L), tcpData.getOrDefault((Object)TcpStat.AttemptFails, 0L), tcpData.getOrDefault((Object)TcpStat.EstabResets, 0L), tcpData.getOrDefault((Object)TcpStat.OutSegs, 0L), tcpData.getOrDefault((Object)TcpStat.InSegs, 0L), tcpData.getOrDefault((Object)TcpStat.RetransSegs, 0L), tcpData.getOrDefault((Object)TcpStat.InErrs, 0L), tcpData.getOrDefault((Object)TcpStat.OutRsts, 0L));
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv4Stats() {
        byte[] fileBytes = FileUtil.readAllBytes(ProcPath.SNMP, true);
        List<String> lines = ParseUtil.parseByteArrayToStrings(fileBytes);
        EnumMap<UdpStat, Long> udpData = new EnumMap<UdpStat, Long>(UdpStat.class);
        for (int line = 0; line < lines.size() - 1; line += 2) {
            if (!lines.get(line).startsWith("Udp:") || !lines.get(line + 1).startsWith("Udp:")) continue;
            Map<UdpStat, String> parsedData = ParseUtil.stringToEnumMap(UdpStat.class, lines.get(line + 1).substring("Udp:".length()).trim(), ' ');
            for (Map.Entry<UdpStat, String> entry : parsedData.entrySet()) {
                udpData.put(entry.getKey(), ParseUtil.parseLongOrDefault(entry.getValue(), 0L));
            }
            break;
        }
        return new InternetProtocolStats.UdpStats(udpData.getOrDefault((Object)UdpStat.OutDatagrams, 0L), udpData.getOrDefault((Object)UdpStat.InDatagrams, 0L), udpData.getOrDefault((Object)UdpStat.NoPorts, 0L), udpData.getOrDefault((Object)UdpStat.InErrors, 0L));
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv6Stats() {
        byte[] fileBytes = FileUtil.readAllBytes(ProcPath.SNMP6, true);
        List<String> lines = ParseUtil.parseByteArrayToStrings(fileBytes);
        long inDatagrams = 0L;
        long noPorts = 0L;
        long inErrors = 0L;
        long outDatagrams = 0L;
        int foundUDPv6StatsCount = 0;
        block12: for (int line = lines.size() - 1; line >= 0 && foundUDPv6StatsCount < 4; --line) {
            if (!lines.get(line).startsWith("Udp6")) continue;
            String[] parts = lines.get(line).split("\\s+");
            switch (parts[0]) {
                case "Udp6InDatagrams": {
                    inDatagrams = ParseUtil.parseLongOrDefault(parts[1], 0L);
                    ++foundUDPv6StatsCount;
                    continue block12;
                }
                case "Udp6NoPorts": {
                    noPorts = ParseUtil.parseLongOrDefault(parts[1], 0L);
                    ++foundUDPv6StatsCount;
                    continue block12;
                }
                case "Udp6InErrors": {
                    inErrors = ParseUtil.parseLongOrDefault(parts[1], 0L);
                    ++foundUDPv6StatsCount;
                    continue block12;
                }
                case "Udp6OutDatagrams": {
                    outDatagrams = ParseUtil.parseLongOrDefault(parts[1], 0L);
                    ++foundUDPv6StatsCount;
                    continue block12;
                }
            }
        }
        return new InternetProtocolStats.UdpStats(inDatagrams, noPorts, inErrors, outDatagrams);
    }

    @Override
    public List<InternetProtocolStats.IPConnection> getConnections() {
        ArrayList<InternetProtocolStats.IPConnection> conns = new ArrayList<InternetProtocolStats.IPConnection>();
        Map<Long, Integer> pidMap = ProcessStat.querySocketToPidMap();
        conns.addAll(LinuxInternetProtocolStats.queryConnections("tcp", 4, pidMap));
        conns.addAll(LinuxInternetProtocolStats.queryConnections("tcp", 6, pidMap));
        conns.addAll(LinuxInternetProtocolStats.queryConnections("udp", 4, pidMap));
        conns.addAll(LinuxInternetProtocolStats.queryConnections("udp", 6, pidMap));
        return conns;
    }

    private static List<InternetProtocolStats.IPConnection> queryConnections(String protocol, int ipver, Map<Long, Integer> pidMap) {
        ArrayList<InternetProtocolStats.IPConnection> conns = new ArrayList<InternetProtocolStats.IPConnection>();
        for (String s : FileUtil.readFile(ProcPath.NET + "/" + protocol + (ipver == 6 ? "6" : ""))) {
            String[] split;
            if (s.indexOf(58) < 0 || (split = ParseUtil.whitespaces.split(s.trim())).length <= 9) continue;
            Pair<byte[], Integer> lAddr = LinuxInternetProtocolStats.parseIpAddr(split[1]);
            Pair<byte[], Integer> fAddr = LinuxInternetProtocolStats.parseIpAddr(split[2]);
            InternetProtocolStats.TcpState state = LinuxInternetProtocolStats.stateLookup(ParseUtil.hexStringToInt(split[3], 0));
            Pair<Integer, Integer> txQrxQ = LinuxInternetProtocolStats.parseHexColonHex(split[4]);
            long inode = ParseUtil.parseLongOrDefault(split[9], 0L);
            conns.add(new InternetProtocolStats.IPConnection(protocol + ipver, lAddr.getA(), lAddr.getB(), fAddr.getA(), fAddr.getB(), state, txQrxQ.getA(), txQrxQ.getB(), pidMap.getOrDefault(inode, -1)));
        }
        return conns;
    }

    private static Pair<byte[], Integer> parseIpAddr(String s) {
        int colon = s.indexOf(58);
        if (colon > 0 && colon < s.length()) {
            byte[] first = ParseUtil.hexStringToByteArray(s.substring(0, colon));
            int i = 0;
            while (i + 3 < first.length) {
                byte tmp = first[i];
                first[i] = first[i + 3];
                first[i + 3] = tmp;
                tmp = first[i + 1];
                first[i + 1] = first[i + 2];
                first[i + 2] = tmp;
                i += 4;
            }
            int second = ParseUtil.hexStringToInt(s.substring(colon + 1), 0);
            return new Pair<byte[], Integer>(first, second);
        }
        return new Pair<byte[], Integer>(new byte[0], 0);
    }

    private static Pair<Integer, Integer> parseHexColonHex(String s) {
        int colon = s.indexOf(58);
        if (colon > 0 && colon < s.length()) {
            int first = ParseUtil.hexStringToInt(s.substring(0, colon), 0);
            int second = ParseUtil.hexStringToInt(s.substring(colon + 1), 0);
            return new Pair<Integer, Integer>(first, second);
        }
        return new Pair<Integer, Integer>(0, 0);
    }

    private static InternetProtocolStats.TcpState stateLookup(int state) {
        switch (state) {
            case 1: {
                return InternetProtocolStats.TcpState.ESTABLISHED;
            }
            case 2: {
                return InternetProtocolStats.TcpState.SYN_SENT;
            }
            case 3: {
                return InternetProtocolStats.TcpState.SYN_RECV;
            }
            case 4: {
                return InternetProtocolStats.TcpState.FIN_WAIT_1;
            }
            case 5: {
                return InternetProtocolStats.TcpState.FIN_WAIT_2;
            }
            case 6: {
                return InternetProtocolStats.TcpState.TIME_WAIT;
            }
            case 7: {
                return InternetProtocolStats.TcpState.CLOSED;
            }
            case 8: {
                return InternetProtocolStats.TcpState.CLOSE_WAIT;
            }
            case 9: {
                return InternetProtocolStats.TcpState.LAST_ACK;
            }
            case 10: {
                return InternetProtocolStats.TcpState.LISTEN;
            }
            case 11: {
                return InternetProtocolStats.TcpState.CLOSING;
            }
        }
        return InternetProtocolStats.TcpState.UNKNOWN;
    }

    private static enum TcpStat {
        RtoAlgorithm,
        RtoMin,
        RtoMax,
        MaxConn,
        ActiveOpens,
        PassiveOpens,
        AttemptFails,
        EstabResets,
        CurrEstab,
        InSegs,
        OutSegs,
        RetransSegs,
        InErrs,
        OutRsts,
        InCsumErrors;

    }

    private static enum UdpStat {
        OutDatagrams,
        InDatagrams,
        NoPorts,
        InErrors,
        RcvbufErrors,
        SndbufErrors,
        InCsumErrors,
        IgnoredMulti,
        MemErrors;

    }
}


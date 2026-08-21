/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.solaris;

import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.common.AbstractInternetProtocolStats;
import oshi.software.os.InternetProtocolStats;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@ThreadSafe
public class SolarisInternetProtocolStats
extends AbstractInternetProtocolStats {
    @Override
    public InternetProtocolStats.TcpStats getTCPv4Stats() {
        return SolarisInternetProtocolStats.getTcpStats();
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv4Stats() {
        return SolarisInternetProtocolStats.getUdpStats();
    }

    private static InternetProtocolStats.TcpStats getTcpStats() {
        long connectionsEstablished = 0L;
        long connectionsActive = 0L;
        long connectionsPassive = 0L;
        long connectionFailures = 0L;
        long connectionsReset = 0L;
        long segmentsSent = 0L;
        long segmentsReceived = 0L;
        long segmentsRetransmitted = 0L;
        long inErrors = 0L;
        long outResets = 0L;
        List<String> netstat = ExecutingCommand.runNative("netstat -s -P tcp");
        netstat.addAll(ExecutingCommand.runNative("netstat -s -P ip"));
        for (String s : netstat) {
            String[] stats;
            block25: for (String stat : stats = SolarisInternetProtocolStats.splitOnPrefix(s, "tcp")) {
                String[] split;
                if (stat == null || (split = stat.split("=")).length != 2) continue;
                switch (split[0].trim()) {
                    case "tcpCurrEstab": {
                        connectionsEstablished = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block25;
                    }
                    case "tcpActiveOpens": {
                        connectionsActive = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block25;
                    }
                    case "tcpPassiveOpens": {
                        connectionsPassive = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block25;
                    }
                    case "tcpAttemptFails": {
                        connectionFailures = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block25;
                    }
                    case "tcpEstabResets": {
                        connectionsReset = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block25;
                    }
                    case "tcpOutSegs": {
                        segmentsSent = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block25;
                    }
                    case "tcpInSegs": {
                        segmentsReceived = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block25;
                    }
                    case "tcpRetransSegs": {
                        segmentsRetransmitted = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block25;
                    }
                    case "tcpInErr": {
                        inErrors = ParseUtil.getFirstIntValue(split[1].trim());
                        continue block25;
                    }
                    case "tcpOutRsts": {
                        outResets = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block25;
                    }
                }
            }
        }
        return new InternetProtocolStats.TcpStats(connectionsEstablished, connectionsActive, connectionsPassive, connectionFailures, connectionsReset, segmentsSent, segmentsReceived, segmentsRetransmitted, inErrors, outResets);
    }

    private static InternetProtocolStats.UdpStats getUdpStats() {
        long datagramsSent = 0L;
        long datagramsReceived = 0L;
        long datagramsNoPort = 0L;
        long datagramsReceivedErrors = 0L;
        List<String> netstat = ExecutingCommand.runNative("netstat -s -P udp");
        netstat.addAll(ExecutingCommand.runNative("netstat -s -P ip"));
        for (String s : netstat) {
            String[] stats;
            block13: for (String stat : stats = SolarisInternetProtocolStats.splitOnPrefix(s, "udp")) {
                String[] split;
                if (stat == null || (split = stat.split("=")).length != 2) continue;
                switch (split[0].trim()) {
                    case "udpOutDatagrams": {
                        datagramsSent = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block13;
                    }
                    case "udpInDatagrams": {
                        datagramsReceived = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block13;
                    }
                    case "udpNoPorts": {
                        datagramsNoPort = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block13;
                    }
                    case "udpInErrors": {
                        datagramsReceivedErrors = ParseUtil.parseLongOrDefault(split[1].trim(), 0L);
                        continue block13;
                    }
                }
            }
        }
        return new InternetProtocolStats.UdpStats(datagramsSent, datagramsReceived, datagramsNoPort, datagramsReceivedErrors);
    }

    private static String[] splitOnPrefix(String s, String prefix) {
        String[] stats = new String[2];
        int first = s.indexOf(prefix);
        if (first >= 0) {
            int second = s.indexOf(prefix, first + 1);
            if (second >= 0) {
                stats[0] = s.substring(first, second).trim();
                stats[1] = s.substring(second).trim();
            } else {
                stats[0] = s.substring(first).trim();
            }
        }
        return stats;
    }
}


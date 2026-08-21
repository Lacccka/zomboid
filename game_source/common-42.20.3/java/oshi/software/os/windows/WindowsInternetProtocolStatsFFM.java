/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.windows;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.ffm.windows.Win32Exception;
import oshi.software.common.AbstractInternetProtocolStats;
import oshi.software.os.InternetProtocolStats;
import oshi.util.platform.windows.IPHlpAPIUtilFFM;

public class WindowsInternetProtocolStatsFFM
extends AbstractInternetProtocolStats {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsInternetProtocolStatsFFM.class);

    @Override
    public List<InternetProtocolStats.IPConnection> getConnections() {
        ArrayList<InternetProtocolStats.IPConnection> conns = new ArrayList<InternetProtocolStats.IPConnection>();
        conns.addAll(IPHlpAPIUtilFFM.queryTCPv4Connections());
        conns.addAll(IPHlpAPIUtilFFM.queryTCPv6Connections());
        conns.addAll(IPHlpAPIUtilFFM.queryUDPv4Connections());
        conns.addAll(IPHlpAPIUtilFFM.queryUDPv6Connections());
        return conns;
    }

    @Override
    public InternetProtocolStats.TcpStats getTCPv4Stats() {
        try {
            return IPHlpAPIUtilFFM.getTcpStats(2);
        }
        catch (Win32Exception e) {
            LOG.error("Failed to read TCPv4 stats: {}", (Object)e.getMessage());
            return null;
        }
    }

    @Override
    public InternetProtocolStats.TcpStats getTCPv6Stats() {
        try {
            return IPHlpAPIUtilFFM.getTcpStats(23);
        }
        catch (Win32Exception e) {
            LOG.error("Failed to read TCPv6 stats: {}", (Object)e.getMessage());
            return null;
        }
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv4Stats() {
        try {
            return IPHlpAPIUtilFFM.getUdpStats(2);
        }
        catch (Win32Exception e) {
            LOG.error("Failed to read UDPv4 stats: {}", (Object)e.getMessage());
            return null;
        }
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv6Stats() {
        try {
            return IPHlpAPIUtilFFM.getUdpStats(23);
        }
        catch (Win32Exception e) {
            LOG.error("Failed to read UDPv6 stats: {}", (Object)e.getMessage());
            return null;
        }
    }
}


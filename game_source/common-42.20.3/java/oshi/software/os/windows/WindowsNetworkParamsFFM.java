/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.windows;

import com.sun.jna.platform.win32.Win32Exception;
import java.util.List;
import oshi.software.common.AbstractNetworkParams;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.platform.windows.IPHlpAPIUtilFFM;
import oshi.util.platform.windows.Kernel32UtilFFM;

public final class WindowsNetworkParamsFFM
extends AbstractNetworkParams {
    @Override
    public String[] getDnsServers() {
        return IPHlpAPIUtilFFM.getDnsServers();
    }

    @Override
    public String getDomainName() {
        return Kernel32UtilFFM.getComputerNameEx();
    }

    @Override
    public String getHostName() {
        try {
            return Kernel32UtilFFM.getComputerName();
        }
        catch (Win32Exception e) {
            return super.getHostName();
        }
    }

    @Override
    public String getIpv4DefaultGateway() {
        return WindowsNetworkParamsFFM.parseIpv4Route();
    }

    @Override
    public String getIpv6DefaultGateway() {
        return WindowsNetworkParamsFFM.parseIpv6Route();
    }

    private static String parseIpv4Route() {
        List<String> lines = ExecutingCommand.runNative("route print -4 0.0.0.0");
        for (String line : lines) {
            String[] fields = ParseUtil.whitespaces.split(line.trim());
            if (fields.length <= 2 || !"0.0.0.0".equals(fields[0])) continue;
            return fields[2];
        }
        return "";
    }

    private static String parseIpv6Route() {
        List<String> lines = ExecutingCommand.runNative("route print -6 ::/0");
        for (String line : lines) {
            String[] fields = ParseUtil.whitespaces.split(line.trim());
            if (fields.length <= 3 || !"::/0".equals(fields[2])) continue;
            return fields[3];
        }
        return "";
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.windows;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.IPHlpAPI;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Win32Exception;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;
import oshi.software.common.AbstractNetworkParams;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@ThreadSafe
final class WindowsNetworkParams
extends AbstractNetworkParams {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsNetworkParams.class);
    private static final int COMPUTER_NAME_DNS_DOMAIN_FULLY_QUALIFIED = 3;

    WindowsNetworkParams() {
    }

    @Override
    public String getDomainName() {
        char[] buffer = new char[256];
        try (ByRef.CloseableIntByReference bufferSize = new ByRef.CloseableIntByReference(buffer.length);){
            if (!Kernel32.INSTANCE.GetComputerNameEx(3, buffer, bufferSize)) {
                LOG.error("Failed to get dns domain name. Error code: {}", (Object)Kernel32.INSTANCE.GetLastError());
                String string = "";
                return string;
            }
        }
        return Native.toString(buffer);
    }

    @Override
    public String[] getDnsServers() {
        try (ByRef.CloseableIntByReference bufferSize = new ByRef.CloseableIntByReference();){
            Memory buffer;
            block16: {
                int ret = IPHlpAPI.INSTANCE.GetNetworkParams(null, bufferSize);
                if (ret != 111) {
                    LOG.error("Failed to get network parameters buffer size. Error code: {}", (Object)ret);
                    String[] stringArray = new String[]{};
                    return stringArray;
                }
                buffer = new Memory(bufferSize.getValue());
                ret = IPHlpAPI.INSTANCE.GetNetworkParams(buffer, bufferSize);
                if (ret == 0) break block16;
                LOG.error("Failed to get network parameters. Error code: {}", (Object)ret);
                String[] stringArray = new String[]{};
                buffer.close();
                return stringArray;
            }
            try {
                IPHlpAPI.FIXED_INFO fixedInfo = new IPHlpAPI.FIXED_INFO(buffer);
                ArrayList<String> list = new ArrayList<String>();
                IPHlpAPI.IP_ADDR_STRING dns = fixedInfo.DnsServerList;
                while (dns != null) {
                    String addr = Native.toString(dns.IpAddress.String, StandardCharsets.US_ASCII);
                    int nullPos = addr.indexOf(0);
                    if (nullPos != -1) {
                        addr = addr.substring(0, nullPos);
                    }
                    list.add(addr);
                    dns = dns.Next;
                }
                String[] stringArray = list.toArray(new String[0]);
                buffer.close();
                return stringArray;
            }
            catch (Throwable throwable) {
                try {
                    buffer.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
        }
    }

    @Override
    public String getHostName() {
        try {
            return Kernel32Util.getComputerName();
        }
        catch (Win32Exception e) {
            return super.getHostName();
        }
    }

    @Override
    public String getIpv4DefaultGateway() {
        return WindowsNetworkParams.parseIpv4Route();
    }

    @Override
    public String getIpv6DefaultGateway() {
        return WindowsNetworkParams.parseIpv6Route();
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


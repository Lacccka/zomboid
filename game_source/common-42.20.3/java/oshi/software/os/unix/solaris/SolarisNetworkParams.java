/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.solaris;

import com.sun.jna.Native;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.platform.unix.SolarisLibc;
import oshi.software.common.AbstractNetworkParams;
import oshi.util.ExecutingCommand;

@ThreadSafe
final class SolarisNetworkParams
extends AbstractNetworkParams {
    private static final SolarisLibc LIBC = SolarisLibc.INSTANCE;

    SolarisNetworkParams() {
    }

    @Override
    public String getHostName() {
        byte[] hostnameBuffer = new byte[256];
        if (0 != LIBC.gethostname(hostnameBuffer, hostnameBuffer.length)) {
            return super.getHostName();
        }
        return Native.toString(hostnameBuffer);
    }

    @Override
    public String getIpv4DefaultGateway() {
        return SolarisNetworkParams.searchGateway(ExecutingCommand.runNative("route get -inet default"));
    }

    @Override
    public String getIpv6DefaultGateway() {
        return SolarisNetworkParams.searchGateway(ExecutingCommand.runNative("route get -inet6 default"));
    }
}


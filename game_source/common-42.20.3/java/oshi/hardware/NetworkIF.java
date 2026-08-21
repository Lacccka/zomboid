/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware;

import java.net.NetworkInterface;
import java.util.Arrays;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface NetworkIF {
    public NetworkInterface queryNetworkInterface();

    public String getName();

    public int getIndex();

    public String getDisplayName();

    default public String getIfAlias() {
        return "";
    }

    default public IfOperStatus getIfOperStatus() {
        return IfOperStatus.UNKNOWN;
    }

    public long getMTU();

    public String getMacaddr();

    public String[] getIPv4addr();

    public Short[] getSubnetMasks();

    public String[] getIPv6addr();

    public Short[] getPrefixLengths();

    default public int getIfType() {
        return 0;
    }

    default public int getNdisPhysicalMediumType() {
        return 0;
    }

    default public boolean isConnectorPresent() {
        return false;
    }

    public long getBytesRecv();

    public long getBytesSent();

    public long getPacketsRecv();

    public long getPacketsSent();

    public long getInErrors();

    public long getOutErrors();

    public long getInDrops();

    public long getCollisions();

    public long getSpeed();

    public long getTimeStamp();

    public boolean isKnownVmMacAddr();

    public boolean updateAttributes();

    public static enum IfOperStatus {
        UP(1),
        DOWN(2),
        TESTING(3),
        UNKNOWN(4),
        DORMANT(5),
        NOT_PRESENT(6),
        LOWER_LAYER_DOWN(7);

        private final int value;

        private IfOperStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static IfOperStatus byValue(int value) {
            return Arrays.stream(IfOperStatus.values()).filter(st -> st.getValue() == value).findFirst().orElse(UNKNOWN);
        }
    }
}


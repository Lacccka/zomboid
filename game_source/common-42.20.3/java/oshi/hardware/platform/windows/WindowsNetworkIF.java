/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.windows;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.IPHlpAPI;
import com.sun.jna.platform.win32.VersionHelpers;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.NetworkIF;
import oshi.hardware.common.AbstractNetworkIF;
import oshi.jna.Struct;
import oshi.util.ParseUtil;

@ThreadSafe
public final class WindowsNetworkIF
extends AbstractNetworkIF {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsNetworkIF.class);
    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
    private static final byte CONNECTOR_PRESENT_BIT = 4;
    private int ifType;
    private int ndisPhysicalMediumType;
    private boolean connectorPresent;
    private long bytesRecv;
    private long bytesSent;
    private long packetsRecv;
    private long packetsSent;
    private long inErrors;
    private long outErrors;
    private long inDrops;
    private long collisions;
    private long speed;
    private long timeStamp;
    private String ifAlias;
    private NetworkIF.IfOperStatus ifOperStatus;

    public WindowsNetworkIF(NetworkInterface netint) throws InstantiationException {
        super(netint);
        this.updateAttributes();
    }

    public static List<NetworkIF> getNetworks(boolean includeLocalInterfaces) {
        ArrayList<NetworkIF> ifList = new ArrayList<NetworkIF>();
        for (NetworkInterface ni : WindowsNetworkIF.getNetworkInterfaces(includeLocalInterfaces)) {
            try {
                ifList.add(new WindowsNetworkIF(ni));
            }
            catch (InstantiationException e) {
                LOG.debug("Network Interface Instantiation failed: {}", (Object)e.getMessage());
            }
        }
        return ifList;
    }

    @Override
    public int getIfType() {
        return this.ifType;
    }

    @Override
    public int getNdisPhysicalMediumType() {
        return this.ndisPhysicalMediumType;
    }

    @Override
    public boolean isConnectorPresent() {
        return this.connectorPresent;
    }

    @Override
    public long getBytesRecv() {
        return this.bytesRecv;
    }

    @Override
    public long getBytesSent() {
        return this.bytesSent;
    }

    @Override
    public long getPacketsRecv() {
        return this.packetsRecv;
    }

    @Override
    public long getPacketsSent() {
        return this.packetsSent;
    }

    @Override
    public long getInErrors() {
        return this.inErrors;
    }

    @Override
    public long getOutErrors() {
        return this.outErrors;
    }

    @Override
    public long getInDrops() {
        return this.inDrops;
    }

    @Override
    public long getCollisions() {
        return this.collisions;
    }

    @Override
    public long getSpeed() {
        return this.speed;
    }

    @Override
    public long getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public String getIfAlias() {
        return this.ifAlias;
    }

    @Override
    public NetworkIF.IfOperStatus getIfOperStatus() {
        return this.ifOperStatus;
    }

    @Override
    public boolean updateAttributes() {
        if (IS_VISTA_OR_GREATER) {
            try (Struct.CloseableMibIfRow2 ifRow = new Struct.CloseableMibIfRow2();){
                ifRow.InterfaceIndex = this.queryNetworkInterface().getIndex();
                if (0 != IPHlpAPI.INSTANCE.GetIfEntry2(ifRow)) {
                    LOG.error("Failed to retrieve data for interface {}, {}", (Object)this.queryNetworkInterface().getIndex(), (Object)this.getName());
                    boolean bl = false;
                    return bl;
                }
                this.ifType = ifRow.Type;
                this.ndisPhysicalMediumType = ifRow.PhysicalMediumType;
                this.connectorPresent = (ifRow.InterfaceAndOperStatusFlags & 4) > 0;
                this.bytesSent = ifRow.OutOctets;
                this.bytesRecv = ifRow.InOctets;
                this.packetsSent = ifRow.OutUcastPkts;
                this.packetsRecv = ifRow.InUcastPkts;
                this.outErrors = ifRow.OutErrors;
                this.inErrors = ifRow.InErrors;
                this.collisions = ifRow.OutDiscards;
                this.inDrops = ifRow.InDiscards;
                this.speed = ifRow.ReceiveLinkSpeed;
                this.ifAlias = Native.toString(ifRow.Alias);
                this.ifOperStatus = NetworkIF.IfOperStatus.byValue(ifRow.OperStatus);
            }
        }
        try (Struct.CloseableMibIfRow ifRow = new Struct.CloseableMibIfRow();){
            ifRow.dwIndex = this.queryNetworkInterface().getIndex();
            if (0 != IPHlpAPI.INSTANCE.GetIfEntry(ifRow)) {
                LOG.error("Failed to retrieve data for interface {}, {}", (Object)this.queryNetworkInterface().getIndex(), (Object)this.getName());
                boolean bl = false;
                return bl;
            }
            this.ifType = ifRow.dwType;
            this.bytesSent = ParseUtil.unsignedIntToLong(ifRow.dwOutOctets);
            this.bytesRecv = ParseUtil.unsignedIntToLong(ifRow.dwInOctets);
            this.packetsSent = ParseUtil.unsignedIntToLong(ifRow.dwOutUcastPkts);
            this.packetsRecv = ParseUtil.unsignedIntToLong(ifRow.dwInUcastPkts);
            this.outErrors = ParseUtil.unsignedIntToLong(ifRow.dwOutErrors);
            this.inErrors = ParseUtil.unsignedIntToLong(ifRow.dwInErrors);
            this.collisions = ParseUtil.unsignedIntToLong(ifRow.dwOutDiscards);
            this.inDrops = ParseUtil.unsignedIntToLong(ifRow.dwInDiscards);
            this.speed = ParseUtil.unsignedIntToLong(ifRow.dwSpeed);
            this.ifAlias = "";
            this.ifOperStatus = NetworkIF.IfOperStatus.UNKNOWN;
        }
        this.timeStamp = System.currentTimeMillis();
        return true;
    }
}


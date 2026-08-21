/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.linux;

import com.sun.jna.platform.linux.Udev;
import java.io.File;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.NetworkIF;
import oshi.hardware.common.AbstractNetworkIF;
import oshi.software.os.linux.LinuxOperatingSystem;
import oshi.util.FileUtil;
import oshi.util.Util;
import oshi.util.platform.linux.SysPath;

@ThreadSafe
public final class LinuxNetworkIF
extends AbstractNetworkIF {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxNetworkIF.class);
    private int ifType;
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
    private String ifAlias = "";
    private NetworkIF.IfOperStatus ifOperStatus = NetworkIF.IfOperStatus.UNKNOWN;

    public LinuxNetworkIF(NetworkInterface netint) throws InstantiationException {
        super(netint, LinuxNetworkIF.queryIfModel(netint));
        this.updateAttributes();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String queryIfModel(NetworkInterface netint) {
        String name;
        block13: {
            name = netint.getName();
            if (!LinuxOperatingSystem.HAS_UDEV) {
                return LinuxNetworkIF.queryIfModelFromSysfs(name);
            }
            Udev.UdevContext udev = Udev.INSTANCE.udev_new();
            if (udev != null) {
                try {
                    Udev.UdevDevice device = udev.deviceNewFromSyspath(SysPath.NET + name);
                    if (device == null) break block13;
                    try {
                        String devVendor = device.getPropertyValue("ID_VENDOR_FROM_DATABASE");
                        String devModel = device.getPropertyValue("ID_MODEL_FROM_DATABASE");
                        if (!Util.isBlank(devModel)) {
                            if (!Util.isBlank(devVendor)) {
                                String string = devVendor + " " + devModel;
                                return string;
                            }
                            String string = devModel;
                            return string;
                        }
                    }
                    finally {
                        device.unref();
                    }
                }
                finally {
                    udev.unref();
                }
            }
        }
        return name;
    }

    private static String queryIfModelFromSysfs(String name) {
        Map<String, String> uevent = FileUtil.getKeyValueMapFromFile(SysPath.NET + name + "/uevent", "=");
        String devVendor = uevent.get("ID_VENDOR_FROM_DATABASE");
        String devModel = uevent.get("ID_MODEL_FROM_DATABASE");
        if (!Util.isBlank(devModel)) {
            if (!Util.isBlank(devVendor)) {
                return devVendor + " " + devModel;
            }
            return devModel;
        }
        return name;
    }

    public static List<NetworkIF> getNetworks(boolean includeLocalInterfaces) {
        ArrayList<NetworkIF> ifList = new ArrayList<NetworkIF>();
        for (NetworkInterface ni : LinuxNetworkIF.getNetworkInterfaces(includeLocalInterfaces)) {
            try {
                ifList.add(new LinuxNetworkIF(ni));
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
        String name = SysPath.NET + this.getName();
        try {
            File ifDir = new File(name + "/statistics");
            if (!ifDir.isDirectory()) {
                return false;
            }
        }
        catch (SecurityException e) {
            return false;
        }
        this.timeStamp = System.currentTimeMillis();
        this.ifType = FileUtil.getIntFromFile(name + "/type");
        this.connectorPresent = FileUtil.getIntFromFile(name + "/carrier") > 0;
        this.bytesSent = FileUtil.getUnsignedLongFromFile(name + "/statistics/tx_bytes");
        this.bytesRecv = FileUtil.getUnsignedLongFromFile(name + "/statistics/rx_bytes");
        this.packetsSent = FileUtil.getUnsignedLongFromFile(name + "/statistics/tx_packets");
        this.packetsRecv = FileUtil.getUnsignedLongFromFile(name + "/statistics/rx_packets");
        this.outErrors = FileUtil.getUnsignedLongFromFile(name + "/statistics/tx_errors");
        this.inErrors = FileUtil.getUnsignedLongFromFile(name + "/statistics/rx_errors");
        this.collisions = FileUtil.getUnsignedLongFromFile(name + "/statistics/collisions");
        this.inDrops = FileUtil.getUnsignedLongFromFile(name + "/statistics/rx_dropped");
        long speedMbps = FileUtil.getUnsignedLongFromFile(name + "/speed");
        this.speed = speedMbps < 0L ? 0L : speedMbps * 1000000L;
        this.ifAlias = FileUtil.getStringFromFile(name + "/ifalias");
        this.ifOperStatus = LinuxNetworkIF.parseIfOperStatus(FileUtil.getStringFromFile(name + "/operstate"));
        return true;
    }

    private static NetworkIF.IfOperStatus parseIfOperStatus(String operState) {
        switch (operState) {
            case "up": {
                return NetworkIF.IfOperStatus.UP;
            }
            case "down": {
                return NetworkIF.IfOperStatus.DOWN;
            }
            case "testing": {
                return NetworkIF.IfOperStatus.TESTING;
            }
            case "dormant": {
                return NetworkIF.IfOperStatus.DORMANT;
            }
            case "notpresent": {
                return NetworkIF.IfOperStatus.NOT_PRESENT;
            }
            case "lowerlayerdown": {
                return NetworkIF.IfOperStatus.LOWER_LAYER_DOWN;
            }
        }
        return NetworkIF.IfOperStatus.UNKNOWN;
    }
}


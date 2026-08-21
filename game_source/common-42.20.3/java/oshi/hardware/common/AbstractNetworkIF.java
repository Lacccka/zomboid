/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.common;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.NetworkIF;
import oshi.util.FileUtil;
import oshi.util.FormatUtil;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;

@ThreadSafe
public abstract class AbstractNetworkIF
implements NetworkIF {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractNetworkIF.class);
    private static final String OSHI_VM_MAC_ADDR_PROPERTIES = "oshi.vmmacaddr.properties";
    private NetworkInterface networkInterface;
    private String name;
    private String displayName;
    private int index;
    private long mtu;
    private String mac;
    private String[] ipv4;
    private Short[] subnetMasks;
    private String[] ipv6;
    private Short[] prefixLengths;
    private final Supplier<Properties> vmMacAddrProps = Memoizer.memoize(AbstractNetworkIF::queryVmMacAddrProps);

    protected AbstractNetworkIF(NetworkInterface netint) throws InstantiationException {
        this(netint, netint.getDisplayName());
    }

    protected AbstractNetworkIF(NetworkInterface netint, String displayName) throws InstantiationException {
        this.networkInterface = netint;
        try {
            this.name = this.networkInterface.getName();
            this.displayName = displayName;
            this.index = this.networkInterface.getIndex();
            this.mtu = ParseUtil.unsignedIntToLong(this.networkInterface.getMTU());
            byte[] hwmac = this.networkInterface.getHardwareAddress();
            if (hwmac != null) {
                ArrayList<String> octets = new ArrayList<String>(6);
                for (byte b : hwmac) {
                    octets.add(String.format(Locale.ROOT, "%02x", b));
                }
                this.mac = String.join((CharSequence)":", octets);
            } else {
                this.mac = "unknown";
            }
            ArrayList<String> ipv4list = new ArrayList<String>();
            ArrayList<Short> subnetMaskList = new ArrayList<Short>();
            ArrayList<String> ipv6list = new ArrayList<String>();
            ArrayList<Short> prefixLengthList = new ArrayList<Short>();
            for (InterfaceAddress interfaceAddress : this.networkInterface.getInterfaceAddresses()) {
                InetAddress address = interfaceAddress.getAddress();
                if (address.getHostAddress().length() <= 0) continue;
                if (address.getHostAddress().contains(":")) {
                    ipv6list.add(address.getHostAddress().split("%")[0]);
                    prefixLengthList.add(interfaceAddress.getNetworkPrefixLength());
                    continue;
                }
                ipv4list.add(address.getHostAddress());
                subnetMaskList.add(interfaceAddress.getNetworkPrefixLength());
            }
            this.ipv4 = ipv4list.toArray(new String[0]);
            this.subnetMasks = subnetMaskList.toArray(new Short[0]);
            this.ipv6 = ipv6list.toArray(new String[0]);
            this.prefixLengths = prefixLengthList.toArray(new Short[0]);
        }
        catch (SocketException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    protected static List<NetworkInterface> getNetworkInterfaces(boolean includeLocalInterfaces) {
        List<NetworkInterface> interfaces = AbstractNetworkIF.getAllNetworkInterfaces();
        return includeLocalInterfaces ? interfaces : ((Stream)AbstractNetworkIF.getAllNetworkInterfaces().stream().parallel()).filter(ni -> !AbstractNetworkIF.isLocalInterface(ni)).collect(Collectors.toList());
    }

    private static List<NetworkInterface> getAllNetworkInterfaces() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            return interfaces == null ? Collections.emptyList() : Collections.list(interfaces);
        }
        catch (SocketException ex) {
            LOG.error("Socket exception when retrieving interfaces: {}", (Object)ex.getMessage());
            return Collections.emptyList();
        }
    }

    private static boolean isLocalInterface(NetworkInterface networkInterface) {
        try {
            return networkInterface.getHardwareAddress() == null;
        }
        catch (SocketException e) {
            LOG.error("Socket exception when retrieving interface information for {}: {}", (Object)networkInterface, (Object)e.getMessage());
            return false;
        }
    }

    @Override
    public NetworkInterface queryNetworkInterface() {
        return this.networkInterface;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public long getMTU() {
        return this.mtu;
    }

    @Override
    public String getMacaddr() {
        return this.mac;
    }

    @Override
    public String[] getIPv4addr() {
        return Arrays.copyOf(this.ipv4, this.ipv4.length);
    }

    @Override
    public Short[] getSubnetMasks() {
        return Arrays.copyOf(this.subnetMasks, this.subnetMasks.length);
    }

    @Override
    public String[] getIPv6addr() {
        return Arrays.copyOf(this.ipv6, this.ipv6.length);
    }

    @Override
    public Short[] getPrefixLengths() {
        return Arrays.copyOf(this.prefixLengths, this.prefixLengths.length);
    }

    @Override
    public boolean isKnownVmMacAddr() {
        String oui = this.getMacaddr().length() > 7 ? this.getMacaddr().substring(0, 8) : this.getMacaddr();
        return this.vmMacAddrProps.get().containsKey(oui.toUpperCase(Locale.ROOT));
    }

    private static Properties queryVmMacAddrProps() {
        return FileUtil.readPropertiesFromFilename(OSHI_VM_MAC_ADDR_PROPERTIES);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(this.getName());
        if (!this.getName().equals(this.getDisplayName())) {
            sb.append(" (").append(this.getDisplayName()).append(")");
        }
        if (!this.getIfAlias().isEmpty()) {
            sb.append(" [IfAlias=").append(this.getIfAlias()).append("]");
        }
        sb.append("\n");
        sb.append("  MAC Address: ").append(this.getMacaddr()).append("\n");
        sb.append("  MTU: ").append(this.getMTU()).append(", ").append("Speed: ").append(this.getSpeed()).append("\n");
        Object[] ipv4withmask = this.getIPv4addr();
        if (this.ipv4.length == this.subnetMasks.length) {
            for (int i = 0; i < this.subnetMasks.length; ++i) {
                int n = i;
                ipv4withmask[n] = (String)ipv4withmask[n] + "/" + this.subnetMasks[i];
            }
        }
        sb.append("  IPv4: ").append(Arrays.toString(ipv4withmask)).append("\n");
        Object[] ipv6withprefixlength = this.getIPv6addr();
        if (this.ipv6.length == this.prefixLengths.length) {
            for (int j = 0; j < this.prefixLengths.length; ++j) {
                int n = j;
                ipv6withprefixlength[n] = (String)ipv6withprefixlength[n] + "/" + this.prefixLengths[j];
            }
        }
        sb.append("  IPv6: ").append(Arrays.toString(ipv6withprefixlength)).append("\n");
        sb.append("  Traffic: received ").append(this.getPacketsRecv()).append(" packets/").append(FormatUtil.formatBytes(this.getBytesRecv())).append(" (" + this.getInErrors() + " err, ").append(this.getInDrops() + " drop);");
        sb.append(" transmitted ").append(this.getPacketsSent()).append(" packets/").append(FormatUtil.formatBytes(this.getBytesSent())).append(" (" + this.getOutErrors() + " err, ").append(this.getCollisions() + " coll);");
        return sb.toString();
    }
}


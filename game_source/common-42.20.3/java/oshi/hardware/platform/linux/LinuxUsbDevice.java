/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.linux;

import com.sun.jna.platform.linux.Udev;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.UsbDevice;
import oshi.hardware.common.AbstractUsbDevice;
import oshi.software.os.linux.LinuxOperatingSystem;

@Immutable
public class LinuxUsbDevice
extends AbstractUsbDevice {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxUsbDevice.class);
    private static final String SUBSYSTEM_USB = "usb";
    private static final String DEVTYPE_USB_DEVICE = "usb_device";
    private static final String ATTR_PRODUCT = "product";
    private static final String ATTR_MANUFACTURER = "manufacturer";
    private static final String ATTR_VENDOR_ID = "idVendor";
    private static final String ATTR_PRODUCT_ID = "idProduct";
    private static final String ATTR_SERIAL = "serial";

    public LinuxUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber, String uniqueDeviceId, List<UsbDevice> connectedDevices) {
        super(name, vendor, vendorId, productId, serialNumber, uniqueDeviceId, connectedDevices);
    }

    public static List<UsbDevice> getUsbDevices(boolean tree) {
        List<UsbDevice> devices = LinuxUsbDevice.getUsbDevices();
        if (tree) {
            return devices;
        }
        ArrayList<UsbDevice> deviceList = new ArrayList<UsbDevice>();
        for (UsbDevice device : devices) {
            deviceList.add(new LinuxUsbDevice(device.getName(), device.getVendor(), device.getVendorId(), device.getProductId(), device.getSerialNumber(), device.getUniqueDeviceId(), Collections.emptyList()));
            LinuxUsbDevice.addDevicesToList(deviceList, device.getConnectedDevices());
        }
        return deviceList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static List<UsbDevice> getUsbDevices() {
        if (!LinuxOperatingSystem.HAS_UDEV) {
            LOG.warn("USB Device information requires libudev, which is not present.");
            return Collections.emptyList();
        }
        ArrayList<String> usbControllers = new ArrayList<String>();
        HashMap<String, String> nameMap = new HashMap<String, String>();
        HashMap<String, String> vendorMap = new HashMap<String, String>();
        HashMap<String, String> vendorIdMap = new HashMap<String, String>();
        HashMap<String, String> productIdMap = new HashMap<String, String>();
        HashMap<String, String> serialMap = new HashMap<String, String>();
        HashMap<String, List<String>> hubMap = new HashMap<String, List<String>>();
        Udev.UdevContext udev = Udev.INSTANCE.udev_new();
        try {
            Udev.UdevEnumerate enumerate = udev.enumerateNew();
            try {
                enumerate.addMatchSubsystem(SUBSYSTEM_USB);
                enumerate.scanDevices();
                for (Udev.UdevListEntry entry = enumerate.getListEntry(); entry != null; entry = entry.getNext()) {
                    String syspath = entry.getName();
                    Udev.UdevDevice device = udev.deviceNewFromSyspath(syspath);
                    if (device == null) continue;
                    try {
                        Udev.UdevDevice parent;
                        if (!DEVTYPE_USB_DEVICE.equals(device.getDevtype())) continue;
                        String value = device.getSysattrValue(ATTR_PRODUCT);
                        if (value != null) {
                            nameMap.put(syspath, value);
                        }
                        if ((value = device.getSysattrValue(ATTR_MANUFACTURER)) != null) {
                            vendorMap.put(syspath, value);
                        }
                        if ((value = device.getSysattrValue(ATTR_VENDOR_ID)) != null) {
                            vendorIdMap.put(syspath, value);
                        }
                        if ((value = device.getSysattrValue(ATTR_PRODUCT_ID)) != null) {
                            productIdMap.put(syspath, value);
                        }
                        if ((value = device.getSysattrValue(ATTR_SERIAL)) != null) {
                            serialMap.put(syspath, value);
                        }
                        if ((parent = device.getParentWithSubsystemDevtype(SUBSYSTEM_USB, DEVTYPE_USB_DEVICE)) == null) {
                            usbControllers.add(syspath);
                            continue;
                        }
                        String parentPath = parent.getSyspath();
                        hubMap.computeIfAbsent(parentPath, x -> new ArrayList()).add(syspath);
                        continue;
                    }
                    finally {
                        device.unref();
                    }
                }
            }
            finally {
                enumerate.unref();
            }
        }
        finally {
            udev.unref();
        }
        ArrayList<UsbDevice> controllerDevices = new ArrayList<UsbDevice>();
        for (String controller : usbControllers) {
            controllerDevices.add(LinuxUsbDevice.getDeviceAndChildren(controller, "0000", "0000", nameMap, vendorMap, vendorIdMap, productIdMap, serialMap, hubMap));
        }
        return controllerDevices;
    }

    private static void addDevicesToList(List<UsbDevice> deviceList, List<UsbDevice> list) {
        for (UsbDevice device : list) {
            deviceList.add(device);
            LinuxUsbDevice.addDevicesToList(deviceList, device.getConnectedDevices());
        }
    }

    private static LinuxUsbDevice getDeviceAndChildren(String devPath, String vid, String pid, Map<String, String> nameMap, Map<String, String> vendorMap, Map<String, String> vendorIdMap, Map<String, String> productIdMap, Map<String, String> serialMap, Map<String, List<String>> hubMap) {
        String vendorId = vendorIdMap.getOrDefault(devPath, vid);
        String productId = productIdMap.getOrDefault(devPath, pid);
        List childPaths = hubMap.getOrDefault(devPath, new ArrayList());
        ArrayList<UsbDevice> usbDevices = new ArrayList<UsbDevice>();
        for (String path : childPaths) {
            usbDevices.add(LinuxUsbDevice.getDeviceAndChildren(path, vendorId, productId, nameMap, vendorMap, vendorIdMap, productIdMap, serialMap, hubMap));
        }
        Collections.sort(usbDevices);
        return new LinuxUsbDevice(nameMap.getOrDefault(devPath, vendorId + ":" + productId), vendorMap.getOrDefault(devPath, ""), vendorId, productId, serialMap.getOrDefault(devPath, ""), devPath, usbDevices);
    }
}


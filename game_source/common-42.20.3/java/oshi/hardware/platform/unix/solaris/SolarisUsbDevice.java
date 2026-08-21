/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.solaris;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.UsbDevice;
import oshi.hardware.common.AbstractUsbDevice;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@Immutable
public class SolarisUsbDevice
extends AbstractUsbDevice {
    private static final String PCI_TYPE_USB = "000c";

    public SolarisUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber, String uniqueDeviceId, List<UsbDevice> connectedDevices) {
        super(name, vendor, vendorId, productId, serialNumber, uniqueDeviceId, connectedDevices);
    }

    public static List<UsbDevice> getUsbDevices(boolean tree) {
        List<UsbDevice> devices = SolarisUsbDevice.getUsbDevices();
        if (tree) {
            return devices;
        }
        ArrayList<UsbDevice> deviceList = new ArrayList<UsbDevice>();
        for (UsbDevice device : devices) {
            deviceList.add(new SolarisUsbDevice(device.getName(), device.getVendor(), device.getVendorId(), device.getProductId(), device.getSerialNumber(), device.getUniqueDeviceId(), Collections.emptyList()));
            SolarisUsbDevice.addDevicesToList(deviceList, device.getConnectedDevices());
        }
        return deviceList;
    }

    private static List<UsbDevice> getUsbDevices() {
        HashMap<String, String> nameMap = new HashMap<String, String>();
        HashMap<String, String> vendorIdMap = new HashMap<String, String>();
        HashMap<String, String> productIdMap = new HashMap<String, String>();
        HashMap<String, List<String>> hubMap = new HashMap<String, List<String>>();
        HashMap<String, String> deviceTypeMap = new HashMap<String, String>();
        List<String> devices = ExecutingCommand.runNative("prtconf -pv");
        if (devices.isEmpty()) {
            return Collections.emptyList();
        }
        HashMap<Integer, String> lastParent = new HashMap<Integer, String>();
        String key = "";
        int indent = 0;
        ArrayList<String> usbControllers = new ArrayList<String>();
        for (String line : devices) {
            if (line.contains("Node 0x")) {
                key = line.replaceFirst("^\\s*", "");
                int depth = line.length() - key.length();
                if (indent == 0) {
                    indent = depth;
                }
                lastParent.put(depth, key);
                if (depth > indent) {
                    hubMap.computeIfAbsent((String)lastParent.get(depth - indent), x -> new ArrayList()).add(key);
                    continue;
                }
                usbControllers.add(key);
                continue;
            }
            if (key.isEmpty()) continue;
            if ((line = line.trim()).startsWith("model:")) {
                nameMap.put(key, ParseUtil.getSingleQuoteStringValue(line));
                continue;
            }
            if (line.startsWith("name:")) {
                nameMap.putIfAbsent(key, ParseUtil.getSingleQuoteStringValue(line));
                continue;
            }
            if (line.startsWith("vendor-id:")) {
                vendorIdMap.put(key, line.substring(line.length() - 4));
                continue;
            }
            if (line.startsWith("device-id:")) {
                productIdMap.put(key, line.substring(line.length() - 4));
                continue;
            }
            if (line.startsWith("class-code:")) {
                deviceTypeMap.putIfAbsent(key, line.substring(line.length() - 8, line.length() - 4));
                continue;
            }
            if (!line.startsWith("device_type:")) continue;
            deviceTypeMap.putIfAbsent(key, ParseUtil.getSingleQuoteStringValue(line));
        }
        ArrayList<UsbDevice> controllerDevices = new ArrayList<UsbDevice>();
        for (String controller : usbControllers) {
            if (!PCI_TYPE_USB.equals(deviceTypeMap.getOrDefault(controller, "")) && !"usb".equals(deviceTypeMap.getOrDefault(controller, ""))) continue;
            controllerDevices.add(SolarisUsbDevice.getDeviceAndChildren(controller, "0000", "0000", nameMap, vendorIdMap, productIdMap, hubMap));
        }
        return controllerDevices;
    }

    private static void addDevicesToList(List<UsbDevice> deviceList, List<UsbDevice> list) {
        for (UsbDevice device : list) {
            deviceList.add(device);
            SolarisUsbDevice.addDevicesToList(deviceList, device.getConnectedDevices());
        }
    }

    private static SolarisUsbDevice getDeviceAndChildren(String devPath, String vid, String pid, Map<String, String> nameMap, Map<String, String> vendorIdMap, Map<String, String> productIdMap, Map<String, List<String>> hubMap) {
        String vendorId = vendorIdMap.getOrDefault(devPath, vid);
        String productId = productIdMap.getOrDefault(devPath, pid);
        List childPaths = hubMap.getOrDefault(devPath, new ArrayList());
        ArrayList<UsbDevice> usbDevices = new ArrayList<UsbDevice>();
        for (String path : childPaths) {
            usbDevices.add(SolarisUsbDevice.getDeviceAndChildren(path, vendorId, productId, nameMap, vendorIdMap, productIdMap, hubMap));
        }
        Collections.sort(usbDevices);
        return new SolarisUsbDevice(nameMap.getOrDefault(devPath, vendorId + ":" + productId), "", vendorId, productId, "", devPath, usbDevices);
    }
}


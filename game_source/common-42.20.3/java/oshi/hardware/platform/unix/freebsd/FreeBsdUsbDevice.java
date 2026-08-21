/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.freebsd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.UsbDevice;
import oshi.hardware.common.AbstractUsbDevice;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@Immutable
public class FreeBsdUsbDevice
extends AbstractUsbDevice {
    public FreeBsdUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber, String uniqueDeviceId, List<UsbDevice> connectedDevices) {
        super(name, vendor, vendorId, productId, serialNumber, uniqueDeviceId, connectedDevices);
    }

    public static List<UsbDevice> getUsbDevices(boolean tree) {
        List<UsbDevice> devices = FreeBsdUsbDevice.getUsbDevices();
        if (tree) {
            return devices;
        }
        ArrayList<UsbDevice> deviceList = new ArrayList<UsbDevice>();
        for (UsbDevice device : devices) {
            deviceList.add(new FreeBsdUsbDevice(device.getName(), device.getVendor(), device.getVendorId(), device.getProductId(), device.getSerialNumber(), device.getUniqueDeviceId(), Collections.emptyList()));
            FreeBsdUsbDevice.addDevicesToList(deviceList, device.getConnectedDevices());
        }
        return deviceList;
    }

    private static List<UsbDevice> getUsbDevices() {
        HashMap<String, String> nameMap = new HashMap<String, String>();
        HashMap<String, String> vendorMap = new HashMap<String, String>();
        HashMap<String, String> vendorIdMap = new HashMap<String, String>();
        HashMap<String, String> productIdMap = new HashMap<String, String>();
        HashMap<String, String> serialMap = new HashMap<String, String>();
        HashMap<String, String> parentMap = new HashMap<String, String>();
        HashMap<String, List<String>> hubMap = new HashMap<String, List<String>>();
        List<String> devices = ExecutingCommand.runNative("lshal");
        if (devices.isEmpty()) {
            return Collections.emptyList();
        }
        String key = "";
        ArrayList<String> usBuses = new ArrayList<String>();
        for (String line : devices) {
            if (line.startsWith("udi =")) {
                key = ParseUtil.getSingleQuoteStringValue(line);
                continue;
            }
            if (key.isEmpty() || (line = line.trim()).isEmpty()) continue;
            if (line.startsWith("freebsd.driver =") && "usbus".equals(ParseUtil.getSingleQuoteStringValue(line))) {
                usBuses.add(key);
                continue;
            }
            if (line.contains(".parent =")) {
                String parent = ParseUtil.getSingleQuoteStringValue(line);
                if (key.replace(parent, "").startsWith("_if")) continue;
                parentMap.put(key, parent);
                hubMap.computeIfAbsent(parent, x -> new ArrayList()).add(key);
                continue;
            }
            if (line.contains(".product =")) {
                nameMap.put(key, ParseUtil.getSingleQuoteStringValue(line));
                continue;
            }
            if (line.contains(".vendor =")) {
                vendorMap.put(key, ParseUtil.getSingleQuoteStringValue(line));
                continue;
            }
            if (line.contains(".serial =")) {
                String serial = ParseUtil.getSingleQuoteStringValue(line);
                serialMap.put(key, serial.startsWith("0x") ? ParseUtil.hexStringToString(serial.replace("0x", "")) : serial);
                continue;
            }
            if (line.contains(".vendor_id =")) {
                vendorIdMap.put(key, String.format(Locale.ROOT, "%04x", ParseUtil.getFirstIntValue(line)));
                continue;
            }
            if (!line.contains(".product_id =")) continue;
            productIdMap.put(key, String.format(Locale.ROOT, "%04x", ParseUtil.getFirstIntValue(line)));
        }
        ArrayList<UsbDevice> controllerDevices = new ArrayList<UsbDevice>();
        for (String usbus : usBuses) {
            String parent = (String)parentMap.get(usbus);
            hubMap.put(parent, (List)hubMap.get(usbus));
            controllerDevices.add(FreeBsdUsbDevice.getDeviceAndChildren(parent, "0000", "0000", nameMap, vendorMap, vendorIdMap, productIdMap, serialMap, hubMap));
        }
        return controllerDevices;
    }

    private static void addDevicesToList(List<UsbDevice> deviceList, List<UsbDevice> list) {
        for (UsbDevice device : list) {
            deviceList.add(device);
            FreeBsdUsbDevice.addDevicesToList(deviceList, device.getConnectedDevices());
        }
    }

    private static FreeBsdUsbDevice getDeviceAndChildren(String devPath, String vid, String pid, Map<String, String> nameMap, Map<String, String> vendorMap, Map<String, String> vendorIdMap, Map<String, String> productIdMap, Map<String, String> serialMap, Map<String, List<String>> hubMap) {
        String vendorId = vendorIdMap.getOrDefault(devPath, vid);
        String productId = productIdMap.getOrDefault(devPath, pid);
        List childPaths = hubMap.getOrDefault(devPath, new ArrayList());
        ArrayList<UsbDevice> usbDevices = new ArrayList<UsbDevice>();
        for (String path : childPaths) {
            usbDevices.add(FreeBsdUsbDevice.getDeviceAndChildren(path, vendorId, productId, nameMap, vendorMap, vendorIdMap, productIdMap, serialMap, hubMap));
        }
        Collections.sort(usbDevices);
        return new FreeBsdUsbDevice(nameMap.getOrDefault(devPath, vendorId + ":" + productId), vendorMap.getOrDefault(devPath, ""), vendorId, productId, serialMap.getOrDefault(devPath, ""), devPath, usbDevices);
    }
}


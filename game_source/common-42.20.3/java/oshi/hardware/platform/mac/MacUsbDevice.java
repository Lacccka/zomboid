/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.IOKit;
import com.sun.jna.platform.mac.IOKitUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.UsbDevice;
import oshi.hardware.common.AbstractUsbDevice;

@Immutable
public class MacUsbDevice
extends AbstractUsbDevice {
    private static final CoreFoundation CF = CoreFoundation.INSTANCE;
    private static final String IOUSB = "IOUSB";
    private static final String IOSERVICE = "IOService";

    public MacUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber, String uniqueDeviceId, List<UsbDevice> connectedDevices) {
        super(name, vendor, vendorId, productId, serialNumber, uniqueDeviceId, connectedDevices);
    }

    public static List<UsbDevice> getUsbDevices(boolean tree) {
        List<UsbDevice> devices = MacUsbDevice.getUsbDevices();
        if (tree) {
            return devices;
        }
        ArrayList<UsbDevice> deviceList = new ArrayList<UsbDevice>();
        for (UsbDevice device : devices) {
            MacUsbDevice.addDevicesToList(deviceList, device.getConnectedDevices());
        }
        return deviceList;
    }

    private static List<UsbDevice> getUsbDevices() {
        HashMap<Long, String> nameMap = new HashMap<Long, String>();
        HashMap<Long, String> vendorMap = new HashMap<Long, String>();
        HashMap<Long, String> vendorIdMap = new HashMap<Long, String>();
        HashMap<Long, String> productIdMap = new HashMap<Long, String>();
        HashMap<Long, String> serialMap = new HashMap<Long, String>();
        HashMap<Long, List<Long>> hubMap = new HashMap<Long, List<Long>>();
        ArrayList<Long> usbControllers = new ArrayList<Long>();
        IOKit.IORegistryEntry root = IOKitUtil.getRoot();
        IOKit.IOIterator iter = root.getChildIterator(IOUSB);
        if (iter != null) {
            CoreFoundation.CFStringRef locationIDKey = CoreFoundation.CFStringRef.createCFString("locationID");
            CoreFoundation.CFStringRef ioPropertyMatchKey = CoreFoundation.CFStringRef.createCFString("IOPropertyMatch");
            IOKit.IORegistryEntry device = iter.next();
            while (device != null) {
                long id = 0L;
                IOKit.IORegistryEntry controller = device.getParentEntry(IOSERVICE);
                if (controller != null) {
                    id = controller.getRegistryEntryID();
                    nameMap.put(id, controller.getName());
                    CoreFoundation.CFTypeRef ref = controller.createCFProperty(locationIDKey);
                    if (ref != null) {
                        MacUsbDevice.getControllerIdByLocation(id, ref, locationIDKey, ioPropertyMatchKey, vendorIdMap, productIdMap);
                        ref.release();
                    }
                    controller.release();
                }
                usbControllers.add(id);
                MacUsbDevice.addDeviceAndChildrenToMaps(device, id, nameMap, vendorMap, vendorIdMap, productIdMap, serialMap, hubMap);
                device.release();
                device = iter.next();
            }
            locationIDKey.release();
            ioPropertyMatchKey.release();
            iter.release();
        }
        root.release();
        ArrayList<UsbDevice> controllerDevices = new ArrayList<UsbDevice>();
        for (Long controller : usbControllers) {
            controllerDevices.add(MacUsbDevice.getDeviceAndChildren(controller, "0000", "0000", nameMap, vendorMap, vendorIdMap, productIdMap, serialMap, hubMap));
        }
        return controllerDevices;
    }

    private static void addDeviceAndChildrenToMaps(IOKit.IORegistryEntry device, long parentId, Map<Long, String> nameMap, Map<Long, String> vendorMap, Map<Long, String> vendorIdMap, Map<Long, String> productIdMap, Map<Long, String> serialMap, Map<Long, List<Long>> hubMap) {
        String serial;
        Long productId;
        Long vendorId;
        long id = device.getRegistryEntryID();
        hubMap.computeIfAbsent(parentId, x -> new ArrayList()).add(id);
        nameMap.put(id, device.getName().trim());
        String vendor = device.getStringProperty("USB Vendor Name");
        if (vendor != null) {
            vendorMap.put(id, vendor.trim());
        }
        if ((vendorId = device.getLongProperty("idVendor")) != null) {
            vendorIdMap.put(id, String.format(Locale.ROOT, "%04x", 0xFFFFL & vendorId));
        }
        if ((productId = device.getLongProperty("idProduct")) != null) {
            productIdMap.put(id, String.format(Locale.ROOT, "%04x", 0xFFFFL & productId));
        }
        if ((serial = device.getStringProperty("USB Serial Number")) != null) {
            serialMap.put(id, serial.trim());
        }
        IOKit.IOIterator childIter = device.getChildIterator(IOUSB);
        IOKit.IORegistryEntry childDevice = childIter.next();
        while (childDevice != null) {
            MacUsbDevice.addDeviceAndChildrenToMaps(childDevice, id, nameMap, vendorMap, vendorIdMap, productIdMap, serialMap, hubMap);
            childDevice.release();
            childDevice = childIter.next();
        }
        childIter.release();
    }

    private static void addDevicesToList(List<UsbDevice> deviceList, List<UsbDevice> list) {
        for (UsbDevice device : list) {
            deviceList.add(new MacUsbDevice(device.getName(), device.getVendor(), device.getVendorId(), device.getProductId(), device.getSerialNumber(), device.getUniqueDeviceId(), Collections.emptyList()));
            MacUsbDevice.addDevicesToList(deviceList, device.getConnectedDevices());
        }
    }

    private static void getControllerIdByLocation(long id, CoreFoundation.CFTypeRef locationId, CoreFoundation.CFStringRef locationIDKey, CoreFoundation.CFStringRef ioPropertyMatchKey, Map<Long, String> vendorIdMap, Map<Long, String> productIdMap) {
        CoreFoundation.CFMutableDictionaryRef propertyDict = CF.CFDictionaryCreateMutable(CF.CFAllocatorGetDefault(), new CoreFoundation.CFIndex(0L), null, null);
        propertyDict.setValue(locationIDKey, locationId);
        CoreFoundation.CFMutableDictionaryRef matchingDict = CF.CFDictionaryCreateMutable(CF.CFAllocatorGetDefault(), new CoreFoundation.CFIndex(0L), null, null);
        matchingDict.setValue(ioPropertyMatchKey, propertyDict);
        IOKit.IOIterator serviceIterator = IOKitUtil.getMatchingServices(matchingDict);
        propertyDict.release();
        boolean found = false;
        if (serviceIterator != null) {
            IOKit.IORegistryEntry matchingService = serviceIterator.next();
            while (matchingService != null && !found) {
                IOKit.IORegistryEntry parent = matchingService.getParentEntry(IOSERVICE);
                if (parent != null) {
                    byte[] pid;
                    byte[] vid = parent.getByteArrayProperty("vendor-id");
                    if (vid != null && vid.length >= 2) {
                        vendorIdMap.put(id, String.format(Locale.ROOT, "%02x%02x", vid[1], vid[0]));
                        found = true;
                    }
                    if ((pid = parent.getByteArrayProperty("device-id")) != null && pid.length >= 2) {
                        productIdMap.put(id, String.format(Locale.ROOT, "%02x%02x", pid[1], pid[0]));
                        found = true;
                    }
                    parent.release();
                }
                matchingService.release();
                matchingService = serviceIterator.next();
            }
            serviceIterator.release();
        }
    }

    private static MacUsbDevice getDeviceAndChildren(Long registryEntryId, String vid, String pid, Map<Long, String> nameMap, Map<Long, String> vendorMap, Map<Long, String> vendorIdMap, Map<Long, String> productIdMap, Map<Long, String> serialMap, Map<Long, List<Long>> hubMap) {
        String vendorId = vendorIdMap.getOrDefault(registryEntryId, vid);
        String productId = productIdMap.getOrDefault(registryEntryId, pid);
        List childIds = hubMap.getOrDefault(registryEntryId, new ArrayList());
        ArrayList<UsbDevice> usbDevices = new ArrayList<UsbDevice>();
        for (Long id : childIds) {
            usbDevices.add(MacUsbDevice.getDeviceAndChildren(id, vendorId, productId, nameMap, vendorMap, vendorIdMap, productIdMap, serialMap, hubMap));
        }
        Collections.sort(usbDevices);
        return new MacUsbDevice(nameMap.getOrDefault(registryEntryId, vendorId + ":" + productId), vendorMap.getOrDefault(registryEntryId, ""), vendorId, productId, serialMap.getOrDefault(registryEntryId, ""), "0x" + Long.toHexString(registryEntryId), usbDevices);
    }
}


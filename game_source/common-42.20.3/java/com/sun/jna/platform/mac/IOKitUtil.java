/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.mac;

import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.IOKit;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class IOKitUtil {
    private static final IOKit IO = IOKit.INSTANCE;
    private static final SystemB SYS = SystemB.INSTANCE;

    private IOKitUtil() {
    }

    public static int getMasterPort() {
        IntByReference port = new IntByReference();
        IO.IOMasterPort(0, port);
        return port.getValue();
    }

    public static IOKit.IORegistryEntry getRoot() {
        int masterPort = IOKitUtil.getMasterPort();
        IOKit.IORegistryEntry root = IO.IORegistryGetRootEntry(masterPort);
        SYS.mach_port_deallocate(SYS.mach_task_self(), masterPort);
        return root;
    }

    public static IOKit.IOService getMatchingService(String serviceName) {
        CoreFoundation.CFMutableDictionaryRef dict = IO.IOServiceMatching(serviceName);
        if (dict != null) {
            return IOKitUtil.getMatchingService(dict);
        }
        return null;
    }

    public static IOKit.IOService getMatchingService(CoreFoundation.CFDictionaryRef matchingDictionary) {
        int masterPort = IOKitUtil.getMasterPort();
        IOKit.IOService service = IO.IOServiceGetMatchingService(masterPort, matchingDictionary);
        SYS.mach_port_deallocate(SYS.mach_task_self(), masterPort);
        return service;
    }

    public static IOKit.IOIterator getMatchingServices(String serviceName) {
        CoreFoundation.CFMutableDictionaryRef dict = IO.IOServiceMatching(serviceName);
        if (dict != null) {
            return IOKitUtil.getMatchingServices(dict);
        }
        return null;
    }

    public static IOKit.IOIterator getMatchingServices(CoreFoundation.CFDictionaryRef matchingDictionary) {
        int masterPort = IOKitUtil.getMasterPort();
        PointerByReference serviceIterator = new PointerByReference();
        int result = IO.IOServiceGetMatchingServices(masterPort, matchingDictionary, serviceIterator);
        SYS.mach_port_deallocate(SYS.mach_task_self(), masterPort);
        if (result == 0 && serviceIterator.getValue() != null) {
            return new IOKit.IOIterator(serviceIterator.getValue());
        }
        return null;
    }

    public static CoreFoundation.CFMutableDictionaryRef getBSDNameMatchingDict(String bsdName) {
        int masterPort = IOKitUtil.getMasterPort();
        CoreFoundation.CFMutableDictionaryRef result = IO.IOBSDNameMatching(masterPort, 0, bsdName);
        SYS.mach_port_deallocate(SYS.mach_task_self(), masterPort);
        return result;
    }
}


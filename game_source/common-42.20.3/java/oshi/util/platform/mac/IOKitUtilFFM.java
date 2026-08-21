/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.mac;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import oshi.ffm.mac.IOKit;
import oshi.ffm.mac.IOKitFunctions;
import oshi.ffm.mac.MacSystemFunctions;

public final class IOKitUtilFFM {
    private IOKitUtilFFM() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static int getMasterPort() {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment port = arena.allocate(ValueLayout.JAVA_INT);
            int result = IOKitFunctions.IOMasterPort(0, port);
            if (result == 0) {
                int n = port.get(ValueLayout.JAVA_INT, 0L);
                return n;
            }
            int n = 0;
            return n;
        }
        catch (Throwable e) {
            return 0;
        }
    }

    public static IOKit.IORegistryEntry getRoot() {
        try {
            int masterPort = IOKitUtilFFM.getMasterPort();
            MemorySegment root = IOKitFunctions.IORegistryGetRootEntry(masterPort);
            IOKitUtilFFM.deallocatePort(masterPort);
            return root.equals(MemorySegment.NULL) ? null : new IOKit.IORegistryEntry(root);
        }
        catch (Throwable e) {
            return null;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static IOKit.IOService getMatchingService(String serviceName) {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment nameStr = arena.allocateFrom(serviceName);
            MemorySegment dict = IOKitFunctions.IOServiceMatching(nameStr);
            if (dict != null && !dict.equals(MemorySegment.NULL)) {
                IOKit.IOService iOService = IOKitUtilFFM.getMatchingService(dict);
                return iOService;
            }
            IOKit.IOService iOService = null;
            return iOService;
        }
        catch (Throwable e) {
            return null;
        }
    }

    public static IOKit.IOService getMatchingService(MemorySegment matchingDictionary) {
        try {
            int masterPort = IOKitUtilFFM.getMasterPort();
            MemorySegment service = IOKitFunctions.IOServiceGetMatchingService(masterPort, matchingDictionary);
            IOKitUtilFFM.deallocatePort(masterPort);
            return service.equals(MemorySegment.NULL) ? null : new IOKit.IOService(service);
        }
        catch (Throwable e) {
            return null;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static IOKit.IOIterator getMatchingServices(String serviceName) {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment nameStr = arena.allocateFrom(serviceName);
            MemorySegment dict = IOKitFunctions.IOServiceMatching(nameStr);
            if (dict != null && !dict.equals(MemorySegment.NULL)) {
                IOKit.IOIterator iOIterator = IOKitUtilFFM.getMatchingServices(dict);
                return iOIterator;
            }
            IOKit.IOIterator iOIterator = null;
            return iOIterator;
        }
        catch (Throwable e) {
            return null;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static IOKit.IOIterator getMatchingServices(MemorySegment matchingDictionary) {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment iterator2;
            int masterPort = IOKitUtilFFM.getMasterPort();
            MemorySegment iteratorSeg = arena.allocate(ValueLayout.ADDRESS);
            int result = IOKitFunctions.IOServiceGetMatchingServices(masterPort, matchingDictionary, iteratorSeg);
            IOKitUtilFFM.deallocatePort(masterPort);
            if (result == 0 && !(iterator2 = iteratorSeg.get(ValueLayout.ADDRESS, 0L)).equals(MemorySegment.NULL)) {
                IOKit.IOIterator iOIterator = new IOKit.IOIterator(iterator2);
                return iOIterator;
            }
            IOKit.IOIterator iOIterator = null;
            return iOIterator;
        }
        catch (Throwable e) {
            return null;
        }
    }

    public static MemorySegment getBSDNameMatchingDict(String bsdName) {
        Arena arena = Arena.ofConfined();
        try {
            int masterPort = IOKitUtilFFM.getMasterPort();
            MemorySegment bsdNameStr = arena.allocateFrom(bsdName);
            MemorySegment result = IOKitFunctions.IOBSDNameMatching(masterPort, 0, bsdNameStr);
            IOKitUtilFFM.deallocatePort(masterPort);
            MemorySegment memorySegment = result;
            if (arena != null) {
                arena.close();
            }
            return memorySegment;
        }
        catch (Throwable throwable) {
            try {
                if (arena != null) {
                    try {
                        arena.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Throwable e) {
                return null;
            }
        }
    }

    private static void deallocatePort(int port) {
        try {
            if (port != 0) {
                MacSystemFunctions.mach_port_deallocate(MacSystemFunctions.mach_task_self(), port);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}


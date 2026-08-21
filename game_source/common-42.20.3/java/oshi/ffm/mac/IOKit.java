/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.mac;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;
import oshi.ffm.ForeignFunctions;
import oshi.ffm.mac.CoreFoundationFunctions;
import oshi.ffm.mac.IOKitFunctions;

public interface IOKit {
    public static final int kIORegistryIterateRecursively = 1;
    public static final int kIORegistryIterateParents = 2;
    public static final int kIOReturnNoDevice = -536870208;
    public static final double kIOPSTimeRemainingUnlimited = -2.0;
    public static final double kIOPSTimeRemainingUnknown = -1.0;

    private static void releaseCFObjects(MemorySegment ... cfObjects) {
        for (MemorySegment segment : cfObjects) {
            if (segment == null || segment.equals(MemorySegment.NULL)) continue;
            try {
                CoreFoundationFunctions.CFRelease(segment);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public static class IOService
    extends IORegistryEntry {
        public IOService(MemorySegment segment) {
            super(segment);
        }
    }

    public static class IORegistryEntry
    extends IOObject {
        public IORegistryEntry(MemorySegment segment) {
            super(segment);
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public long getRegistryEntryID() {
            if (this.isNull()) {
                return 0L;
            }
            try (Arena arena = Arena.ofConfined();){
                MemorySegment idSeg = arena.allocate(ValueLayout.JAVA_LONG);
                int result = IOKitFunctions.IORegistryEntryGetRegistryEntryID(this.segment(), idSeg);
                if (result != 0) {
                    long l = 0L;
                    return l;
                }
                long l = idSeg.get(ValueLayout.JAVA_LONG, 0L);
                return l;
            }
            catch (Throwable e) {
                return 0L;
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public String getName() {
            if (this.isNull()) {
                return null;
            }
            try (Arena arena = Arena.ofConfined();){
                MemorySegment nameSeg = arena.allocate(128L);
                int result = IOKitFunctions.IORegistryEntryGetName(this.segment(), nameSeg);
                if (result != 0) {
                    String string = null;
                    return string;
                }
                String string = nameSeg.getString(0L);
                return string;
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
        public IOIterator getChildIterator(String plane) {
            if (this.isNull()) {
                return null;
            }
            try (Arena arena = Arena.ofConfined();){
                MemorySegment planeStr = arena.allocateFrom(plane);
                MemorySegment iterSeg = arena.allocate(ValueLayout.ADDRESS);
                int result = IOKitFunctions.IORegistryEntryGetChildIterator(this.segment(), planeStr, iterSeg);
                if (result != 0) {
                    IOIterator iOIterator2 = null;
                    return iOIterator2;
                }
                MemorySegment iterator2 = iterSeg.get(ValueLayout.ADDRESS, 0L);
                IOIterator iOIterator = iterator2.equals(MemorySegment.NULL) ? null : new IOIterator(iterator2);
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
        public IORegistryEntry getChildEntry(String plane) {
            if (this.isNull()) {
                return null;
            }
            try (Arena arena = Arena.ofConfined();){
                MemorySegment planeStr = arena.allocateFrom(plane);
                MemorySegment childSeg = arena.allocate(ValueLayout.ADDRESS);
                int result = IOKitFunctions.IORegistryEntryGetChildEntry(this.segment(), planeStr, childSeg);
                if (result == -536870208 || result != 0) {
                    IORegistryEntry iORegistryEntry2 = null;
                    return iORegistryEntry2;
                }
                MemorySegment child = childSeg.get(ValueLayout.ADDRESS, 0L);
                IORegistryEntry iORegistryEntry = child.equals(MemorySegment.NULL) ? null : new IORegistryEntry(child);
                return iORegistryEntry;
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
        public IORegistryEntry getParentEntry(String plane) {
            if (this.isNull()) {
                return null;
            }
            try (Arena arena = Arena.ofConfined();){
                MemorySegment planeStr = arena.allocateFrom(plane);
                MemorySegment parentSeg = arena.allocate(ValueLayout.ADDRESS);
                int result = IOKitFunctions.IORegistryEntryGetParentEntry(this.segment(), planeStr, parentSeg);
                if (result == -536870208 || result != 0) {
                    IORegistryEntry iORegistryEntry2 = null;
                    return iORegistryEntry2;
                }
                MemorySegment parent = parentSeg.get(ValueLayout.ADDRESS, 0L);
                IORegistryEntry iORegistryEntry = parent.equals(MemorySegment.NULL) ? null : new IORegistryEntry(parent);
                return iORegistryEntry;
            }
            catch (Throwable e) {
                return null;
            }
        }

        public MemorySegment createCFProperty(MemorySegment key) {
            if (this.isNull()) {
                return MemorySegment.NULL;
            }
            try {
                MemorySegment allocator = CoreFoundationFunctions.CFAllocatorGetDefault();
                return IOKitFunctions.IORegistryEntryCreateCFProperty(this.segment(), key, allocator, 0);
            }
            catch (Throwable e) {
                return MemorySegment.NULL;
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public MemorySegment createCFProperties() {
            if (this.isNull()) {
                return MemorySegment.NULL;
            }
            try (Arena arena = Arena.ofConfined();){
                MemorySegment propsSeg = arena.allocate(ValueLayout.ADDRESS);
                MemorySegment allocator = CoreFoundationFunctions.CFAllocatorGetDefault();
                int result = IOKitFunctions.IORegistryEntryCreateCFProperties(this.segment(), propsSeg, allocator, 0);
                if (result != 0) {
                    MemorySegment memorySegment = MemorySegment.NULL;
                    return memorySegment;
                }
                MemorySegment memorySegment = propsSeg.get(ValueLayout.ADDRESS, 0L);
                return memorySegment;
            }
            catch (Throwable e) {
                return MemorySegment.NULL;
            }
        }

        public MemorySegment searchCFProperty(String plane, MemorySegment key, int options) {
            if (this.isNull()) {
                return MemorySegment.NULL;
            }
            Arena arena = Arena.ofConfined();
            try {
                MemorySegment planeStr = arena.allocateFrom(plane);
                MemorySegment allocator = CoreFoundationFunctions.CFAllocatorGetDefault();
                MemorySegment memorySegment = IOKitFunctions.IORegistryEntrySearchCFProperty(this.segment(), planeStr, key, allocator, options);
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
                    return MemorySegment.NULL;
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive exception aggregation
         */
        public String getStringProperty(String key) {
            try (Arena arena = Arena.ofConfined();){
                MemorySegment cfValue;
                MemorySegment cfKey;
                block13: {
                    String string;
                    cfKey = IORegistryEntry.createCFString(key, arena);
                    cfValue = null;
                    try {
                        cfValue = this.createCFProperty(cfKey);
                        if (!cfValue.equals(MemorySegment.NULL)) break block13;
                        string = null;
                    }
                    catch (Throwable throwable) {
                        IOKit.releaseCFObjects(cfKey, cfValue);
                        throw throwable;
                    }
                    IOKit.releaseCFObjects(cfKey, cfValue);
                    return string;
                }
                String string = IORegistryEntry.getStringFromCFString(cfValue, arena);
                IOKit.releaseCFObjects(cfKey, cfValue);
                return string;
            }
            catch (Throwable e) {
                return null;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive exception aggregation
         */
        public Long getLongProperty(String key) {
            try (Arena arena = Arena.ofConfined();){
                MemorySegment cfValue;
                MemorySegment cfKey;
                block16: {
                    block15: {
                        Long l;
                        cfKey = IORegistryEntry.createCFString(key, arena);
                        cfValue = null;
                        try {
                            cfValue = this.createCFProperty(cfKey);
                            if (!cfValue.equals(MemorySegment.NULL)) break block15;
                            l = null;
                        }
                        catch (Throwable throwable) {
                            IOKit.releaseCFObjects(cfKey, cfValue);
                            throw throwable;
                        }
                        IOKit.releaseCFObjects(cfKey, cfValue);
                        return l;
                    }
                    MemorySegment valuePtr = arena.allocate(ValueLayout.JAVA_LONG);
                    if (!CoreFoundationFunctions.CFNumberGetValue(cfValue, 4, valuePtr)) break block16;
                    Long l = valuePtr.get(ValueLayout.JAVA_LONG, 0L);
                    IOKit.releaseCFObjects(cfKey, cfValue);
                    return l;
                }
                Long l = null;
                IOKit.releaseCFObjects(cfKey, cfValue);
                return l;
            }
            catch (Throwable e) {
                return null;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive exception aggregation
         */
        public Integer getIntegerProperty(String key) {
            try (Arena arena = Arena.ofConfined();){
                MemorySegment cfValue;
                MemorySegment cfKey;
                block16: {
                    block15: {
                        Integer n;
                        cfKey = IORegistryEntry.createCFString(key, arena);
                        cfValue = null;
                        try {
                            cfValue = this.createCFProperty(cfKey);
                            if (!cfValue.equals(MemorySegment.NULL)) break block15;
                            n = null;
                        }
                        catch (Throwable throwable) {
                            IOKit.releaseCFObjects(cfKey, cfValue);
                            throw throwable;
                        }
                        IOKit.releaseCFObjects(cfKey, cfValue);
                        return n;
                    }
                    MemorySegment valuePtr = arena.allocate(ValueLayout.JAVA_INT);
                    if (!CoreFoundationFunctions.CFNumberGetValue(cfValue, 3, valuePtr)) break block16;
                    Integer n = valuePtr.get(ValueLayout.JAVA_INT, 0L);
                    IOKit.releaseCFObjects(cfKey, cfValue);
                    return n;
                }
                Integer n = null;
                IOKit.releaseCFObjects(cfKey, cfValue);
                return n;
            }
            catch (Throwable e) {
                return null;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive exception aggregation
         */
        public Double getDoubleProperty(String key) {
            try (Arena arena = Arena.ofConfined();){
                MemorySegment cfValue;
                MemorySegment cfKey;
                block16: {
                    block15: {
                        Double d;
                        cfKey = IORegistryEntry.createCFString(key, arena);
                        cfValue = null;
                        try {
                            cfValue = this.createCFProperty(cfKey);
                            if (!cfValue.equals(MemorySegment.NULL)) break block15;
                            d = null;
                        }
                        catch (Throwable throwable) {
                            IOKit.releaseCFObjects(cfKey, cfValue);
                            throw throwable;
                        }
                        IOKit.releaseCFObjects(cfKey, cfValue);
                        return d;
                    }
                    MemorySegment valuePtr = arena.allocate(ValueLayout.JAVA_DOUBLE);
                    if (!CoreFoundationFunctions.CFNumberGetValue(cfValue, 6, valuePtr)) break block16;
                    Double d = valuePtr.get(ValueLayout.JAVA_DOUBLE, 0L);
                    IOKit.releaseCFObjects(cfKey, cfValue);
                    return d;
                }
                Double d = null;
                IOKit.releaseCFObjects(cfKey, cfValue);
                return d;
            }
            catch (Throwable e) {
                return null;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive exception aggregation
         */
        public byte[] getByteArrayProperty(String key) {
            try (Arena arena = Arena.ofConfined();){
                MemorySegment cfValue;
                MemorySegment cfKey;
                block13: {
                    byte[] byArray;
                    cfKey = IORegistryEntry.createCFString(key, arena);
                    cfValue = null;
                    try {
                        cfValue = this.createCFProperty(cfKey);
                        if (!cfValue.equals(MemorySegment.NULL)) break block13;
                        byArray = null;
                    }
                    catch (Throwable throwable) {
                        IOKit.releaseCFObjects(cfKey, cfValue);
                        throw throwable;
                    }
                    IOKit.releaseCFObjects(cfKey, cfValue);
                    return byArray;
                }
                long length = CoreFoundationFunctions.CFDataGetLength(cfValue);
                MemorySegment bytePtr = CoreFoundationFunctions.CFDataGetBytePtr(cfValue);
                byte[] byArray = ForeignFunctions.getByteArrayFromNativePointer(bytePtr, length, arena);
                IOKit.releaseCFObjects(cfKey, cfValue);
                return byArray;
            }
            catch (Throwable e) {
                return null;
            }
        }

        private static MemorySegment createCFString(String str, Arena arena) throws Throwable {
            char[] chars = str.toCharArray();
            MemorySegment charsSeg = arena.allocateFrom(ValueLayout.JAVA_CHAR, chars);
            MemorySegment allocator = CoreFoundationFunctions.CFAllocatorGetDefault();
            return CoreFoundationFunctions.CFStringCreateWithCharacters(allocator, charsSeg, chars.length);
        }

        private static String getStringFromCFString(MemorySegment cfString, Arena arena) throws Throwable {
            if (cfString.equals(MemorySegment.NULL)) {
                return null;
            }
            long length = CoreFoundationFunctions.CFStringGetLength(cfString);
            long bufSize = length * 4L + 1L;
            MemorySegment buffer = arena.allocate(bufSize);
            boolean success = CoreFoundationFunctions.CFStringGetCString(cfString, buffer, bufSize, 0x8000100);
            if (!success) {
                return null;
            }
            return buffer.getString(0L);
        }
    }

    public static class IOIterator
    extends IOObject {
        public IOIterator(MemorySegment segment) {
            super(segment);
        }

        public IORegistryEntry next() {
            if (this.isNull()) {
                return null;
            }
            try {
                MemorySegment nextObj = IOKitFunctions.IOIteratorNext(this.segment());
                return nextObj.equals(MemorySegment.NULL) ? null : new IORegistryEntry(nextObj);
            }
            catch (Throwable e) {
                return null;
            }
        }

        public List<IORegistryEntry> listEntries() {
            IORegistryEntry entry;
            ArrayList<IORegistryEntry> entries = new ArrayList<IORegistryEntry>();
            if (this.isNull()) {
                return entries;
            }
            while ((entry = this.next()) != null) {
                entries.add(entry);
            }
            return entries;
        }
    }

    public static class IOObject {
        private final MemorySegment segment;

        public IOObject(MemorySegment segment) {
            this.segment = segment;
        }

        public MemorySegment segment() {
            return this.segment;
        }

        public boolean isNull() {
            return this.segment == null || this.segment.equals(MemorySegment.NULL);
        }

        public int release() {
            if (this.isNull()) {
                return 0;
            }
            try {
                return IOKitFunctions.IOObjectRelease(this.segment);
            }
            catch (Throwable e) {
                return -1;
            }
        }

        public boolean conformsTo(String className) {
            if (this.isNull()) {
                return false;
            }
            Arena arena = Arena.ofConfined();
            try {
                MemorySegment classNameStr = arena.allocateFrom(className);
                boolean bl = IOKitFunctions.IOObjectConformsTo(this.segment, classNameStr);
                if (arena != null) {
                    arena.close();
                }
                return bl;
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
                    return false;
                }
            }
        }
    }
}


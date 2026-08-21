/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.DiskArbitration;
import com.sun.jna.platform.mac.IOKit;
import com.sun.jna.platform.mac.IOKitUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.mac.disk.Fsstat;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.common.AbstractHWDiskStore;
import oshi.util.platform.mac.CFUtil;

@ThreadSafe
public final class MacHWDiskStore
extends AbstractHWDiskStore {
    private static final CoreFoundation CF = CoreFoundation.INSTANCE;
    private static final DiskArbitration DA = DiskArbitration.INSTANCE;
    private static final Logger LOG = LoggerFactory.getLogger(MacHWDiskStore.class);
    private long reads = 0L;
    private long readBytes = 0L;
    private long writes = 0L;
    private long writeBytes = 0L;
    private long currentQueueLength = 0L;
    private long transferTime = 0L;
    private long timeStamp = 0L;
    private List<HWPartition> partitionList;

    private MacHWDiskStore(String name, String model, String serial, long size, DiskArbitration.DASessionRef session, Map<String, String> mountPointMap, Map<CFKey, CoreFoundation.CFStringRef> cfKeyMap) {
        super(name, model, serial, size);
        this.updateDiskStats(session, mountPointMap, cfKeyMap);
    }

    @Override
    public long getReads() {
        return this.reads;
    }

    @Override
    public long getReadBytes() {
        return this.readBytes;
    }

    @Override
    public long getWrites() {
        return this.writes;
    }

    @Override
    public long getWriteBytes() {
        return this.writeBytes;
    }

    @Override
    public long getCurrentQueueLength() {
        return this.currentQueueLength;
    }

    @Override
    public long getTransferTime() {
        return this.transferTime;
    }

    @Override
    public long getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public List<HWPartition> getPartitions() {
        return this.partitionList;
    }

    @Override
    public boolean updateAttributes() {
        DiskArbitration.DASessionRef session = DA.DASessionCreate(CF.CFAllocatorGetDefault());
        if (session == null) {
            LOG.error("Unable to open session to DiskArbitration framework.");
            return false;
        }
        Map<CFKey, CoreFoundation.CFStringRef> cfKeyMap = MacHWDiskStore.mapCFKeys();
        boolean diskFound = this.updateDiskStats(session, Fsstat.queryPartitionToMountMap(), cfKeyMap);
        session.release();
        for (CoreFoundation.CFTypeRef cFTypeRef : cfKeyMap.values()) {
            cFTypeRef.release();
        }
        return diskFound;
    }

    private boolean updateDiskStats(DiskArbitration.DASessionRef session, Map<String, String> mountPointMap, Map<CFKey, CoreFoundation.CFStringRef> cfKeyMap) {
        IOKit.IOIterator driveListIter;
        String bsdName = this.getName();
        CoreFoundation.CFMutableDictionaryRef matchingDict = IOKitUtil.getBSDNameMatchingDict(bsdName);
        if (matchingDict != null && (driveListIter = IOKitUtil.getMatchingServices(matchingDict)) != null) {
            IOKit.IORegistryEntry drive = driveListIter.next();
            if (drive != null) {
                if (drive.conformsTo("IOMedia")) {
                    IOKit.IORegistryEntry parent = drive.getParentEntry("IOService");
                    if (parent != null && (parent.conformsTo("IOBlockStorageDriver") || parent.conformsTo("AppleAPFSContainerScheme"))) {
                        CoreFoundation.CFMutableDictionaryRef properties = parent.createCFProperties();
                        Pointer result = properties.getValue(cfKeyMap.get((Object)CFKey.STATISTICS));
                        CoreFoundation.CFDictionaryRef statistics = new CoreFoundation.CFDictionaryRef(result);
                        this.timeStamp = System.currentTimeMillis();
                        result = statistics.getValue(cfKeyMap.get((Object)CFKey.READ_OPS));
                        CoreFoundation.CFNumberRef stat = new CoreFoundation.CFNumberRef(result);
                        this.reads = stat.longValue();
                        result = statistics.getValue(cfKeyMap.get((Object)CFKey.READ_BYTES));
                        stat.setPointer(result);
                        this.readBytes = stat.longValue();
                        result = statistics.getValue(cfKeyMap.get((Object)CFKey.WRITE_OPS));
                        stat.setPointer(result);
                        this.writes = stat.longValue();
                        result = statistics.getValue(cfKeyMap.get((Object)CFKey.WRITE_BYTES));
                        stat.setPointer(result);
                        this.writeBytes = stat.longValue();
                        Pointer readTimeResult = statistics.getValue(cfKeyMap.get((Object)CFKey.READ_TIME));
                        Pointer writeTimeResult = statistics.getValue(cfKeyMap.get((Object)CFKey.WRITE_TIME));
                        if (readTimeResult != null && writeTimeResult != null) {
                            stat.setPointer(readTimeResult);
                            long xferTime = stat.longValue();
                            stat.setPointer(writeTimeResult);
                            this.transferTime = (xferTime += stat.longValue()) / 1000000L;
                        }
                        properties.release();
                    } else {
                        LOG.debug("Unable to find block storage driver properties for {}", (Object)bsdName);
                    }
                    ArrayList<HWPartition> partitions = new ArrayList<HWPartition>();
                    CoreFoundation.CFMutableDictionaryRef properties = drive.createCFProperties();
                    Pointer result = properties.getValue(cfKeyMap.get((Object)CFKey.BSD_UNIT));
                    CoreFoundation.CFNumberRef bsdUnit = new CoreFoundation.CFNumberRef(result);
                    result = properties.getValue(cfKeyMap.get((Object)CFKey.LEAF));
                    CoreFoundation.CFBooleanRef cfFalse = new CoreFoundation.CFBooleanRef(result);
                    CoreFoundation.CFMutableDictionaryRef propertyDict = CF.CFDictionaryCreateMutable(CF.CFAllocatorGetDefault(), new CoreFoundation.CFIndex(0L), null, null);
                    propertyDict.setValue(cfKeyMap.get((Object)CFKey.BSD_UNIT), bsdUnit);
                    propertyDict.setValue(cfKeyMap.get((Object)CFKey.WHOLE), cfFalse);
                    matchingDict = CF.CFDictionaryCreateMutable(CF.CFAllocatorGetDefault(), new CoreFoundation.CFIndex(0L), null, null);
                    matchingDict.setValue(cfKeyMap.get((Object)CFKey.IO_PROPERTY_MATCH), propertyDict);
                    IOKit.IOIterator serviceIterator = IOKitUtil.getMatchingServices(matchingDict);
                    properties.release();
                    propertyDict.release();
                    if (serviceIterator != null) {
                        IOKit.IORegistryEntry sdService = IOKit.INSTANCE.IOIteratorNext(serviceIterator);
                        while (sdService != null) {
                            String partBsdName;
                            String name = partBsdName = sdService.getStringProperty("BSD Name");
                            String type = "";
                            DiskArbitration.DADiskRef disk = DA.DADiskCreateFromBSDName(CF.CFAllocatorGetDefault(), session, partBsdName);
                            if (disk != null) {
                                CoreFoundation.CFDictionaryRef diskInfo = DA.DADiskCopyDescription(disk);
                                if (diskInfo != null) {
                                    result = diskInfo.getValue(cfKeyMap.get((Object)CFKey.DA_MEDIA_NAME));
                                    type = CFUtil.cfPointerToString(result);
                                    result = diskInfo.getValue(cfKeyMap.get((Object)CFKey.DA_VOLUME_NAME));
                                    name = result == null ? type : CFUtil.cfPointerToString(result);
                                    diskInfo.release();
                                }
                                disk.release();
                            }
                            String mountPoint = mountPointMap.getOrDefault(partBsdName, "");
                            Long size = sdService.getLongProperty("Size");
                            Integer bsdMajor = sdService.getIntegerProperty("BSD Major");
                            Integer bsdMinor = sdService.getIntegerProperty("BSD Minor");
                            String uuid = sdService.getStringProperty("UUID");
                            partitions.add(new HWPartition(partBsdName, name, type, uuid == null ? "unknown" : uuid, size == null ? 0L : size, bsdMajor == null ? 0 : bsdMajor, bsdMinor == null ? 0 : bsdMinor, mountPoint));
                            sdService.release();
                            sdService = IOKit.INSTANCE.IOIteratorNext(serviceIterator);
                        }
                        serviceIterator.release();
                    }
                    this.partitionList = Collections.unmodifiableList(partitions.stream().sorted(Comparator.comparing(HWPartition::getName)).collect(Collectors.toList()));
                    if (parent != null) {
                        parent.release();
                    }
                } else {
                    LOG.error("Unable to find IOMedia device or parent for {}", (Object)bsdName);
                }
                drive.release();
            }
            driveListIter.release();
            return true;
        }
        return false;
    }

    public static List<HWDiskStore> getDisks() {
        Map<String, String> mountPointMap = Fsstat.queryPartitionToMountMap();
        Map<CFKey, CoreFoundation.CFStringRef> cfKeyMap = MacHWDiskStore.mapCFKeys();
        ArrayList<HWDiskStore> diskList = new ArrayList<HWDiskStore>();
        DiskArbitration.DASessionRef session = DA.DASessionCreate(CF.CFAllocatorGetDefault());
        if (session == null) {
            LOG.error("Unable to open session to DiskArbitration framework.");
            return Collections.emptyList();
        }
        ArrayList<String> bsdNames = new ArrayList<String>();
        IOKit.IOIterator iter = IOKitUtil.getMatchingServices("IOMedia");
        if (iter != null) {
            IOKit.IORegistryEntry media = iter.next();
            while (media != null) {
                Boolean bl = media.getBooleanProperty("Whole");
                if (bl != null && bl.booleanValue()) {
                    DiskArbitration.DADiskRef disk = DA.DADiskCreateFromIOMedia(CF.CFAllocatorGetDefault(), session, media);
                    bsdNames.add(DA.DADiskGetBSDName(disk));
                    disk.release();
                }
                media.release();
                media = iter.next();
            }
            iter.release();
        }
        for (String string : bsdNames) {
            String model = "";
            String serial = "";
            long size = 0L;
            String path = "/dev/" + string;
            DiskArbitration.DADiskRef disk = DA.DADiskCreateFromBSDName(CF.CFAllocatorGetDefault(), session, path);
            if (disk == null) continue;
            CoreFoundation.CFDictionaryRef diskInfo = DA.DADiskCopyDescription(disk);
            if (diskInfo != null) {
                Pointer result = diskInfo.getValue(cfKeyMap.get((Object)CFKey.DA_DEVICE_MODEL));
                model = CFUtil.cfPointerToString(result);
                result = diskInfo.getValue(cfKeyMap.get((Object)CFKey.DA_MEDIA_SIZE));
                CoreFoundation.CFNumberRef sizePtr = new CoreFoundation.CFNumberRef(result);
                size = sizePtr.longValue();
                diskInfo.release();
                if (!"Disk Image".equals(model)) {
                    CoreFoundation.CFStringRef modelNameRef = CoreFoundation.CFStringRef.createCFString(model);
                    CoreFoundation.CFMutableDictionaryRef propertyDict = CF.CFDictionaryCreateMutable(CF.CFAllocatorGetDefault(), new CoreFoundation.CFIndex(0L), null, null);
                    propertyDict.setValue(cfKeyMap.get((Object)CFKey.MODEL), modelNameRef);
                    CoreFoundation.CFMutableDictionaryRef matchingDict = CF.CFDictionaryCreateMutable(CF.CFAllocatorGetDefault(), new CoreFoundation.CFIndex(0L), null, null);
                    matchingDict.setValue(cfKeyMap.get((Object)CFKey.IO_PROPERTY_MATCH), propertyDict);
                    IOKit.IOIterator serviceIterator = IOKitUtil.getMatchingServices(matchingDict);
                    modelNameRef.release();
                    propertyDict.release();
                    if (serviceIterator != null) {
                        IOKit.IORegistryEntry sdService = serviceIterator.next();
                        while (sdService != null) {
                            serial = sdService.getStringProperty("Serial Number");
                            sdService.release();
                            if (serial != null) break;
                            sdService.release();
                            sdService = serviceIterator.next();
                        }
                        serviceIterator.release();
                    }
                    if (serial == null) {
                        serial = "";
                    }
                }
            }
            disk.release();
            if (size <= 0L) continue;
            MacHWDiskStore diskStore = new MacHWDiskStore(string, model.trim(), serial.trim(), size, session, mountPointMap, cfKeyMap);
            diskList.add(diskStore);
        }
        session.release();
        for (CoreFoundation.CFTypeRef cFTypeRef : cfKeyMap.values()) {
            cFTypeRef.release();
        }
        return diskList;
    }

    private static Map<CFKey, CoreFoundation.CFStringRef> mapCFKeys() {
        EnumMap<CFKey, CoreFoundation.CFStringRef> keyMap = new EnumMap<CFKey, CoreFoundation.CFStringRef>(CFKey.class);
        for (CFKey cfKey : CFKey.values()) {
            keyMap.put(cfKey, CoreFoundation.CFStringRef.createCFString(cfKey.getKey()));
        }
        return keyMap;
    }

    private static enum CFKey {
        IO_PROPERTY_MATCH("IOPropertyMatch"),
        STATISTICS("Statistics"),
        READ_OPS("Operations (Read)"),
        READ_BYTES("Bytes (Read)"),
        READ_TIME("Total Time (Read)"),
        WRITE_OPS("Operations (Write)"),
        WRITE_BYTES("Bytes (Write)"),
        WRITE_TIME("Total Time (Write)"),
        BSD_UNIT("BSD Unit"),
        LEAF("Leaf"),
        WHOLE("Whole"),
        DA_MEDIA_NAME("DAMediaName"),
        DA_VOLUME_NAME("DAVolumeName"),
        DA_MEDIA_SIZE("DAMediaSize"),
        DA_DEVICE_MODEL("DADeviceModel"),
        MODEL("Model");

        private final String key;

        private CFKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }
    }
}


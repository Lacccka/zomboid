/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.mac;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.ffm.mac.CoreFoundation;
import oshi.ffm.mac.CoreFoundationFunctions;
import oshi.ffm.mac.DiskArbitration;
import oshi.ffm.mac.DiskArbitrationFunctions;
import oshi.ffm.mac.IOKit;
import oshi.ffm.mac.MacSystem;
import oshi.ffm.mac.MacSystemFunctions;
import oshi.software.os.OSFileStore;
import oshi.software.os.mac.MacFileSystem;
import oshi.software.os.mac.MacOSFileStore;
import oshi.util.FileSystemUtil;
import oshi.util.platform.mac.CFUtilFFM;
import oshi.util.platform.mac.IOKitUtilFFM;
import oshi.util.platform.mac.SysctlUtilFFM;

@ThreadSafe
public class MacFileSystemFFM
extends MacFileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(MacFileSystemFFM.class);

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        return MacFileSystemFFM.getFileStoreMatching(null, localOnly);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static List<OSFileStore> getFileStoreMatching(String nameToMatch, boolean localOnly) {
        ArrayList<OSFileStore> fsList = new ArrayList<OSFileStore>();
        try (Arena arena = Arena.ofConfined();){
            int numfs = MacSystemFunctions.getfsstat64(MemorySegment.NULL, 0, 0);
            if (numfs <= 0) {
                ArrayList<OSFileStore> arrayList = fsList;
                return arrayList;
            }
            long statfsSize = MacSystem.STATFS.byteSize();
            MemorySegment statfsBuffer = arena.allocate(statfsSize * (long)numfs);
            numfs = MacSystemFunctions.getfsstat64(statfsBuffer, (int)statfsBuffer.byteSize(), 16);
            if (numfs <= 0) {
                ArrayList<OSFileStore> arrayList = fsList;
                return arrayList;
            }
            CoreFoundation.CFAllocatorRef allocator = new CoreFoundation.CFAllocatorRef(CoreFoundationFunctions.CFAllocatorGetDefault());
            DiskArbitration.DASessionRef session = new DiskArbitration.DASessionRef(DiskArbitrationFunctions.DASessionCreate(allocator.segment()));
            if (session.segment() == null) {
                LOG.error("Unable to open session to DiskArbitration framework.");
                ArrayList<OSFileStore> arrayList = fsList;
                return arrayList;
            }
            try {
                CoreFoundation.CFStringRef daVolumeNameKey = CoreFoundation.CFStringRef.createCFString("DAVolumeName");
                try {
                    int f = 0;
                    while (f < numfs) {
                        boolean isLocal;
                        MemorySegment statfs = statfsBuffer.asSlice((long)f * statfsSize, statfsSize);
                        String volume = statfs.getString(MacSystem.STATFS.byteOffset(MacSystem.F_MNTFROMNAME));
                        String path = statfs.getString(MacSystem.STATFS.byteOffset(MacSystem.F_MNTONNAME));
                        String type = statfs.getString(MacSystem.STATFS.byteOffset(MacSystem.F_FSTYPENAME));
                        int flags = statfs.get(ValueLayout.JAVA_INT, MacSystem.STATFS.byteOffset(MacSystem.F_FLAGS));
                        long ffree = statfs.get(ValueLayout.JAVA_LONG, MacSystem.STATFS.byteOffset(MacSystem.F_FFREE));
                        long files = statfs.get(ValueLayout.JAVA_LONG, MacSystem.STATFS.byteOffset(MacSystem.F_FILES));
                        boolean bl = isLocal = (flags & 0x1000) != 0;
                        if (!(localOnly && !isLocal || !path.equals("/") && (PSEUDO_FS_TYPES.contains(type) || FileSystemUtil.isFileStoreExcluded(path, volume, FS_PATH_INCLUDES, FS_PATH_EXCLUDES, FS_VOLUME_INCLUDES, FS_VOLUME_EXCLUDES)))) {
                            String description = "Volume";
                            if (LOCAL_DISK.matcher(volume).matches()) {
                                description = "Local Disk";
                            } else if (volume.startsWith("localhost:") || volume.startsWith("//") || volume.startsWith("smb://") || NETWORK_FS_TYPES.contains(type)) {
                                description = "Network Drive";
                            }
                            File file = new File(path);
                            String name = file.getName();
                            if (name.isEmpty()) {
                                name = file.getPath();
                            }
                            StringBuilder options = new StringBuilder((1 & flags) == 0 ? "rw" : "ro");
                            String moreOptions = OPTIONS_MAP.entrySet().stream().filter(e -> ((Integer)e.getKey() & flags) > 0).map(Map.Entry::getValue).collect(Collectors.joining(","));
                            if (!moreOptions.isEmpty()) {
                                options.append(',').append(moreOptions);
                            }
                            String uuid = "";
                            String bsdName = volume.replace("/dev/disk", "disk");
                            if (bsdName.startsWith("disk")) {
                                IOKit.IOIterator fsIter;
                                DiskArbitration.DADiskRef disk = null;
                                CoreFoundation.CFTypeRef diskInfo = null;
                                try {
                                    MemorySegment result;
                                    disk = DiskArbitration.DADiskRef.createFromBSDName(allocator, session, volume);
                                    if (!(disk.isNull() || (diskInfo = disk.copyDescription()).isNull() || (result = ((CoreFoundation.CFDictionaryRef)diskInfo).getValue(daVolumeNameKey)).equals(MemorySegment.NULL))) {
                                        name = CFUtilFFM.cfPointerToString(result);
                                    }
                                }
                                finally {
                                    if (diskInfo != null) {
                                        diskInfo.release();
                                    }
                                    if (disk != null) {
                                        disk.release();
                                    }
                                }
                                MemorySegment matchingDict = IOKitUtilFFM.getBSDNameMatchingDict(bsdName);
                                if (matchingDict != null && (fsIter = IOKitUtilFFM.getMatchingServices(matchingDict)) != null) {
                                    IOKit.IORegistryEntry fsEntry = fsIter.next();
                                    if (fsEntry != null && fsEntry.conformsTo("IOMedia")) {
                                        uuid = fsEntry.getStringProperty("UUID");
                                        if (uuid != null) {
                                            uuid = uuid.toLowerCase(Locale.ROOT);
                                        }
                                        fsEntry.release();
                                    }
                                    fsIter.release();
                                }
                            }
                            if (nameToMatch == null || nameToMatch.equals(name)) {
                                fsList.add(new MacOSFileStore(name, volume, name, path, options.toString(), uuid == null ? "" : uuid, isLocal, "", description, type, file.getFreeSpace(), file.getUsableSpace(), file.getTotalSpace(), ffree, files));
                            }
                        }
                        ++f;
                    }
                    return fsList;
                }
                finally {
                    daVolumeNameKey.release();
                }
            }
            finally {
                session.release();
            }
        }
        catch (Throwable e2) {
            LOG.warn("Failed to query file systems: {}", (Object)e2.getMessage(), (Object)e2);
            return fsList;
        }
    }

    @Override
    public long getOpenFileDescriptors() {
        return SysctlUtilFFM.sysctl("kern.num_files", 0);
    }

    @Override
    public long getMaxFileDescriptors() {
        return SysctlUtilFFM.sysctl("kern.maxfiles", 0);
    }

    @Override
    public long getMaxFileDescriptorsPerProcess() {
        return SysctlUtilFFM.sysctl("kern.maxfilesperproc", 0);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.freebsd;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.common.AbstractFileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.unix.freebsd.FreeBsdOSFileStore;
import oshi.util.ExecutingCommand;
import oshi.util.FileSystemUtil;
import oshi.util.ParseUtil;
import oshi.util.platform.unix.freebsd.BsdSysctlUtil;

@ThreadSafe
public final class FreeBsdFileSystem
extends AbstractFileSystem {
    public static final String OSHI_FREEBSD_FS_PATH_EXCLUDES = "oshi.os.freebsd.filesystem.path.excludes";
    public static final String OSHI_FREEBSD_FS_PATH_INCLUDES = "oshi.os.freebsd.filesystem.path.includes";
    public static final String OSHI_FREEBSD_FS_VOLUME_EXCLUDES = "oshi.os.freebsd.filesystem.volume.excludes";
    public static final String OSHI_FREEBSD_FS_VOLUME_INCLUDES = "oshi.os.freebsd.filesystem.volume.includes";
    private static final List<PathMatcher> FS_PATH_EXCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.freebsd.filesystem.path.excludes");
    private static final List<PathMatcher> FS_PATH_INCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.freebsd.filesystem.path.includes");
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.freebsd.filesystem.volume.excludes");
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.freebsd.filesystem.volume.includes");

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        String[] split;
        HashMap<String, String> uuidMap = new HashMap<String, String>();
        String device = "";
        for (String line : ExecutingCommand.runNative("geom part list")) {
            if (line.contains("Name: ")) {
                device = line.substring(line.lastIndexOf(32) + 1);
            }
            if (device.isEmpty() || !(line = line.trim()).startsWith("rawuuid:")) continue;
            uuidMap.put(device, line.substring(line.lastIndexOf(32) + 1));
            device = "";
        }
        ArrayList<OSFileStore> fsList = new ArrayList<OSFileStore>();
        HashMap<String, Long> inodeFreeMap = new HashMap<String, Long>();
        HashMap<String, Long> inodeTotalMap = new HashMap<String, Long>();
        for (String line : ExecutingCommand.runNative("df -i")) {
            if (!line.startsWith("/") || (split = ParseUtil.whitespaces.split(line)).length <= 7) continue;
            inodeFreeMap.put(split[0], ParseUtil.parseLongOrDefault(split[6], 0L));
            inodeTotalMap.put(split[0], (Long)inodeFreeMap.get(split[0]) + ParseUtil.parseLongOrDefault(split[5], 0L));
        }
        for (String fs : ExecutingCommand.runNative("mount -p")) {
            boolean isLocal;
            split = ParseUtil.whitespaces.split(fs);
            if (split.length < 5) continue;
            String volume = split[0];
            String path = split[1];
            String type = split[2];
            String options = split[3];
            boolean bl = isLocal = !NETWORK_FS_TYPES.contains(type);
            if (localOnly && !isLocal || !path.equals("/") && (PSEUDO_FS_TYPES.contains(type) || FileSystemUtil.isFileStoreExcluded(path, volume, FS_PATH_INCLUDES, FS_PATH_EXCLUDES, FS_VOLUME_INCLUDES, FS_VOLUME_EXCLUDES))) continue;
            String name = path.substring(path.lastIndexOf(47) + 1);
            if (name.isEmpty()) {
                name = volume.substring(volume.lastIndexOf(47) + 1);
            }
            File f = new File(path);
            long totalSpace = f.getTotalSpace();
            long usableSpace = f.getUsableSpace();
            long freeSpace = f.getFreeSpace();
            String description = volume.startsWith("/dev") || path.equals("/") ? "Local Disk" : (volume.equals("tmpfs") ? "Ram Disk" : (NETWORK_FS_TYPES.contains(type) ? "Network Disk" : "Mount Point"));
            String uuid = uuidMap.getOrDefault(name, "");
            fsList.add(new FreeBsdOSFileStore(name, volume, name, path, options, uuid, isLocal, "", description, type, freeSpace, usableSpace, totalSpace, inodeFreeMap.containsKey(path) ? (Long)inodeFreeMap.get(path) : 0L, inodeTotalMap.containsKey(path) ? (Long)inodeTotalMap.get(path) : 0L));
        }
        return fsList;
    }

    @Override
    public long getOpenFileDescriptors() {
        return BsdSysctlUtil.sysctl("kern.openfiles", 0);
    }

    @Override
    public long getMaxFileDescriptors() {
        return BsdSysctlUtil.sysctl("kern.maxfiles", 0);
    }

    @Override
    public long getMaxFileDescriptorsPerProcess() {
        return this.getMaxFileDescriptors();
    }
}


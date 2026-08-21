/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.aix;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.common.AbstractFileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.unix.aix.AixOSFileStore;
import oshi.util.ExecutingCommand;
import oshi.util.FileSystemUtil;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;

@ThreadSafe
public class AixFileSystem
extends AbstractFileSystem {
    public static final String OSHI_AIX_FS_PATH_EXCLUDES = "oshi.os.aix.filesystem.path.excludes";
    public static final String OSHI_AIX_FS_PATH_INCLUDES = "oshi.os.aix.filesystem.path.includes";
    public static final String OSHI_AIX_FS_VOLUME_EXCLUDES = "oshi.os.aix.filesystem.volume.excludes";
    public static final String OSHI_AIX_FS_VOLUME_INCLUDES = "oshi.os.aix.filesystem.volume.includes";
    private static final List<PathMatcher> FS_PATH_EXCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.aix.filesystem.path.excludes");
    private static final List<PathMatcher> FS_PATH_INCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.aix.filesystem.path.includes");
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.aix.filesystem.volume.excludes");
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.aix.filesystem.volume.includes");

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        return AixFileSystem.getFileStoreMatching(null, localOnly);
    }

    static List<OSFileStore> getFileStoreMatching(String nameToMatch, boolean localOnly) {
        String[] split;
        ArrayList<OSFileStore> fsList = new ArrayList<OSFileStore>();
        HashMap<String, Long> inodeFreeMap = new HashMap<String, Long>();
        HashMap<String, Long> inodeTotalMap = new HashMap<String, Long>();
        String command = "df -i" + (localOnly ? " -l" : "");
        for (String line : ExecutingCommand.runNative(command)) {
            if (!line.startsWith("/") || (split = ParseUtil.whitespaces.split(line)).length <= 5) continue;
            inodeTotalMap.put(split[0], ParseUtil.parseLongOrDefault(split[1], 0L));
            inodeFreeMap.put(split[0], ParseUtil.parseLongOrDefault(split[3], 0L));
        }
        for (String fs : ExecutingCommand.runNative("mount")) {
            File f;
            boolean isLocal;
            split = ParseUtil.whitespaces.split("x" + fs);
            if (split.length <= 7) continue;
            String volume = split[1];
            String path = split[2];
            String type = split[3];
            String options = split[4];
            boolean bl = isLocal = !NETWORK_FS_TYPES.contains(type);
            if (localOnly && !isLocal || !path.equals("/") && (PSEUDO_FS_TYPES.contains(type) || FileSystemUtil.isFileStoreExcluded(path, volume, FS_PATH_INCLUDES, FS_PATH_EXCLUDES, FS_VOLUME_INCLUDES, FS_VOLUME_EXCLUDES))) continue;
            String name = path.substring(path.lastIndexOf(47) + 1);
            if (name.isEmpty()) {
                name = volume.substring(volume.lastIndexOf(47) + 1);
            }
            if (nameToMatch != null && !nameToMatch.equals(name) || !(f = new File(path)).exists() || f.getTotalSpace() < 0L) continue;
            long totalSpace = f.getTotalSpace();
            long usableSpace = f.getUsableSpace();
            long freeSpace = f.getFreeSpace();
            String description = volume.startsWith("/dev") || path.equals("/") ? "Local Disk" : (volume.equals("tmpfs") ? "Ram Disk" : (NETWORK_FS_TYPES.contains(type) ? "Network Disk" : "Mount Point"));
            fsList.add(new AixOSFileStore(name, volume, name, path, options, "", isLocal, "", description, type, freeSpace, usableSpace, totalSpace, inodeFreeMap.getOrDefault(volume, 0L), inodeTotalMap.getOrDefault(volume, 0L)));
        }
        return fsList;
    }

    @Override
    public long getOpenFileDescriptors() {
        boolean header = false;
        long openfiles = 0L;
        for (String f : ExecutingCommand.runNative("lsof -nl")) {
            if (!header) {
                header = f.startsWith("COMMAND");
                continue;
            }
            ++openfiles;
        }
        return openfiles;
    }

    @Override
    public long getMaxFileDescriptors() {
        return ParseUtil.parseLongOrDefault(ExecutingCommand.getFirstAnswer("ulimit -n"), 0L);
    }

    @Override
    public long getMaxFileDescriptorsPerProcess() {
        List<String> lines = FileUtil.readFile("/etc/security/limits");
        for (String line : lines) {
            if (!line.trim().startsWith("nofiles")) continue;
            return ParseUtil.parseLastLong(line, Long.MAX_VALUE);
        }
        return Long.MAX_VALUE;
    }
}


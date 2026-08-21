/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.unix.solaris;

import com.sun.jna.platform.unix.solaris.LibKstat;
import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.common.AbstractFileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.unix.solaris.SolarisOSFileStore;
import oshi.software.os.unix.solaris.SolarisOperatingSystem;
import oshi.util.ExecutingCommand;
import oshi.util.FileSystemUtil;
import oshi.util.FileUtil;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.platform.unix.solaris.KstatUtil;
import oshi.util.tuples.Pair;

@ThreadSafe
public class SolarisFileSystem
extends AbstractFileSystem {
    private static final Supplier<Pair<Long, Long>> FILE_DESC = Memoizer.memoize(SolarisFileSystem::queryFileDescriptors, Memoizer.defaultExpiration());
    public static final String OSHI_SOLARIS_FS_PATH_EXCLUDES = "oshi.os.solaris.filesystem.path.excludes";
    public static final String OSHI_SOLARIS_FS_PATH_INCLUDES = "oshi.os.solaris.filesystem.path.includes";
    public static final String OSHI_SOLARIS_FS_VOLUME_EXCLUDES = "oshi.os.solaris.filesystem.volume.excludes";
    public static final String OSHI_SOLARIS_FS_VOLUME_INCLUDES = "oshi.os.solaris.filesystem.volume.includes";
    private static final List<PathMatcher> FS_PATH_EXCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.solaris.filesystem.path.excludes");
    private static final List<PathMatcher> FS_PATH_INCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.solaris.filesystem.path.includes");
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.solaris.filesystem.volume.excludes");
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.solaris.filesystem.volume.includes");

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        return SolarisFileSystem.getFileStoreMatching(null, localOnly);
    }

    static List<OSFileStore> getFileStoreMatching(String nameToMatch, boolean localOnly) {
        ArrayList<OSFileStore> fsList = new ArrayList<OSFileStore>();
        HashMap<String, Long> inodeFreeMap = new HashMap<String, Long>();
        HashMap<String, Long> inodeTotalMap = new HashMap<String, Long>();
        String key = null;
        String total = null;
        String free = null;
        String command = "df -g" + (localOnly ? " -l" : "");
        for (String line : ExecutingCommand.runNative(command)) {
            if (line.startsWith("/")) {
                key = ParseUtil.whitespaces.split(line)[0];
                total = null;
                continue;
            }
            if (line.contains("available") && line.contains("total files")) {
                total = ParseUtil.getTextBetweenStrings(line, "available", "total files").trim();
                continue;
            }
            if (!line.contains("free files")) continue;
            free = ParseUtil.getTextBetweenStrings(line, "", "free files").trim();
            if (key == null || total == null) continue;
            inodeFreeMap.put(key, ParseUtil.parseLongOrDefault(free, 0L));
            inodeTotalMap.put(key, ParseUtil.parseLongOrDefault(total, 0L));
            key = null;
        }
        for (String fs : ExecutingCommand.runNative("cat /etc/mnttab")) {
            boolean isLocal;
            String[] split = ParseUtil.whitespaces.split(fs);
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
            if (nameToMatch != null && !nameToMatch.equals(name)) continue;
            File f = new File(path);
            long totalSpace = f.getTotalSpace();
            long usableSpace = f.getUsableSpace();
            long freeSpace = f.getFreeSpace();
            String description = volume.startsWith("/dev") || path.equals("/") ? "Local Disk" : (volume.equals("tmpfs") ? "Ram Disk" : (NETWORK_FS_TYPES.contains(type) ? "Network Disk" : "Mount Point"));
            fsList.add(new SolarisOSFileStore(name, volume, name, path, options, "", isLocal, "", description, type, freeSpace, usableSpace, totalSpace, inodeFreeMap.getOrDefault(path, 0L), inodeTotalMap.getOrDefault(path, 0L)));
        }
        return fsList;
    }

    @Override
    public long getOpenFileDescriptors() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            return FILE_DESC.get().getA();
        }
        try (KstatUtil.KstatChain kc = KstatUtil.openChain();){
            LibKstat.Kstat ksp = kc.lookup(null, -1, "file_cache");
            if (ksp != null && kc.read(ksp)) {
                long l = KstatUtil.dataLookupLong(ksp, "buf_inuse");
                return l;
            }
        }
        return 0L;
    }

    @Override
    public long getMaxFileDescriptors() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            return FILE_DESC.get().getB();
        }
        try (KstatUtil.KstatChain kc = KstatUtil.openChain();){
            LibKstat.Kstat ksp = kc.lookup(null, -1, "file_cache");
            if (ksp != null && kc.read(ksp)) {
                long l = KstatUtil.dataLookupLong(ksp, "buf_max");
                return l;
            }
        }
        return 0L;
    }

    @Override
    public long getMaxFileDescriptorsPerProcess() {
        List<String> lines = FileUtil.readFile("/etc/system");
        for (String line : lines) {
            if (!line.startsWith("set rlim_fd_max")) continue;
            return ParseUtil.parseLastLong(line, 65536L);
        }
        return 65536L;
    }

    private static Pair<Long, Long> queryFileDescriptors() {
        Object[] results = KstatUtil.queryKstat2("kstat:/kmem_cache/kmem_default/file_cache", "buf_inuse", "buf_max");
        long inuse = results[0] == null ? 0L : (Long)results[0];
        long max = results[1] == null ? 0L : (Long)results[1];
        return new Pair<Long, Long>(inuse, max);
    }
}


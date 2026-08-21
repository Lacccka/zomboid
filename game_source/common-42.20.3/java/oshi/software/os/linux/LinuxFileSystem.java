/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.linux;

import com.sun.jna.Native;
import com.sun.jna.platform.linux.LibC;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.common.AbstractFileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.linux.LinuxOSFileStore;
import oshi.util.ExecutingCommand;
import oshi.util.FileSystemUtil;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.platform.linux.DevPath;
import oshi.util.platform.linux.ProcPath;

@ThreadSafe
public class LinuxFileSystem
extends AbstractFileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxFileSystem.class);
    public static final String OSHI_LINUX_FS_PATH_EXCLUDES = "oshi.os.linux.filesystem.path.excludes";
    public static final String OSHI_LINUX_FS_PATH_INCLUDES = "oshi.os.linux.filesystem.path.includes";
    public static final String OSHI_LINUX_FS_VOLUME_EXCLUDES = "oshi.os.linux.filesystem.volume.excludes";
    public static final String OSHI_LINUX_FS_VOLUME_INCLUDES = "oshi.os.linux.filesystem.volume.includes";
    private static final List<PathMatcher> FS_PATH_EXCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.linux.filesystem.path.excludes");
    private static final List<PathMatcher> FS_PATH_INCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.linux.filesystem.path.includes");
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.linux.filesystem.volume.excludes");
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = FileSystemUtil.loadAndParseFileSystemConfig("oshi.os.linux.filesystem.volume.includes");
    private static final String UNICODE_SPACE = "\\040";

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        HashMap<String, String> volumeDeviceMap = new HashMap<String, String>();
        File devMapper = new File(DevPath.MAPPER);
        File[] volumes = devMapper.listFiles();
        if (volumes != null) {
            for (File volume : volumes) {
                try {
                    volumeDeviceMap.put(volume.getCanonicalPath(), volume.getAbsolutePath());
                }
                catch (IOException e) {
                    LOG.debug("Couldn't get canonical path for {}. {}", (Object)volume.getName(), (Object)e.getMessage());
                }
            }
        }
        HashMap<String, String> uuidMap = new HashMap<String, String>();
        File uuidDir = new File(DevPath.DISK_BY_UUID);
        File[] uuids = uuidDir.listFiles();
        if (uuids != null) {
            for (File uuid : uuids) {
                try {
                    String canonicalPath = uuid.getCanonicalPath();
                    uuidMap.put(canonicalPath, uuid.getName().toLowerCase(Locale.ROOT));
                    if (!volumeDeviceMap.containsKey(canonicalPath)) continue;
                    uuidMap.put((String)volumeDeviceMap.get(canonicalPath), uuid.getName().toLowerCase(Locale.ROOT));
                }
                catch (IOException e) {
                    LOG.debug("Couldn't get canonical path for {}. {}", (Object)uuid.getName(), (Object)e.getMessage());
                }
            }
        }
        return LinuxFileSystem.getFileStoreMatching(null, uuidMap, localOnly);
    }

    static List<OSFileStore> getFileStoreMatching(String nameToMatch, Map<String, String> uuidMap, boolean localOnly) {
        ArrayList<OSFileStore> fsList = new ArrayList<OSFileStore>();
        Map<String, String> labelMap = LinuxFileSystem.queryLabelMap();
        List<String> mounts = FileUtil.readFile(ProcPath.MOUNTS);
        for (String mount : mounts) {
            String uuid;
            String type;
            boolean isLocal;
            String volume;
            String[] split = mount.split(" ");
            if (split.length < 6) continue;
            String name = volume = split[0].replace(UNICODE_SPACE, " ");
            String path = split[1].replace(UNICODE_SPACE, " ");
            if (path.equals("/")) {
                name = "/";
            }
            boolean bl = isLocal = !NETWORK_FS_TYPES.contains(type = split[2]);
            if (localOnly && !isLocal || !path.equals("/") && (PSEUDO_FS_TYPES.contains(type) || FileSystemUtil.isFileStoreExcluded(path, volume, FS_PATH_INCLUDES, FS_PATH_EXCLUDES, FS_VOLUME_INCLUDES, FS_VOLUME_EXCLUDES))) continue;
            String options = split[3];
            if (nameToMatch != null && !nameToMatch.equals(name)) continue;
            String string = uuid = uuidMap != null ? uuidMap.getOrDefault(split[0], "") : "";
            String description = volume.startsWith(DevPath.DEV) ? "Local Disk" : (volume.equals("tmpfs") ? "Ram Disk" : (NETWORK_FS_TYPES.contains(type) ? "Network Disk" : "Mount Point"));
            String logicalVolume = "";
            Path link = Paths.get(volume, new String[0]);
            if (link.toFile().exists() && Files.isSymbolicLink(link)) {
                try {
                    Path slink = Files.readSymbolicLink(link);
                    Path full = Paths.get(DevPath.MAPPER + slink.toString(), new String[0]);
                    if (full.toFile().exists()) {
                        logicalVolume = full.normalize().toString();
                    }
                }
                catch (IOException e) {
                    LOG.debug("Couldn't access symbolic path  {}. {}", (Object)link, (Object)e.getMessage());
                }
            }
            long totalInodes = 0L;
            long freeInodes = 0L;
            long totalSpace = 0L;
            long usableSpace = 0L;
            long freeSpace = 0L;
            try {
                LibC.Statvfs vfsStat = new LibC.Statvfs();
                if (0 == LibC.INSTANCE.statvfs(path, vfsStat)) {
                    totalInodes = vfsStat.f_files.longValue();
                    freeInodes = vfsStat.f_ffree.longValue();
                    totalSpace = vfsStat.f_blocks.longValue() * vfsStat.f_frsize.longValue();
                    usableSpace = vfsStat.f_bavail.longValue() * vfsStat.f_frsize.longValue();
                    freeSpace = vfsStat.f_bfree.longValue() * vfsStat.f_frsize.longValue();
                } else {
                    LOG.debug("Failed to get information to use statvfs. path: {}, Error code: {}", (Object)path, (Object)Native.getLastError());
                }
            }
            catch (NoClassDefFoundError | UnsatisfiedLinkError e) {
                LOG.error("Failed to get file counts from statvfs. {}", (Object)e.getMessage());
            }
            if (totalSpace == 0L) {
                File tmpFile = new File(path);
                totalSpace = tmpFile.getTotalSpace();
                usableSpace = tmpFile.getUsableSpace();
                freeSpace = tmpFile.getFreeSpace();
            }
            fsList.add(new LinuxOSFileStore(name, volume, labelMap.getOrDefault(path, name), path, options, uuid, isLocal, logicalVolume, description, type, freeSpace, usableSpace, totalSpace, freeInodes, totalInodes));
        }
        return fsList;
    }

    private static Map<String, String> queryLabelMap() {
        HashMap<String, String> labelMap = new HashMap<String, String>();
        for (String line : ExecutingCommand.runNative("lsblk -o mountpoint,label")) {
            String[] split = ParseUtil.whitespaces.split(line, 2);
            if (split.length != 2) continue;
            labelMap.put(split[0], split[1]);
        }
        return labelMap;
    }

    @Override
    public long getOpenFileDescriptors() {
        return LinuxFileSystem.getFileDescriptors(0);
    }

    @Override
    public long getMaxFileDescriptors() {
        return LinuxFileSystem.getFileDescriptors(2);
    }

    @Override
    public long getMaxFileDescriptorsPerProcess() {
        return LinuxFileSystem.getFileDescriptorsPerProcess();
    }

    private static long getFileDescriptors(int index) {
        String filename = ProcPath.SYS_FS_FILE_NR;
        if (index < 0 || index > 2) {
            throw new IllegalArgumentException("Index must be between 0 and 2.");
        }
        List<String> osDescriptors = FileUtil.readFile(filename);
        if (!osDescriptors.isEmpty()) {
            String[] splittedLine = osDescriptors.get(0).split("\\D+");
            return ParseUtil.parseLongOrDefault(splittedLine[index], 0L);
        }
        return 0L;
    }

    private static long getFileDescriptorsPerProcess() {
        return FileUtil.getLongFromFile(ProcPath.SYS_FS_FILE_MAX);
    }
}


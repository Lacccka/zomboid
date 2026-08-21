/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.windows;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.perfmon.ProcessInformation;
import oshi.driver.windows.wmi.Win32LogicalDisk;
import oshi.jna.ByRef;
import oshi.software.common.AbstractFileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.windows.WindowsOSFileStore;
import oshi.util.ParseUtil;
import oshi.util.platform.windows.WmiUtil;

@ThreadSafe
public class WindowsFileSystem
extends AbstractFileSystem {
    private static final int BUFSIZE = 255;
    private static final int SEM_FAILCRITICALERRORS = 1;
    private static final int FILE_CASE_SENSITIVE_SEARCH = 1;
    private static final int FILE_CASE_PRESERVED_NAMES = 2;
    private static final int FILE_FILE_COMPRESSION = 16;
    private static final int FILE_DAX_VOLUME = 0x20000000;
    private static final int FILE_NAMED_STREAMS = 262144;
    private static final int FILE_PERSISTENT_ACLS = 8;
    private static final int FILE_READ_ONLY_VOLUME = 524288;
    private static final int FILE_SEQUENTIAL_WRITE_ONCE = 0x100000;
    private static final int FILE_SUPPORTS_ENCRYPTION = 131072;
    private static final int FILE_SUPPORTS_OBJECT_IDS = 65536;
    private static final int FILE_SUPPORTS_REPARSE_POINTS = 128;
    private static final int FILE_SUPPORTS_SPARSE_FILES = 64;
    private static final int FILE_SUPPORTS_TRANSACTIONS = 0x200000;
    private static final int FILE_SUPPORTS_USN_JOURNAL = 0x2000000;
    private static final int FILE_UNICODE_ON_DISK = 4;
    private static final int FILE_VOLUME_IS_COMPRESSED = 32768;
    private static final int FILE_VOLUME_QUOTAS = 32;
    private static final Map<Integer, String> OPTIONS_MAP = new HashMap<Integer, String>();
    static final long MAX_WINDOWS_HANDLES;

    public WindowsFileSystem() {
        Kernel32.INSTANCE.SetErrorMode(1);
    }

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        ArrayList<OSFileStore> result = WindowsFileSystem.getLocalVolumes(null);
        HashMap<String, OSFileStore> volumeMap = new HashMap<String, OSFileStore>();
        for (OSFileStore volume : result) {
            volumeMap.put(volume.getMount(), volume);
        }
        for (OSFileStore wmiVolume : WindowsFileSystem.getWmiVolumes(null, localOnly)) {
            if (volumeMap.containsKey(wmiVolume.getMount())) {
                OSFileStore volume = (OSFileStore)volumeMap.get(wmiVolume.getMount());
                result.remove(volume);
                result.add(new WindowsOSFileStore(wmiVolume.getName(), volume.getVolume(), volume.getLabel().isEmpty() ? wmiVolume.getLabel() : volume.getLabel(), volume.getMount(), volume.getOptions(), volume.getUUID(), volume.isLocal(), "", volume.getDescription(), volume.getType(), volume.getFreeSpace(), volume.getUsableSpace(), volume.getTotalSpace(), 0L, 0L));
                continue;
            }
            if (localOnly) continue;
            result.add(wmiVolume);
        }
        return result;
    }

    static ArrayList<OSFileStore> getLocalVolumes(String volumeToMatch) {
        ArrayList<OSFileStore> fs = new ArrayList<OSFileStore>();
        char[] aVolume = new char[255];
        WinNT.HANDLE hVol = Kernel32.INSTANCE.FindFirstVolume(aVolume, 255);
        if (WinBase.INVALID_HANDLE_VALUE.equals(hVol)) {
            return fs;
        }
        try {
            ByRef.CloseableIntByReference pFlags = new ByRef.CloseableIntByReference();
            try {
                do {
                    char[] fstype = new char[16];
                    char[] name = new char[255];
                    char[] mount = new char[255];
                    WinNT.LARGE_INTEGER userFreeBytes = new WinNT.LARGE_INTEGER(0L);
                    WinNT.LARGE_INTEGER totalBytes = new WinNT.LARGE_INTEGER(0L);
                    WinNT.LARGE_INTEGER systemFreeBytes = new WinNT.LARGE_INTEGER(0L);
                    String volume = Native.toString(aVolume);
                    Kernel32.INSTANCE.GetVolumeInformation(volume, name, 255, null, null, pFlags, fstype, 16);
                    int flags = pFlags.getValue();
                    Kernel32.INSTANCE.GetVolumePathNamesForVolumeName(volume, mount, 255, null);
                    String strMount = Native.toString(mount);
                    if (strMount.isEmpty() || volumeToMatch != null && !volumeToMatch.equals(volume)) continue;
                    String strName = Native.toString(name);
                    String strFsType = Native.toString(fstype);
                    StringBuilder options = new StringBuilder((0x80000 & flags) == 0 ? "rw" : "ro");
                    String moreOptions = OPTIONS_MAP.entrySet().stream().filter(e -> ((Integer)e.getKey() & flags) > 0).map(Map.Entry::getValue).collect(Collectors.joining(","));
                    if (!moreOptions.isEmpty()) {
                        options.append(',').append(moreOptions);
                    }
                    Kernel32.INSTANCE.GetDiskFreeSpaceEx(volume, userFreeBytes, totalBytes, systemFreeBytes);
                    String uuid = ParseUtil.parseUuidOrDefault(volume, "");
                    fs.add(new WindowsOSFileStore(String.format(Locale.ROOT, "%s (%s)", strName, strMount), volume, strName, strMount, options.toString(), uuid, true, "", WindowsFileSystem.getDriveType(strMount), strFsType, systemFreeBytes.getValue(), userFreeBytes.getValue(), totalBytes.getValue(), 0L, 0L));
                } while (Kernel32.INSTANCE.FindNextVolume(hVol, aVolume, 255));
                ArrayList<OSFileStore> arrayList = fs;
                pFlags.close();
                return arrayList;
            }
            catch (Throwable throwable) {
                try {
                    pFlags.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
        }
        finally {
            Kernel32.INSTANCE.FindVolumeClose(hVol);
        }
    }

    static List<OSFileStore> getWmiVolumes(String nameToMatch, boolean localOnly) {
        ArrayList<OSFileStore> fs = new ArrayList<OSFileStore>();
        WbemcliUtil.WmiResult<Win32LogicalDisk.LogicalDiskProperty> drives = Win32LogicalDisk.queryLogicalDisk(nameToMatch, localOnly);
        for (int i = 0; i < drives.getResultCount(); ++i) {
            String volume;
            long free = WmiUtil.getUint64(drives, Win32LogicalDisk.LogicalDiskProperty.FREESPACE, i);
            long total = WmiUtil.getUint64(drives, Win32LogicalDisk.LogicalDiskProperty.SIZE, i);
            String description = WmiUtil.getString(drives, Win32LogicalDisk.LogicalDiskProperty.DESCRIPTION, i);
            String name = WmiUtil.getString(drives, Win32LogicalDisk.LogicalDiskProperty.NAME, i);
            String label = WmiUtil.getString(drives, Win32LogicalDisk.LogicalDiskProperty.VOLUMENAME, i);
            String options = WmiUtil.getUint16(drives, Win32LogicalDisk.LogicalDiskProperty.ACCESS, i) == 1 ? "ro" : "rw";
            int type = WmiUtil.getUint32(drives, Win32LogicalDisk.LogicalDiskProperty.DRIVETYPE, i);
            if (type != 4) {
                char[] chrVolume = new char[255];
                Kernel32.INSTANCE.GetVolumeNameForVolumeMountPoint(name + "\\", chrVolume, 255);
                volume = Native.toString(chrVolume);
            } else {
                volume = WmiUtil.getString(drives, Win32LogicalDisk.LogicalDiskProperty.PROVIDERNAME, i);
                String[] split = volume.split("\\\\");
                if (split.length > 1 && split[split.length - 1].length() > 0) {
                    description = split[split.length - 1];
                }
            }
            int driveType = Kernel32.INSTANCE.GetDriveType(name);
            boolean local = driveType == 2 || driveType == 3 || driveType == 6;
            fs.add(new WindowsOSFileStore(String.format(Locale.ROOT, "%s (%s)", description, name), volume, label, name + "\\", options, "", local, "", WindowsFileSystem.getDriveType(name), WmiUtil.getString(drives, Win32LogicalDisk.LogicalDiskProperty.FILESYSTEM, i), free, free, total, 0L, 0L));
        }
        return fs;
    }

    private static String getDriveType(String drive) {
        switch (Kernel32.INSTANCE.GetDriveType(drive)) {
            case 2: {
                return "Removable drive";
            }
            case 3: {
                return "Fixed drive";
            }
            case 4: {
                return "Network drive";
            }
            case 5: {
                return "CD-ROM";
            }
            case 6: {
                return "RAM drive";
            }
        }
        return "Unknown drive type";
    }

    @Override
    public long getOpenFileDescriptors() {
        Map<ProcessInformation.HandleCountProperty, List<Long>> valueListMap = ProcessInformation.queryHandles().getB();
        List<Long> valueList = valueListMap.get(ProcessInformation.HandleCountProperty.HANDLECOUNT);
        long descriptors = 0L;
        if (valueList != null) {
            for (Long value : valueList) {
                descriptors += value.longValue();
            }
        }
        return descriptors;
    }

    @Override
    public long getMaxFileDescriptors() {
        return MAX_WINDOWS_HANDLES;
    }

    @Override
    public long getMaxFileDescriptorsPerProcess() {
        return MAX_WINDOWS_HANDLES;
    }

    static {
        OPTIONS_MAP.put(2, "casepn");
        OPTIONS_MAP.put(1, "casess");
        OPTIONS_MAP.put(16, "fcomp");
        OPTIONS_MAP.put(0x20000000, "dax");
        OPTIONS_MAP.put(262144, "streams");
        OPTIONS_MAP.put(8, "acls");
        OPTIONS_MAP.put(0x100000, "wronce");
        OPTIONS_MAP.put(131072, "efs");
        OPTIONS_MAP.put(65536, "oids");
        OPTIONS_MAP.put(128, "reparse");
        OPTIONS_MAP.put(64, "sparse");
        OPTIONS_MAP.put(0x200000, "trans");
        OPTIONS_MAP.put(0x2000000, "journaled");
        OPTIONS_MAP.put(4, "unicode");
        OPTIONS_MAP.put(32768, "vcomp");
        OPTIONS_MAP.put(32, "quota");
        MAX_WINDOWS_HANDLES = System.getenv("ProgramFiles(x86)") == null ? 0xFF8000L : 0xFF0000L;
    }
}


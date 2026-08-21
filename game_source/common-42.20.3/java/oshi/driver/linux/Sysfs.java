/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.linux;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.Util;
import oshi.util.platform.linux.SysPath;

@ThreadSafe
public final class Sysfs {
    private Sysfs() {
    }

    public static String querySystemVendor() {
        String sysVendor = FileUtil.getStringFromFile(SysPath.DMI_ID + "sys_vendor").trim();
        if (!sysVendor.isEmpty()) {
            return sysVendor;
        }
        return null;
    }

    public static String queryProductModel() {
        String productName = FileUtil.getStringFromFile(SysPath.DMI_ID + "product_name").trim();
        String productVersion = FileUtil.getStringFromFile(SysPath.DMI_ID + "product_version").trim();
        if (productName.isEmpty()) {
            if (!productVersion.isEmpty()) {
                return productVersion;
            }
        } else {
            if (!productVersion.isEmpty() && !"None".equals(productVersion)) {
                return productName + " (version: " + productVersion + ")";
            }
            return productName;
        }
        return null;
    }

    public static String queryProductSerial() {
        String serial = FileUtil.getStringFromFile(SysPath.DMI_ID + "product_serial");
        if (!serial.isEmpty() && !"None".equals(serial)) {
            return serial;
        }
        return Sysfs.queryBoardSerial();
    }

    public static String queryUUID() {
        String uuid = FileUtil.getStringFromFile(SysPath.DMI_ID + "product_uuid");
        if (!uuid.isEmpty() && !"None".equals(uuid)) {
            return uuid;
        }
        return null;
    }

    public static String queryBoardVendor() {
        String boardVendor = FileUtil.getStringFromFile(SysPath.DMI_ID + "board_vendor").trim();
        if (!boardVendor.isEmpty()) {
            return boardVendor;
        }
        return null;
    }

    public static String queryBoardModel() {
        String boardName = FileUtil.getStringFromFile(SysPath.DMI_ID + "board_name").trim();
        if (!boardName.isEmpty()) {
            return boardName;
        }
        return null;
    }

    public static String queryBoardVersion() {
        String boardVersion = FileUtil.getStringFromFile(SysPath.DMI_ID + "board_version").trim();
        if (!boardVersion.isEmpty()) {
            return boardVersion;
        }
        return null;
    }

    public static String queryBoardSerial() {
        String boardSerial = FileUtil.getStringFromFile(SysPath.DMI_ID + "board_serial").trim();
        if (!boardSerial.isEmpty()) {
            return boardSerial;
        }
        return null;
    }

    public static String queryBiosVendor() {
        String biosVendor = FileUtil.getStringFromFile(SysPath.DMI_ID + "bios_vendor").trim();
        if (biosVendor.isEmpty()) {
            return biosVendor;
        }
        return null;
    }

    public static String queryBiosDescription() {
        String modalias = FileUtil.getStringFromFile(SysPath.DMI_ID + "modalias").trim();
        if (!modalias.isEmpty()) {
            return modalias;
        }
        return null;
    }

    public static String queryBiosVersion(String biosRevision) {
        String biosVersion = FileUtil.getStringFromFile(SysPath.DMI_ID + "bios_version").trim();
        if (!biosVersion.isEmpty()) {
            return biosVersion + (String)(Util.isBlank(biosRevision) ? "" : " (revision " + biosRevision + ")");
        }
        return null;
    }

    public static String queryBiosReleaseDate() {
        String biosDate = FileUtil.getStringFromFile(SysPath.DMI_ID + "bios_date").trim();
        if (!biosDate.isEmpty()) {
            return ParseUtil.parseMmDdYyyyToYyyyMmDD(biosDate);
        }
        return null;
    }
}


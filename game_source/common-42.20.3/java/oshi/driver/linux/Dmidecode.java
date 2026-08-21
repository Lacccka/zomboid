/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.linux;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.UserGroupInfo;
import oshi.util.tuples.Pair;

@ThreadSafe
public final class Dmidecode {
    private Dmidecode() {
    }

    public static String querySerialNumber() {
        if (UserGroupInfo.isElevated()) {
            String marker = "Serial Number:";
            for (String checkLine : ExecutingCommand.runNative("dmidecode -t system")) {
                if (!checkLine.contains(marker)) continue;
                return checkLine.split(marker)[1].trim();
            }
        }
        return null;
    }

    public static String queryUUID() {
        if (UserGroupInfo.isElevated()) {
            String marker = "UUID:";
            for (String checkLine : ExecutingCommand.runNative("dmidecode -t system")) {
                if (!checkLine.contains(marker)) continue;
                return checkLine.split(marker)[1].trim();
            }
        }
        return null;
    }

    public static Pair<String, String> queryBiosNameRev() {
        String biosName = null;
        String revision = null;
        if (UserGroupInfo.isElevated()) {
            String biosMarker = "SMBIOS";
            String revMarker = "Bios Revision:";
            for (String checkLine : ExecutingCommand.runNative("dmidecode -t bios")) {
                String[] biosArr;
                if (checkLine.contains("SMBIOS") && (biosArr = ParseUtil.whitespaces.split(checkLine)).length >= 2) {
                    biosName = biosArr[0] + " " + biosArr[1];
                }
                if (!checkLine.contains("Bios Revision:")) continue;
                revision = checkLine.split("Bios Revision:")[1].trim();
                break;
            }
        }
        return new Pair<Object, Object>(biosName, revision);
    }
}


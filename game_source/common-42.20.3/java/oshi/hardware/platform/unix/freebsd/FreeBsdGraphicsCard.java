/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.freebsd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.GraphicsCard;
import oshi.hardware.common.AbstractGraphicsCard;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@Immutable
final class FreeBsdGraphicsCard
extends AbstractGraphicsCard {
    private static final String PCI_CLASS_DISPLAY = "0x03";

    FreeBsdGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    public static List<GraphicsCard> getGraphicsCards() {
        ArrayList<GraphicsCard> cardList = new ArrayList<GraphicsCard>();
        List<String> devices = ExecutingCommand.runNative("pciconf -lv");
        if (devices.isEmpty()) {
            return Collections.emptyList();
        }
        String name = "unknown";
        Object vendorId = "unknown";
        String productId = "unknown";
        String classCode = "";
        String versionInfo = "unknown";
        for (String line : devices) {
            String[] split;
            if (line.contains("class=0x")) {
                if (PCI_CLASS_DISPLAY.equals(classCode)) {
                    cardList.add(new FreeBsdGraphicsCard(name.isEmpty() ? "unknown" : name, productId.isEmpty() ? "unknown" : productId, (String)(((String)vendorId).isEmpty() ? "unknown" : vendorId), versionInfo.isEmpty() ? "unknown" : versionInfo, 0L));
                }
                for (String s : split = ParseUtil.whitespaces.split(line)) {
                    String[] keyVal = s.split("=");
                    if (keyVal.length <= 1) continue;
                    if (keyVal[0].equals("class") && keyVal[1].length() >= 4) {
                        classCode = keyVal[1].substring(0, 4);
                        continue;
                    }
                    if (keyVal[0].equals("chip") && keyVal[1].length() >= 10) {
                        productId = keyVal[1].substring(0, 6);
                        vendorId = "0x" + keyVal[1].substring(6, 10);
                        continue;
                    }
                    if (!keyVal[0].contains("rev")) continue;
                    versionInfo = s;
                }
                name = "unknown";
                continue;
            }
            split = line.trim().split("=", 2);
            if (split.length != 2) continue;
            String key = split[0].trim();
            if (key.equals("vendor")) {
                vendorId = ParseUtil.getSingleQuoteStringValue(line) + (String)(((String)vendorId).equals("unknown") ? "" : " (" + (String)vendorId + ")");
                continue;
            }
            if (!key.equals("device")) continue;
            name = ParseUtil.getSingleQuoteStringValue(line);
        }
        if (PCI_CLASS_DISPLAY.equals(classCode)) {
            cardList.add(new FreeBsdGraphicsCard(name.isEmpty() ? "unknown" : name, productId.isEmpty() ? "unknown" : productId, (String)(((String)vendorId).isEmpty() ? "unknown" : vendorId), versionInfo.isEmpty() ? "unknown" : versionInfo, 0L));
        }
        return cardList;
    }
}


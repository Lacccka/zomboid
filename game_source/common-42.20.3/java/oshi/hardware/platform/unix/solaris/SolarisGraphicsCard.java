/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.solaris;

import java.util.ArrayList;
import java.util.List;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.GraphicsCard;
import oshi.hardware.common.AbstractGraphicsCard;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@Immutable
final class SolarisGraphicsCard
extends AbstractGraphicsCard {
    private static final String PCI_CLASS_DISPLAY = "0003";

    SolarisGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    public static List<GraphicsCard> getGraphicsCards() {
        ArrayList<GraphicsCard> cardList = new ArrayList<GraphicsCard>();
        List<String> devices = ExecutingCommand.runNative("prtconf -pv");
        if (devices.isEmpty()) {
            return cardList;
        }
        String name = "";
        Object vendorId = "";
        Object productId = "";
        String classCode = "";
        ArrayList<String> versionInfoList = new ArrayList<String>();
        for (String line : devices) {
            if (line.contains("Node 0x")) {
                if (PCI_CLASS_DISPLAY.equals(classCode)) {
                    cardList.add(new SolarisGraphicsCard(name.isEmpty() ? "unknown" : name, (String)(((String)productId).isEmpty() ? "unknown" : productId), (String)(((String)vendorId).isEmpty() ? "unknown" : vendorId), versionInfoList.isEmpty() ? "unknown" : String.join((CharSequence)", ", versionInfoList), 0L));
                }
                name = "";
                vendorId = "unknown";
                productId = "unknown";
                classCode = "";
                versionInfoList.clear();
                continue;
            }
            String[] split = line.trim().split(":", 2);
            if (split.length != 2) continue;
            if (split[0].equals("model")) {
                name = ParseUtil.getSingleQuoteStringValue(line);
                continue;
            }
            if (split[0].equals("name")) {
                if (!name.isEmpty()) continue;
                name = ParseUtil.getSingleQuoteStringValue(line);
                continue;
            }
            if (split[0].equals("vendor-id")) {
                vendorId = "0x" + line.substring(line.length() - 4);
                continue;
            }
            if (split[0].equals("device-id")) {
                productId = "0x" + line.substring(line.length() - 4);
                continue;
            }
            if (split[0].equals("revision-id")) {
                versionInfoList.add(line.trim());
                continue;
            }
            if (!split[0].equals("class-code")) continue;
            classCode = line.substring(line.length() - 8, line.length() - 4);
        }
        if (PCI_CLASS_DISPLAY.equals(classCode)) {
            cardList.add(new SolarisGraphicsCard(name.isEmpty() ? "unknown" : name, (String)(((String)productId).isEmpty() ? "unknown" : productId), (String)(((String)vendorId).isEmpty() ? "unknown" : vendorId), versionInfoList.isEmpty() ? "unknown" : String.join((CharSequence)", ", versionInfoList), 0L));
        }
        return cardList;
    }
}


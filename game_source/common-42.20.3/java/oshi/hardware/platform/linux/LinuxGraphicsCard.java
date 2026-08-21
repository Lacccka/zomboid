/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.linux;

import java.util.ArrayList;
import java.util.List;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.GraphicsCard;
import oshi.hardware.common.AbstractGraphicsCard;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.tuples.Pair;

@Immutable
final class LinuxGraphicsCard
extends AbstractGraphicsCard {
    LinuxGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    public static List<GraphicsCard> getGraphicsCards() {
        List<GraphicsCard> cardList = LinuxGraphicsCard.getGraphicsCardsFromLspci();
        if (cardList.isEmpty()) {
            cardList = LinuxGraphicsCard.getGraphicsCardsFromLshw();
        }
        return cardList;
    }

    private static List<GraphicsCard> getGraphicsCardsFromLspci() {
        ArrayList<GraphicsCard> cardList = new ArrayList<GraphicsCard>();
        List<String> lspci = ExecutingCommand.runNative("lspci -vnnm");
        String name = "unknown";
        Object deviceId = "unknown";
        Object vendor = "unknown";
        ArrayList<String> versionInfoList = new ArrayList<String>();
        boolean found = false;
        String lookupDevice = null;
        for (String line : lspci) {
            Pair<String, String> pair;
            String[] split = line.trim().split(":", 2);
            String prefix = split[0];
            if (prefix.equals("Class") && (line.contains("VGA") || line.contains("3D controller"))) {
                found = true;
            } else if (prefix.equals("Device") && !found && split.length > 1) {
                lookupDevice = split[1].trim();
            }
            if (!found) continue;
            if (split.length < 2) {
                cardList.add(new LinuxGraphicsCard(name, (String)deviceId, (String)vendor, versionInfoList.isEmpty() ? "unknown" : String.join((CharSequence)", ", versionInfoList), LinuxGraphicsCard.queryLspciMemorySize(lookupDevice)));
                versionInfoList.clear();
                found = false;
                continue;
            }
            if (prefix.equals("Device")) {
                pair = ParseUtil.parseLspciMachineReadable(split[1].trim());
                if (pair == null) continue;
                name = pair.getA();
                deviceId = "0x" + pair.getB();
                continue;
            }
            if (prefix.equals("Vendor")) {
                pair = ParseUtil.parseLspciMachineReadable(split[1].trim());
                if (pair != null) {
                    vendor = pair.getA() + " (0x" + pair.getB() + ")";
                    continue;
                }
                vendor = split[1].trim();
                continue;
            }
            if (!prefix.equals("Rev:")) continue;
            versionInfoList.add(line.trim());
        }
        if (found) {
            cardList.add(new LinuxGraphicsCard(name, (String)deviceId, (String)vendor, versionInfoList.isEmpty() ? "unknown" : String.join((CharSequence)", ", versionInfoList), LinuxGraphicsCard.queryLspciMemorySize(lookupDevice)));
        }
        return cardList;
    }

    private static long queryLspciMemorySize(String lookupDevice) {
        long vram = 0L;
        List<String> lspciMem = ExecutingCommand.runNative("lspci -v -s " + lookupDevice);
        for (String mem : lspciMem) {
            if (!mem.contains(" prefetchable")) continue;
            vram += ParseUtil.parseLspciMemorySize(mem);
        }
        return vram;
    }

    private static List<GraphicsCard> getGraphicsCardsFromLshw() {
        ArrayList<GraphicsCard> cardList = new ArrayList<GraphicsCard>();
        List<String> lshw = ExecutingCommand.runNative("lshw -C display");
        String name = "unknown";
        String deviceId = "unknown";
        String vendor = "unknown";
        ArrayList<String> versionInfoList = new ArrayList<String>();
        long vram = 0L;
        int cardNum = 0;
        for (String line : lshw) {
            String[] split = line.trim().split(":");
            if (split[0].startsWith("*-display")) {
                if (cardNum++ <= 0) continue;
                cardList.add(new LinuxGraphicsCard(name, deviceId, vendor, versionInfoList.isEmpty() ? "unknown" : String.join((CharSequence)", ", versionInfoList), vram));
                versionInfoList.clear();
                continue;
            }
            if (split.length != 2) continue;
            String prefix = split[0];
            if (prefix.equals("product")) {
                name = split[1].trim();
                continue;
            }
            if (prefix.equals("vendor")) {
                vendor = split[1].trim();
                continue;
            }
            if (prefix.equals("version")) {
                versionInfoList.add(line.trim());
                continue;
            }
            if (!prefix.startsWith("resources")) continue;
            vram = ParseUtil.parseLshwResourceString(split[1].trim());
        }
        cardList.add(new LinuxGraphicsCard(name, deviceId, vendor, versionInfoList.isEmpty() ? "unknown" : String.join((CharSequence)", ", versionInfoList), vram));
        return cardList;
    }
}


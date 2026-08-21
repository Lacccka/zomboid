/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.openbsd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.GraphicsCard;
import oshi.hardware.common.AbstractGraphicsCard;
import oshi.util.ExecutingCommand;

@Immutable
final class OpenBsdGraphicsCard
extends AbstractGraphicsCard {
    private static final String PCI_CLASS_DISPLAY = "Class: 03 Display";
    private static final Pattern PCI_DUMP_HEADER = Pattern.compile(" \\d+:\\d+:\\d+: (.+)");

    OpenBsdGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    public static List<GraphicsCard> getGraphicsCards() {
        ArrayList<GraphicsCard> cardList = new ArrayList<GraphicsCard>();
        List<String> devices = ExecutingCommand.runNative("pcidump -v");
        if (devices.isEmpty()) {
            return Collections.emptyList();
        }
        String name = "";
        Object vendorId = "";
        Object productId = "";
        boolean classCodeFound = false;
        String versionInfo = "";
        for (String line : devices) {
            int idx;
            Matcher m = PCI_DUMP_HEADER.matcher(line);
            if (m.matches()) {
                if (classCodeFound) {
                    cardList.add(new OpenBsdGraphicsCard(name.isEmpty() ? "unknown" : name, (String)(((String)productId).isEmpty() ? "0x0000" : productId), (String)(((String)vendorId).isEmpty() ? "0x0000" : vendorId), versionInfo.isEmpty() ? "unknown" : versionInfo, 0L));
                }
                name = m.group(1);
                vendorId = "";
                productId = "";
                classCodeFound = false;
                versionInfo = "";
                continue;
            }
            if (!classCodeFound) {
                idx = line.indexOf("Vendor ID: ");
                if (idx >= 0 && line.length() >= idx + 15) {
                    vendorId = "0x" + line.substring(idx + 11, idx + 15);
                }
                if ((idx = line.indexOf("Product ID: ")) >= 0 && line.length() >= idx + 16) {
                    productId = "0x" + line.substring(idx + 12, idx + 16);
                }
                if (!line.contains(PCI_CLASS_DISPLAY)) continue;
                classCodeFound = true;
                continue;
            }
            if (!versionInfo.isEmpty() || (idx = line.indexOf("Revision: ")) < 0) continue;
            versionInfo = line.substring(idx);
        }
        if (classCodeFound) {
            cardList.add(new OpenBsdGraphicsCard(name.isEmpty() ? "unknown" : name, (String)(((String)productId).isEmpty() ? "0x0000" : productId), (String)(((String)vendorId).isEmpty() ? "0x0000" : vendorId), versionInfo.isEmpty() ? "unknown" : versionInfo, 0L));
        }
        return cardList;
    }
}


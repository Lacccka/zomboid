/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.openbsd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.SoundCard;
import oshi.hardware.common.AbstractSoundCard;
import oshi.util.ExecutingCommand;

@Immutable
final class OpenBsdSoundCard
extends AbstractSoundCard {
    private static final Pattern AUDIO_AT = Pattern.compile("audio\\d+ at (.+)");
    private static final Pattern PCI_AT = Pattern.compile("(.+) at pci\\d+ dev \\d+ function \\d+ \"(.*)\" (rev .+):.*");

    OpenBsdSoundCard(String kernelVersion, String name, String codec) {
        super(kernelVersion, name, codec);
    }

    public static List<SoundCard> getSoundCards() {
        List<String> dmesg = ExecutingCommand.runNative("dmesg");
        HashSet<String> names = new HashSet<String>();
        for (String line : dmesg) {
            Matcher m = AUDIO_AT.matcher(line);
            if (!m.matches()) continue;
            names.add(m.group(1));
        }
        HashMap<String, String> nameMap = new HashMap<String, String>();
        HashMap<String, String> codecMap = new HashMap<String, String>();
        HashMap<String, String> versionMap = new HashMap<String, String>();
        String key = "";
        for (String line : dmesg) {
            Matcher m = PCI_AT.matcher(line);
            if (m.matches() && names.contains(m.group(1))) {
                key = m.group(1);
                nameMap.put(key, m.group(2));
                versionMap.put(key, m.group(3));
                continue;
            }
            if (key.isEmpty()) continue;
            int idx = line.indexOf("codec");
            if (idx >= 0) {
                idx = line.indexOf(58);
                codecMap.put(key, line.substring(idx + 1).trim());
            }
            key = "";
        }
        ArrayList<SoundCard> soundCards = new ArrayList<SoundCard>();
        for (Map.Entry entry : nameMap.entrySet()) {
            soundCards.add(new OpenBsdSoundCard((String)versionMap.get(entry.getKey()), (String)entry.getValue(), (String)codecMap.get(entry.getKey())));
        }
        return soundCards;
    }
}


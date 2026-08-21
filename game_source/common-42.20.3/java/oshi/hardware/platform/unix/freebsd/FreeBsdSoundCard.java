/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.freebsd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.SoundCard;
import oshi.hardware.common.AbstractSoundCard;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@Immutable
final class FreeBsdSoundCard
extends AbstractSoundCard {
    private static final String LSHAL = "lshal";

    FreeBsdSoundCard(String kernelVersion, String name, String codec) {
        super(kernelVersion, name, codec);
    }

    public static List<SoundCard> getSoundCards() {
        HashMap<String, String> vendorMap = new HashMap<String, String>();
        HashMap<String, String> productMap = new HashMap<String, String>();
        vendorMap.clear();
        productMap.clear();
        ArrayList<String> sounds = new ArrayList<String>();
        String key = "";
        for (String line : ExecutingCommand.runNative(LSHAL)) {
            if ((line = line.trim()).startsWith("udi =")) {
                key = ParseUtil.getSingleQuoteStringValue(line);
                continue;
            }
            if (key.isEmpty() || line.isEmpty()) continue;
            if (line.contains("freebsd.driver =") && "pcm".equals(ParseUtil.getSingleQuoteStringValue(line))) {
                sounds.add(key);
                continue;
            }
            if (line.contains("info.product")) {
                productMap.put(key, ParseUtil.getStringBetween(line, '\''));
                continue;
            }
            if (!line.contains("info.vendor")) continue;
            vendorMap.put(key, ParseUtil.getStringBetween(line, '\''));
        }
        ArrayList<SoundCard> soundCards = new ArrayList<SoundCard>();
        for (String s : sounds) {
            soundCards.add(new FreeBsdSoundCard((String)productMap.get(s), (String)vendorMap.get(s) + " " + (String)productMap.get(s), (String)productMap.get(s)));
        }
        return soundCards;
    }
}


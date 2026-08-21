/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.solaris;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.SoundCard;
import oshi.hardware.common.AbstractSoundCard;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@Immutable
final class SolarisSoundCard
extends AbstractSoundCard {
    private static final String LSHAL = "lshal";
    private static final String DEFAULT_AUDIO_DRIVER = "audio810";

    SolarisSoundCard(String kernelVersion, String name, String codec) {
        super(kernelVersion, name, codec);
    }

    public static List<SoundCard> getSoundCards() {
        HashMap<String, String> vendorMap = new HashMap<String, String>();
        HashMap<String, String> productMap = new HashMap<String, String>();
        ArrayList<String> sounds = new ArrayList<String>();
        String key = "";
        for (String line : ExecutingCommand.runNative(LSHAL)) {
            if ((line = line.trim()).startsWith("udi =")) {
                key = ParseUtil.getSingleQuoteStringValue(line);
                continue;
            }
            if (key.isEmpty() || line.isEmpty()) continue;
            if (line.contains("info.solaris.driver =") && DEFAULT_AUDIO_DRIVER.equals(ParseUtil.getSingleQuoteStringValue(line))) {
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
            soundCards.add(new SolarisSoundCard((String)productMap.get(s) + " audio810", (String)vendorMap.get(s) + " " + (String)productMap.get(s), (String)productMap.get(s)));
        }
        return soundCards;
    }
}


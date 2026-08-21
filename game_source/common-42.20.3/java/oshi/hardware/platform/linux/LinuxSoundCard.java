/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.linux;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.SoundCard;
import oshi.hardware.common.AbstractSoundCard;
import oshi.util.FileUtil;
import oshi.util.platform.linux.ProcPath;

@Immutable
final class LinuxSoundCard
extends AbstractSoundCard {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxSoundCard.class);
    private static final String CARD_FOLDER = "card";
    private static final String CARDS_FILE = "cards";
    private static final String ID_FILE = "id";

    LinuxSoundCard(String kernelVersion, String name, String codec) {
        super(kernelVersion, name, codec);
    }

    private static List<File> getCardFolders() {
        File cardsDirectory = new File(ProcPath.ASOUND);
        ArrayList<File> cardFolders = new ArrayList<File>();
        File[] allContents = cardsDirectory.listFiles();
        if (allContents != null) {
            for (File card : allContents) {
                if (!card.getName().startsWith(CARD_FOLDER) || !card.isDirectory()) continue;
                cardFolders.add(card);
            }
        } else {
            LOG.warn("No Audio Cards Found");
        }
        return cardFolders;
    }

    private static String getSoundCardVersion() {
        String driverVersion = FileUtil.getStringFromFile(ProcPath.ASOUND + "version");
        return driverVersion.isEmpty() ? "not available" : driverVersion;
    }

    private static String getCardCodec(File cardDir) {
        String cardCodec = "";
        File[] cardFiles = cardDir.listFiles();
        if (cardFiles != null) {
            block0: for (File file : cardFiles) {
                if (!file.getName().startsWith("codec")) continue;
                if (!file.isDirectory()) {
                    cardCodec = FileUtil.getKeyValueMapFromFile(file.getPath(), ":").get("Codec");
                    continue;
                }
                File[] codecs = file.listFiles();
                if (codecs == null) continue;
                for (File codec : codecs) {
                    if (codec.isDirectory() || !codec.getName().contains("#")) continue;
                    cardCodec = codec.getName().substring(0, codec.getName().indexOf(35));
                    continue block0;
                }
            }
        }
        return cardCodec;
    }

    private static String getCardName(File file) {
        String cardName = "Not Found..";
        Map<String, String> cardNamePairs = FileUtil.getKeyValueMapFromFile(ProcPath.ASOUND + "/cards", ":");
        String cardId = FileUtil.getStringFromFile(file.getPath() + "/id");
        for (Map.Entry<String, String> entry : cardNamePairs.entrySet()) {
            if (!entry.getKey().contains(cardId)) continue;
            cardName = entry.getValue();
            return cardName;
        }
        return cardName;
    }

    public static List<SoundCard> getSoundCards() {
        ArrayList<SoundCard> soundCards = new ArrayList<SoundCard>();
        for (File cardFile : LinuxSoundCard.getCardFolders()) {
            soundCards.add(new LinuxSoundCard(LinuxSoundCard.getSoundCardVersion(), LinuxSoundCard.getCardName(cardFile), LinuxSoundCard.getCardCodec(cardFile)));
        }
        return soundCards;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package zombie.iso;

import java.util.ArrayList;
import java.util.Comparator;
import zombie.audio.FMODParameterUtils;
import zombie.iso.IsoGridSquare;

public class FishSplashSoundManager {
    private static final int SPLASH_SOUND_RADIUS = 20;
    public static final FishSplashSoundManager instance = new FishSplashSoundManager();
    private final ArrayList<IsoGridSquare> squares = new ArrayList();
    private final long[] soundTime = new long[6];
    private final Comparator<IsoGridSquare> comp = (a, b) -> {
        float bScore;
        float aScore = FMODParameterUtils.getClosestListenerDistanceSquared((float)a.x + 0.5f, (float)a.y + 0.5f, a.z);
        if (aScore > (bScore = FMODParameterUtils.getClosestListenerDistanceSquared((float)b.x + 0.5f, (float)b.y + 0.5f, b.z))) {
            return 1;
        }
        if (aScore < bScore) {
            return -1;
        }
        return 0;
    };

    public void addSquare(IsoGridSquare square) {
        if (!this.squares.contains(square)) {
            this.squares.add(square);
        }
    }

    public void update() {
        if (this.squares.isEmpty()) {
            return;
        }
        this.squares.sort(this.comp);
        long ms = System.currentTimeMillis();
        for (int i = 0; i < this.soundTime.length && i < this.squares.size(); ++i) {
            IsoGridSquare square = this.squares.get(i);
            if (FMODParameterUtils.getClosestListenerDistanceSquared((float)square.x + 0.5f, (float)square.y + 0.5f, square.z) > 400.0f) continue;
            int slot = this.getFreeSoundSlot(ms);
            if (slot == -1) break;
            square.playSoundLocal("FishBreath");
            this.soundTime[slot] = ms;
        }
        this.squares.clear();
    }

    private int getFreeSoundSlot(long ms) {
        long oldestTime = Long.MAX_VALUE;
        int oldestIndex = -1;
        for (int i = 0; i < this.soundTime.length; ++i) {
            if (this.soundTime[i] >= oldestTime) continue;
            oldestTime = this.soundTime[i];
            oldestIndex = i;
        }
        if (ms - oldestTime < 3000L) {
            return -1;
        }
        return oldestIndex;
    }
}


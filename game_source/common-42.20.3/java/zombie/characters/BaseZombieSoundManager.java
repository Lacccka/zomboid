/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import zombie.audio.FMODParameterUtils;
import zombie.characters.IsoZombie;

public abstract class BaseZombieSoundManager {
    protected final ArrayList<IsoZombie> characters = new ArrayList();
    private final long[] soundTime;
    private final int staleSlotMs;
    private final Comparator<IsoZombie> comp = new Comparator<IsoZombie>(this){
        {
            Objects.requireNonNull(this$0);
        }

        @Override
        public int compare(IsoZombie a, IsoZombie b) {
            float bScore;
            float aScore = FMODParameterUtils.getClosestListenerDistanceSquared(a.getX(), a.getY(), a.getZ());
            if (aScore > (bScore = FMODParameterUtils.getClosestListenerDistanceSquared(b.getX(), b.getY(), b.getZ()))) {
                return 1;
            }
            if (aScore < bScore) {
                return -1;
            }
            return 0;
        }
    };

    public BaseZombieSoundManager(int numSlots, int staleSlotMs) {
        this.soundTime = new long[numSlots];
        this.staleSlotMs = staleSlotMs;
    }

    public void addCharacter(IsoZombie chr) {
        if (!this.characters.contains(chr)) {
            this.characters.add(chr);
        }
    }

    public void update() {
        if (this.characters.isEmpty()) {
            return;
        }
        this.characters.sort(this.comp);
        long ms = System.currentTimeMillis();
        for (int i = 0; i < this.soundTime.length && i < this.characters.size(); ++i) {
            IsoZombie chr = this.characters.get(i);
            if (chr.getCurrentSquare() == null) continue;
            int slot = this.getFreeSoundSlot(ms);
            if (slot == -1) break;
            this.playSound(chr);
            this.soundTime[slot] = ms;
        }
        this.postUpdate();
        this.characters.clear();
    }

    public abstract void playSound(IsoZombie var1);

    public abstract void postUpdate();

    private int getFreeSoundSlot(long ms) {
        long oldestTime = Long.MAX_VALUE;
        int oldestIndex = -1;
        for (int i = 0; i < this.soundTime.length; ++i) {
            if (this.soundTime[i] >= oldestTime) continue;
            oldestTime = this.soundTime[i];
            oldestIndex = i;
        }
        if (ms - oldestTime < (long)this.staleSlotMs) {
            return -1;
        }
        return oldestIndex;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import zombie.audio.FMODParameterUtils;
import zombie.characters.animals.AnimalSoundState;
import zombie.characters.animals.IsoAnimal;
import zombie.core.math.PZMath;
import zombie.network.GameServer;
import zombie.popman.ObjectPool;
import zombie.util.list.PZArrayUtil;

public final class AnimalVocalsManager {
    public static final int SOUND_RANGE = 70;
    public static final AnimalVocalsManager instance = new AnimalVocalsManager();
    private final HashSet<IsoAnimal> added = new HashSet();
    private final ObjectPool<ObjectWithDistance> objectPool = new ObjectPool<ObjectWithDistance>(ObjectWithDistance::new, "AnimalVocalsManager.objectPool");
    private final ArrayList<ObjectWithDistance> objects = new ArrayList();
    private final Slot[] slots;
    private long updateMs;
    private final Comparator<ObjectWithDistance> comp = new Comparator<ObjectWithDistance>(this){
        {
            Objects.requireNonNull(this$0);
        }

        @Override
        public int compare(ObjectWithDistance a, ObjectWithDistance b) {
            return Float.compare(a.distSq, b.distSq);
        }
    };

    public AnimalVocalsManager() {
        int numSlots = 20;
        this.slots = PZArrayUtil.newInstance(Slot.class, 20, Slot::new);
    }

    public void addCharacter(IsoAnimal chr) {
        if (this.added.contains(chr)) {
            return;
        }
        this.added.add(chr);
        ObjectWithDistance owd = this.objectPool.alloc();
        owd.character = chr;
        this.objects.add(owd);
    }

    public void update() {
        int j;
        IsoAnimal object;
        int i;
        int i2;
        if (GameServer.server) {
            return;
        }
        long ms = System.currentTimeMillis();
        if (ms - this.updateMs < 500L) {
            return;
        }
        this.updateMs = ms;
        for (i2 = 0; i2 < this.slots.length; ++i2) {
            this.slots[i2].playing = false;
        }
        if (this.objects.isEmpty()) {
            this.stopNotPlaying();
            return;
        }
        for (i2 = 0; i2 < this.objects.size(); ++i2) {
            ObjectWithDistance owd = this.objects.get(i2);
            IsoAnimal chr = owd.character;
            owd.distSq = FMODParameterUtils.getClosestListenerDistanceSquared(chr.getX(), chr.getY(), chr.getZ());
        }
        this.objects.sort(this.comp);
        int count = PZMath.min(this.slots.length, this.objects.size());
        for (i = 0; i < count; ++i) {
            object = this.objects.get((int)i).character;
            if (!this.shouldPlay(object) || (j = this.getExistingSlot(object)) == -1) continue;
            this.slots[j].playSound(object);
        }
        for (i = 0; i < count; ++i) {
            object = this.objects.get((int)i).character;
            if (!this.shouldPlay(object) || (j = this.getExistingSlot(object)) != -1) continue;
            j = this.getFreeSlot();
            this.slots[j].playSound(object);
        }
        this.stopNotPlaying();
        this.postUpdate();
        this.added.clear();
        this.objectPool.release((List<ObjectWithDistance>)this.objects);
        this.objects.clear();
    }

    boolean shouldPlay(IsoAnimal chr) {
        if (chr.isDead()) {
            return false;
        }
        if (chr.getVehicle() != null ? chr.getVehicle().getMovingObjectIndex() == -1 : chr.getCurrentSquare() == null) {
            return false;
        }
        return chr.getAnimalSoundState("voice").shouldPlay();
    }

    int getExistingSlot(IsoAnimal chr) {
        for (int i = 0; i < this.slots.length; ++i) {
            if (this.slots[i].character != chr) continue;
            return i;
        }
        return -1;
    }

    int getFreeSlot() {
        for (int i = 0; i < this.slots.length; ++i) {
            if (this.slots[i].playing) continue;
            return i;
        }
        return -1;
    }

    void stopNotPlaying() {
        for (int i = 0; i < this.slots.length; ++i) {
            Slot slot = this.slots[i];
            if (slot.playing) continue;
            slot.stopPlaying();
            slot.character = null;
        }
    }

    public void postUpdate() {
    }

    public void render() {
    }

    public static void Reset() {
        for (int i = 0; i < AnimalVocalsManager.instance.slots.length; ++i) {
            AnimalVocalsManager.instance.slots[i].stopPlaying();
            AnimalVocalsManager.instance.slots[i].character = null;
            AnimalVocalsManager.instance.slots[i].playing = false;
        }
    }

    static final class Slot {
        IsoAnimal character;
        boolean playing;

        Slot() {
        }

        void playSound(IsoAnimal chr) {
            if (this.character != null && this.character != chr && this.character.getAnimalSoundState("voice").getEventInstance() != 0L) {
                this.character.getAnimalSoundState("voice").stop();
            }
            this.character = chr;
            this.playing = true;
            AnimalSoundState ass = this.character.getAnimalSoundState("voice");
            if (!ass.isPlayingDesiredSound()) {
                String soundName = ass.getDesiredSoundName();
                int priority = ass.getDesiredSoundPriority();
                chr.getAnimalSoundState("voice").start(soundName, priority);
            }
        }

        void stopPlaying() {
            if (this.character == null || this.character.getAnimalSoundState("voice").getEventInstance() == 0L) {
                return;
            }
            if (this.character.isDead()) {
                return;
            }
            this.character.getAnimalSoundState("voice").stop();
        }
    }

    static final class ObjectWithDistance {
        IsoAnimal character;
        float distSq;

        ObjectWithDistance() {
        }
    }
}


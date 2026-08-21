/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import zombie.audio.FMODParameterUtils;
import zombie.characters.IsoZombie;
import zombie.core.math.PZMath;
import zombie.debug.DebugOptions;
import zombie.debug.LineDrawer;
import zombie.network.GameServer;
import zombie.popman.ObjectPool;
import zombie.util.list.PZArrayUtil;

public final class ZombieVocalsManager {
    public static final int SOUND_RANGE = 70;
    private static final long UPDATE_INTERVAL = 500L;
    public static final ZombieVocalsManager instance = new ZombieVocalsManager();
    private final Set<IsoZombie> added = new HashSet<IsoZombie>();
    private final ObjectPool<ObjectWithDistance> objectPool = new ObjectPool<ObjectWithDistance>(ObjectWithDistance::new, "ZombieVocalsManager.objectPool");
    private final List<ObjectWithDistance> objects = new ArrayList<ObjectWithDistance>();
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

    public ZombieVocalsManager() {
        int numSlots = 20;
        this.slots = PZArrayUtil.newInstance(Slot.class, 20, Slot::new);
    }

    public void addCharacter(IsoZombie chr) {
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
        IsoZombie object;
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
            IsoZombie chr = owd.character;
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
        for (i = 0; i < this.objects.size(); ++i) {
            ObjectWithDistance owd = this.objects.get(i);
            owd.character = null;
        }
        this.objectPool.release(this.objects);
        this.objects.clear();
    }

    boolean shouldPlay(IsoZombie chr) {
        return chr.getCurrentSquare() != null;
    }

    int getExistingSlot(IsoZombie chr) {
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
        if (!DebugOptions.instance.zombieVocalsRender.getValue()) {
            return;
        }
        for (int i = 0; i < this.slots.length; ++i) {
            Slot slot = this.slots[i];
            if (!slot.playing) continue;
            IsoZombie chr = slot.character;
            LineDrawer.addRect(chr.getX() - 0.45f, chr.getY() - 0.45f, chr.getZ(), 0.9f, 0.9f, 0.0f, 0.0f, 1.0f);
        }
    }

    public static void Reset() {
        int i;
        for (i = 0; i < ZombieVocalsManager.instance.slots.length; ++i) {
            ZombieVocalsManager.instance.slots[i].stopPlaying();
            ZombieVocalsManager.instance.slots[i].character = null;
            ZombieVocalsManager.instance.slots[i].playing = false;
        }
        for (i = 0; i < ZombieVocalsManager.instance.objects.size(); ++i) {
            ZombieVocalsManager.instance.objects.get((int)i).character = null;
        }
        ZombieVocalsManager.instance.objectPool.releaseAll(ZombieVocalsManager.instance.objects);
        ZombieVocalsManager.instance.objects.clear();
        ZombieVocalsManager.instance.added.clear();
    }

    static final class Slot {
        IsoZombie character;
        boolean playing;

        Slot() {
        }

        void playSound(IsoZombie chr) {
            if (this.character != null && this.character != chr && this.character.vocalEvent != 0L) {
                this.character.getEmitter().stopSoundLocal(this.character.vocalEvent);
                this.character.vocalEvent = 0L;
            }
            this.character = chr;
            this.playing = true;
            if (this.character.vocalEvent == 0L) {
                String soundName = chr.getVoiceSoundName();
                if (!chr.getFMODParameters().parameterList.contains(chr.parameterZombieState)) {
                    chr.getFMODParameters().add(chr.parameterCharacterInside);
                    chr.getFMODParameters().add(chr.parameterCharacterOnFire);
                    chr.getFMODParameters().add(chr.parameterPlayerDistance);
                    chr.getFMODParameters().add(chr.parameterZombieState);
                    chr.parameterCharacterInside.update();
                    chr.parameterCharacterOnFire.update();
                    chr.parameterPlayerDistance.update();
                    chr.parameterZombieState.update();
                }
                chr.vocalEvent = chr.getEmitter().playVocals(soundName);
            }
        }

        void stopPlaying() {
            if (this.character == null || this.character.vocalEvent == 0L) {
                return;
            }
            this.character.getEmitter().stopSoundLocal(this.character.vocalEvent);
            this.character.vocalEvent = 0L;
        }
    }

    static final class ObjectWithDistance {
        IsoZombie character;
        float distSq;

        ObjectWithDistance() {
        }
    }
}


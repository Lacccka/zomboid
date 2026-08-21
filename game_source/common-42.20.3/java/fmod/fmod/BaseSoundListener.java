/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

public abstract class BaseSoundListener {
    public int index;
    public float x;
    public float y;
    public float z;

    public BaseSoundListener(int index) {
        this.index = index;
    }

    public void setPos(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public abstract void tick();
}


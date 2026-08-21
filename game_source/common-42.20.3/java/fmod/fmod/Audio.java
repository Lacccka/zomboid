/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

public interface Audio {
    public boolean isPlaying();

    public void setVolume(float var1);

    public void start();

    public void pause();

    public void stop();

    public void setName(String var1);

    public String getName();
}


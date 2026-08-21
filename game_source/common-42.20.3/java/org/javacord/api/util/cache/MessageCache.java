/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.cache;

public interface MessageCache {
    public int getCapacity();

    public void setCapacity(int var1);

    public int getStorageTimeInSeconds();

    public void setStorageTimeInSeconds(int var1);

    public void setAutomaticCleanupEnabled(boolean var1);
}


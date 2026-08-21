/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import se.krka.kahlua.vm.KahluaTable;

public interface Platform {
    public double pow(double var1, double var3);

    public KahluaTable newTable();

    public KahluaTable newEnvironment();

    public void setupEnvironment(KahluaTable var1);
}


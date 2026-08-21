/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import se.krka.kahlua.vm.KahluaTableIterator;

public interface KahluaTable {
    public void setMetatable(KahluaTable var1);

    public KahluaTable getMetatable();

    public void rawset(Object var1, Object var2);

    public Object rawget(Object var1);

    public void rawset(int var1, Object var2);

    public Object rawget(int var1);

    public int len();

    public KahluaTableIterator iterator();

    public boolean isEmpty();

    public void wipe();

    public int size();

    public void save(ByteBuffer var1) throws IOException;

    public void load(ByteBuffer var1, int var2) throws IOException;

    public void save(DataOutputStream var1) throws IOException;

    public void load(DataInputStream var1, int var2) throws IOException;

    public String getString(String var1);
}


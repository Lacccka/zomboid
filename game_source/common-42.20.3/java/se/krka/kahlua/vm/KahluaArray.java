/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaTableIterator;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;

public class KahluaArray
implements KahluaTable {
    private KahluaTable metatable;
    private Object[] data = new Object[16];
    private int len = 0;
    private boolean recalculateLen;

    @Override
    public String getString(String string) {
        return (String)this.rawget(string);
    }

    @Override
    public int size() {
        return this.len();
    }

    @Override
    public int len() {
        if (this.recalculateLen) {
            int index;
            Object[] data = this.data;
            for (index = this.len - 1; index >= 0 && data[index] == null; --index) {
            }
            this.len = index + 1;
            this.recalculateLen = false;
        }
        return this.len;
    }

    @Override
    public KahluaTableIterator iterator() {
        return new KahluaTableIterator(this){
            private Double curKey;
            private Object curValue;
            private int index;
            final /* synthetic */ KahluaArray this$0;
            {
                KahluaArray kahluaArray = this$0;
                Objects.requireNonNull(kahluaArray);
                this.this$0 = kahluaArray;
                this.index = 1;
            }

            @Override
            public int call(LuaCallFrame callFrame, int nArguments) {
                if (this.advance()) {
                    return callFrame.push(this.getKey(), this.getValue());
                }
                return 0;
            }

            @Override
            public boolean advance() {
                while (this.index <= this.this$0.len()) {
                    Object value = this.this$0.rawget(this.index);
                    if (value != null) {
                        int localIndex = this.index++;
                        this.curKey = KahluaUtil.toDouble(localIndex);
                        this.curValue = value;
                        return true;
                    }
                    ++this.index;
                }
                return false;
            }

            @Override
            public Object getKey() {
                return this.curKey;
            }

            @Override
            public Object getValue() {
                return this.curValue;
            }
        };
    }

    @Override
    public boolean isEmpty() {
        return this.len() == 0;
    }

    @Override
    public void wipe() {
        for (int i = 0; i < this.data.length; ++i) {
            this.data[i] = null;
        }
        this.len = 0;
    }

    @Override
    public Object rawget(int index) {
        if (index < 1 || index > this.len) {
            return null;
        }
        return this.data[index - 1];
    }

    @Override
    public void rawset(int index, Object value) {
        if (index <= 0) {
            KahluaUtil.fail("Index out of range: " + index);
        }
        if (index >= this.len) {
            if (value == null) {
                if (index == this.len) {
                    this.data[index - 1] = value;
                    this.recalculateLen = true;
                }
                return;
            }
            if (this.data.length < index) {
                int newMaxLen = 2 * index;
                int newCap = newMaxLen - 1;
                Object[] newData = new Object[newCap];
                System.arraycopy(this.data, 0, newData, 0, this.len);
                this.data = newData;
            }
            this.len = index;
        }
        this.data[index - 1] = value;
    }

    private int getKeyIndex(Object key) {
        if (key instanceof Double) {
            Double d = (Double)key;
            return d.intValue();
        }
        return -1;
    }

    @Override
    public Object rawget(Object key) {
        int index = this.getKeyIndex(key);
        return this.rawget(index);
    }

    @Override
    public void rawset(Object key, Object value) {
        int index = this.getKeyIndex(key);
        if (index == -1) {
            KahluaUtil.fail("Invalid table key: " + String.valueOf(key));
        }
        this.rawset(index, value);
    }

    public Object next(Object key) {
        int index;
        if (key == null) {
            index = 0;
        } else {
            index = this.getKeyIndex(key);
            if (index <= 0 || index > this.len) {
                KahluaUtil.fail("invalid key to 'next'");
                return null;
            }
        }
        while (index < this.len) {
            if (this.data[index] != null) {
                return KahluaUtil.toDouble(index + 1);
            }
            ++index;
        }
        return null;
    }

    @Override
    public KahluaTable getMetatable() {
        return this.metatable;
    }

    @Override
    public void setMetatable(KahluaTable metatable) {
        this.metatable = metatable;
    }

    public Class<?> getJavaClass() {
        return null;
    }

    @Override
    public void save(ByteBuffer output) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void load(ByteBuffer input, int WorldVersion) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void save(DataOutputStream output) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void load(DataInputStream input, int WorldVersion) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}


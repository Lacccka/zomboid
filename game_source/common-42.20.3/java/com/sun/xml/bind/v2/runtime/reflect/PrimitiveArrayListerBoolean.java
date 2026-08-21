/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;

final class PrimitiveArrayListerBoolean<BeanT>
extends Lister<BeanT, boolean[], Boolean, BooleanArrayPack> {
    private PrimitiveArrayListerBoolean() {
    }

    static void register() {
        Lister.primitiveArrayListers.put(Boolean.TYPE, new PrimitiveArrayListerBoolean());
    }

    @Override
    public ListIterator<Boolean> iterator(final boolean[] objects, XMLSerializer context) {
        return new ListIterator<Boolean>(){
            int idx = 0;

            @Override
            public boolean hasNext() {
                return this.idx < objects.length;
            }

            @Override
            public Boolean next() {
                return objects[this.idx++];
            }
        };
    }

    @Override
    public BooleanArrayPack startPacking(BeanT current, Accessor<BeanT, boolean[]> acc) {
        return new BooleanArrayPack();
    }

    @Override
    public void addToPack(BooleanArrayPack objects, Boolean o) {
        objects.add(o);
    }

    @Override
    public void endPacking(BooleanArrayPack pack, BeanT bean, Accessor<BeanT, boolean[]> acc) throws AccessorException {
        acc.set(bean, pack.build());
    }

    @Override
    public void reset(BeanT o, Accessor<BeanT, boolean[]> acc) throws AccessorException {
        acc.set(o, new boolean[0]);
    }

    static final class BooleanArrayPack {
        boolean[] buf = new boolean[16];
        int size;

        BooleanArrayPack() {
        }

        void add(Boolean b) {
            if (this.buf.length == this.size) {
                boolean[] nb = new boolean[this.buf.length * 2];
                System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
                this.buf = nb;
            }
            if (b != null) {
                this.buf[this.size++] = b;
            }
        }

        boolean[] build() {
            if (this.buf.length == this.size) {
                return this.buf;
            }
            boolean[] r = new boolean[this.size];
            System.arraycopy(this.buf, 0, r, 0, this.size);
            return r;
        }
    }
}


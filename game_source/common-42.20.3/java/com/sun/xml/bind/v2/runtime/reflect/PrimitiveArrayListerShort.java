/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;

final class PrimitiveArrayListerShort<BeanT>
extends Lister<BeanT, short[], Short, ShortArrayPack> {
    private PrimitiveArrayListerShort() {
    }

    static void register() {
        Lister.primitiveArrayListers.put(Short.TYPE, new PrimitiveArrayListerShort());
    }

    @Override
    public ListIterator<Short> iterator(final short[] objects, XMLSerializer context) {
        return new ListIterator<Short>(){
            int idx = 0;

            @Override
            public boolean hasNext() {
                return this.idx < objects.length;
            }

            @Override
            public Short next() {
                return objects[this.idx++];
            }
        };
    }

    @Override
    public ShortArrayPack startPacking(BeanT current, Accessor<BeanT, short[]> acc) {
        return new ShortArrayPack();
    }

    @Override
    public void addToPack(ShortArrayPack objects, Short o) {
        objects.add(o);
    }

    @Override
    public void endPacking(ShortArrayPack pack, BeanT bean, Accessor<BeanT, short[]> acc) throws AccessorException {
        acc.set(bean, pack.build());
    }

    @Override
    public void reset(BeanT o, Accessor<BeanT, short[]> acc) throws AccessorException {
        acc.set(o, new short[0]);
    }

    static final class ShortArrayPack {
        short[] buf = new short[16];
        int size;

        ShortArrayPack() {
        }

        void add(Short b) {
            if (this.buf.length == this.size) {
                short[] nb = new short[this.buf.length * 2];
                System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
                this.buf = nb;
            }
            if (b != null) {
                this.buf[this.size++] = b;
            }
        }

        short[] build() {
            if (this.buf.length == this.size) {
                return this.buf;
            }
            short[] r = new short[this.size];
            System.arraycopy(this.buf, 0, r, 0, this.size);
            return r;
        }
    }
}


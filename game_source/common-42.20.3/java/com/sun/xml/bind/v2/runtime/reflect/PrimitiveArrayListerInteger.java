/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;

final class PrimitiveArrayListerInteger<BeanT>
extends Lister<BeanT, int[], Integer, IntegerArrayPack> {
    private PrimitiveArrayListerInteger() {
    }

    static void register() {
        Lister.primitiveArrayListers.put(Integer.TYPE, new PrimitiveArrayListerInteger());
    }

    @Override
    public ListIterator<Integer> iterator(final int[] objects, XMLSerializer context) {
        return new ListIterator<Integer>(){
            int idx = 0;

            @Override
            public boolean hasNext() {
                return this.idx < objects.length;
            }

            @Override
            public Integer next() {
                return objects[this.idx++];
            }
        };
    }

    @Override
    public IntegerArrayPack startPacking(BeanT current, Accessor<BeanT, int[]> acc) {
        return new IntegerArrayPack();
    }

    @Override
    public void addToPack(IntegerArrayPack objects, Integer o) {
        objects.add(o);
    }

    @Override
    public void endPacking(IntegerArrayPack pack, BeanT bean, Accessor<BeanT, int[]> acc) throws AccessorException {
        acc.set(bean, pack.build());
    }

    @Override
    public void reset(BeanT o, Accessor<BeanT, int[]> acc) throws AccessorException {
        acc.set(o, new int[0]);
    }

    static final class IntegerArrayPack {
        int[] buf = new int[16];
        int size;

        IntegerArrayPack() {
        }

        void add(Integer b) {
            if (this.buf.length == this.size) {
                int[] nb = new int[this.buf.length * 2];
                System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
                this.buf = nb;
            }
            if (b != null) {
                this.buf[this.size++] = b;
            }
        }

        int[] build() {
            if (this.buf.length == this.size) {
                return this.buf;
            }
            int[] r = new int[this.size];
            System.arraycopy(this.buf, 0, r, 0, this.size);
            return r;
        }
    }
}


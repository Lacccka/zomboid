/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;

final class PrimitiveArrayListerLong<BeanT>
extends Lister<BeanT, long[], Long, LongArrayPack> {
    private PrimitiveArrayListerLong() {
    }

    static void register() {
        Lister.primitiveArrayListers.put(Long.TYPE, new PrimitiveArrayListerLong());
    }

    @Override
    public ListIterator<Long> iterator(final long[] objects, XMLSerializer context) {
        return new ListIterator<Long>(){
            int idx = 0;

            @Override
            public boolean hasNext() {
                return this.idx < objects.length;
            }

            @Override
            public Long next() {
                return objects[this.idx++];
            }
        };
    }

    @Override
    public LongArrayPack startPacking(BeanT current, Accessor<BeanT, long[]> acc) {
        return new LongArrayPack();
    }

    @Override
    public void addToPack(LongArrayPack objects, Long o) {
        objects.add(o);
    }

    @Override
    public void endPacking(LongArrayPack pack, BeanT bean, Accessor<BeanT, long[]> acc) throws AccessorException {
        acc.set(bean, pack.build());
    }

    @Override
    public void reset(BeanT o, Accessor<BeanT, long[]> acc) throws AccessorException {
        acc.set(o, new long[0]);
    }

    static final class LongArrayPack {
        long[] buf = new long[16];
        int size;

        LongArrayPack() {
        }

        void add(Long b) {
            if (this.buf.length == this.size) {
                long[] nb = new long[this.buf.length * 2];
                System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
                this.buf = nb;
            }
            if (b != null) {
                this.buf[this.size++] = b;
            }
        }

        long[] build() {
            if (this.buf.length == this.size) {
                return this.buf;
            }
            long[] r = new long[this.size];
            System.arraycopy(this.buf, 0, r, 0, this.size);
            return r;
        }
    }
}


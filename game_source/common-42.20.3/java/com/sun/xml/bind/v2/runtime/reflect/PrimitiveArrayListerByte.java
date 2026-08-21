/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;

final class PrimitiveArrayListerByte<BeanT>
extends Lister<BeanT, byte[], Byte, ByteArrayPack> {
    private PrimitiveArrayListerByte() {
    }

    static void register() {
        Lister.primitiveArrayListers.put(Byte.TYPE, new PrimitiveArrayListerByte());
    }

    @Override
    public ListIterator<Byte> iterator(final byte[] objects, XMLSerializer context) {
        return new ListIterator<Byte>(){
            int idx = 0;

            @Override
            public boolean hasNext() {
                return this.idx < objects.length;
            }

            @Override
            public Byte next() {
                return objects[this.idx++];
            }
        };
    }

    @Override
    public ByteArrayPack startPacking(BeanT current, Accessor<BeanT, byte[]> acc) {
        return new ByteArrayPack();
    }

    @Override
    public void addToPack(ByteArrayPack objects, Byte o) {
        objects.add(o);
    }

    @Override
    public void endPacking(ByteArrayPack pack, BeanT bean, Accessor<BeanT, byte[]> acc) throws AccessorException {
        acc.set(bean, pack.build());
    }

    @Override
    public void reset(BeanT o, Accessor<BeanT, byte[]> acc) throws AccessorException {
        acc.set(o, new byte[0]);
    }

    static final class ByteArrayPack {
        byte[] buf = new byte[16];
        int size;

        ByteArrayPack() {
        }

        void add(Byte b) {
            if (this.buf.length == this.size) {
                byte[] nb = new byte[this.buf.length * 2];
                System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
                this.buf = nb;
            }
            if (b != null) {
                this.buf[this.size++] = b;
            }
        }

        byte[] build() {
            if (this.buf.length == this.size) {
                return this.buf;
            }
            byte[] r = new byte[this.size];
            System.arraycopy(this.buf, 0, r, 0, this.size);
            return r;
        }
    }
}


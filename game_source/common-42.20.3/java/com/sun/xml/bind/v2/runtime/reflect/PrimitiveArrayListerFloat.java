/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;

final class PrimitiveArrayListerFloat<BeanT>
extends Lister<BeanT, float[], Float, FloatArrayPack> {
    private PrimitiveArrayListerFloat() {
    }

    static void register() {
        Lister.primitiveArrayListers.put(Float.TYPE, new PrimitiveArrayListerFloat());
    }

    @Override
    public ListIterator<Float> iterator(final float[] objects, XMLSerializer context) {
        return new ListIterator<Float>(){
            int idx = 0;

            @Override
            public boolean hasNext() {
                return this.idx < objects.length;
            }

            @Override
            public Float next() {
                return Float.valueOf(objects[this.idx++]);
            }
        };
    }

    @Override
    public FloatArrayPack startPacking(BeanT current, Accessor<BeanT, float[]> acc) {
        return new FloatArrayPack();
    }

    @Override
    public void addToPack(FloatArrayPack objects, Float o) {
        objects.add(o);
    }

    @Override
    public void endPacking(FloatArrayPack pack, BeanT bean, Accessor<BeanT, float[]> acc) throws AccessorException {
        acc.set(bean, pack.build());
    }

    @Override
    public void reset(BeanT o, Accessor<BeanT, float[]> acc) throws AccessorException {
        acc.set(o, new float[0]);
    }

    static final class FloatArrayPack {
        float[] buf = new float[16];
        int size;

        FloatArrayPack() {
        }

        void add(Float b) {
            if (this.buf.length == this.size) {
                float[] nb = new float[this.buf.length * 2];
                System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
                this.buf = nb;
            }
            if (b != null) {
                this.buf[this.size++] = b.floatValue();
            }
        }

        float[] build() {
            if (this.buf.length == this.size) {
                return this.buf;
            }
            float[] r = new float[this.size];
            System.arraycopy(this.buf, 0, r, 0, this.size);
            return r;
        }
    }
}


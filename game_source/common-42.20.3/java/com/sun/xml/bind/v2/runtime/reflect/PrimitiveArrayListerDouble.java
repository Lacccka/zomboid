/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;

final class PrimitiveArrayListerDouble<BeanT>
extends Lister<BeanT, double[], Double, DoubleArrayPack> {
    private PrimitiveArrayListerDouble() {
    }

    static void register() {
        Lister.primitiveArrayListers.put(Double.TYPE, new PrimitiveArrayListerDouble());
    }

    @Override
    public ListIterator<Double> iterator(final double[] objects, XMLSerializer context) {
        return new ListIterator<Double>(){
            int idx = 0;

            @Override
            public boolean hasNext() {
                return this.idx < objects.length;
            }

            @Override
            public Double next() {
                return objects[this.idx++];
            }
        };
    }

    @Override
    public DoubleArrayPack startPacking(BeanT current, Accessor<BeanT, double[]> acc) {
        return new DoubleArrayPack();
    }

    @Override
    public void addToPack(DoubleArrayPack objects, Double o) {
        objects.add(o);
    }

    @Override
    public void endPacking(DoubleArrayPack pack, BeanT bean, Accessor<BeanT, double[]> acc) throws AccessorException {
        acc.set(bean, pack.build());
    }

    @Override
    public void reset(BeanT o, Accessor<BeanT, double[]> acc) throws AccessorException {
        acc.set(o, new double[0]);
    }

    static final class DoubleArrayPack {
        double[] buf = new double[16];
        int size;

        DoubleArrayPack() {
        }

        void add(Double b) {
            if (this.buf.length == this.size) {
                double[] nb = new double[this.buf.length * 2];
                System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
                this.buf = nb;
            }
            if (b != null) {
                this.buf[this.size++] = b;
            }
        }

        double[] build() {
            if (this.buf.length == this.size) {
                return this.buf;
            }
            double[] r = new double[this.size];
            System.arraycopy(this.buf, 0, r, 0, this.size);
            return r;
        }
    }
}


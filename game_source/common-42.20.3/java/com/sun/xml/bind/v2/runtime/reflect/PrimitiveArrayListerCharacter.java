/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;

final class PrimitiveArrayListerCharacter<BeanT>
extends Lister<BeanT, char[], Character, CharacterArrayPack> {
    private PrimitiveArrayListerCharacter() {
    }

    static void register() {
        Lister.primitiveArrayListers.put(Character.TYPE, new PrimitiveArrayListerCharacter());
    }

    @Override
    public ListIterator<Character> iterator(final char[] objects, XMLSerializer context) {
        return new ListIterator<Character>(){
            int idx = 0;

            @Override
            public boolean hasNext() {
                return this.idx < objects.length;
            }

            @Override
            public Character next() {
                return Character.valueOf(objects[this.idx++]);
            }
        };
    }

    @Override
    public CharacterArrayPack startPacking(BeanT current, Accessor<BeanT, char[]> acc) {
        return new CharacterArrayPack();
    }

    @Override
    public void addToPack(CharacterArrayPack objects, Character o) {
        objects.add(o);
    }

    @Override
    public void endPacking(CharacterArrayPack pack, BeanT bean, Accessor<BeanT, char[]> acc) throws AccessorException {
        acc.set(bean, pack.build());
    }

    @Override
    public void reset(BeanT o, Accessor<BeanT, char[]> acc) throws AccessorException {
        acc.set(o, new char[0]);
    }

    static final class CharacterArrayPack {
        char[] buf = new char[16];
        int size;

        CharacterArrayPack() {
        }

        void add(Character b) {
            if (this.buf.length == this.size) {
                char[] nb = new char[this.buf.length * 2];
                System.arraycopy(this.buf, 0, nb, 0, this.buf.length);
                this.buf = nb;
            }
            if (b != null) {
                this.buf[this.size++] = b.charValue();
            }
        }

        char[] build() {
            if (this.buf.length == this.size) {
                return this.buf;
            }
            char[] r = new char[this.size];
            System.arraycopy(this.buf, 0, r, 0, this.size);
            return r;
        }
    }
}


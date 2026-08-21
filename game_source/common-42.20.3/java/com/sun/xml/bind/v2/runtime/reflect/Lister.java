/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.istack.SAXException2;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.AdaptedLister;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.PrimitiveArrayListerBoolean;
import com.sun.xml.bind.v2.runtime.reflect.PrimitiveArrayListerByte;
import com.sun.xml.bind.v2.runtime.reflect.PrimitiveArrayListerCharacter;
import com.sun.xml.bind.v2.runtime.reflect.PrimitiveArrayListerDouble;
import com.sun.xml.bind.v2.runtime.reflect.PrimitiveArrayListerFloat;
import com.sun.xml.bind.v2.runtime.reflect.PrimitiveArrayListerInteger;
import com.sun.xml.bind.v2.runtime.reflect.PrimitiveArrayListerLong;
import com.sun.xml.bind.v2.runtime.reflect.PrimitiveArrayListerShort;
import com.sun.xml.bind.v2.runtime.reflect.Utils;
import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import com.sun.xml.bind.v2.runtime.unmarshaller.Patcher;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;

public abstract class Lister<BeanT, PropT, ItemT, PackT> {
    private static final Map<Class, WeakReference<Lister>> arrayListerCache = Collections.synchronizedMap(new WeakHashMap());
    static final Map<Class, Lister> primitiveArrayListers = new HashMap<Class, Lister>();
    public static final Lister ERROR;
    private static final ListIterator EMPTY_ITERATOR;
    private static final Class[] COLLECTION_IMPL_CLASSES;

    protected Lister() {
    }

    public abstract ListIterator<ItemT> iterator(PropT var1, XMLSerializer var2);

    public abstract PackT startPacking(BeanT var1, Accessor<BeanT, PropT> var2) throws AccessorException;

    public abstract void addToPack(PackT var1, ItemT var2) throws AccessorException;

    public abstract void endPacking(PackT var1, BeanT var2, Accessor<BeanT, PropT> var3) throws AccessorException;

    public abstract void reset(BeanT var1, Accessor<BeanT, PropT> var2) throws AccessorException;

    public static <BeanT, PropT, ItemT, PackT> Lister<BeanT, PropT, ItemT, PackT> create(Type fieldType, ID idness, Adapter<Type, Class> adapter) {
        Lister l;
        Class<Object> itemType;
        Class rawType = (Class)Utils.REFLECTION_NAVIGATOR.erasure(fieldType);
        if (rawType.isArray()) {
            itemType = rawType.getComponentType();
            l = Lister.getArrayLister(itemType);
        } else if (Collection.class.isAssignableFrom(rawType)) {
            Type bt = Utils.REFLECTION_NAVIGATOR.getBaseClass(fieldType, Collection.class);
            itemType = bt instanceof ParameterizedType ? (Class<Object>)Utils.REFLECTION_NAVIGATOR.erasure(((ParameterizedType)bt).getActualTypeArguments()[0]) : Object.class;
            l = new CollectionLister(Lister.getImplClass(rawType));
        } else {
            return null;
        }
        if (idness == ID.IDREF) {
            l = new IDREFS(l, itemType);
        }
        if (adapter != null) {
            l = new AdaptedLister(l, (Class)adapter.adapterType);
        }
        return l;
    }

    private static Class getImplClass(Class<?> fieldType) {
        return ClassFactory.inferImplClass(fieldType, COLLECTION_IMPL_CLASSES);
    }

    private static Lister getArrayLister(Class componentType) {
        ArrayLister l = null;
        if (componentType.isPrimitive()) {
            l = primitiveArrayListers.get(componentType);
        } else {
            WeakReference<Lister> wr = arrayListerCache.get(componentType);
            if (wr != null) {
                l = (Lister)wr.get();
            }
            if (l == null) {
                l = new ArrayLister(componentType);
                arrayListerCache.put(componentType, new WeakReference(l));
            }
        }
        assert (l != null);
        return l;
    }

    public static <A, B, C, D> Lister<A, B, C, D> getErrorInstance() {
        return ERROR;
    }

    static {
        PrimitiveArrayListerBoolean.register();
        PrimitiveArrayListerByte.register();
        PrimitiveArrayListerCharacter.register();
        PrimitiveArrayListerDouble.register();
        PrimitiveArrayListerFloat.register();
        PrimitiveArrayListerInteger.register();
        PrimitiveArrayListerLong.register();
        PrimitiveArrayListerShort.register();
        ERROR = new Lister(){

            public ListIterator iterator(Object o, XMLSerializer context) {
                return EMPTY_ITERATOR;
            }

            public Object startPacking(Object o, Accessor accessor) {
                return null;
            }

            public void addToPack(Object o, Object o1) {
            }

            public void endPacking(Object o, Object o1, Accessor accessor) {
            }

            public void reset(Object o, Accessor accessor) {
            }
        };
        EMPTY_ITERATOR = new ListIterator(){

            @Override
            public boolean hasNext() {
                return false;
            }

            public Object next() {
                throw new IllegalStateException();
            }
        };
        COLLECTION_IMPL_CLASSES = new Class[]{ArrayList.class, LinkedList.class, HashSet.class, TreeSet.class, Stack.class};
    }

    public static final class IDREFSIterator
    implements ListIterator<String> {
        private final ListIterator i;
        private final XMLSerializer context;
        private Object last;

        private IDREFSIterator(ListIterator i, XMLSerializer context) {
            this.i = i;
            this.context = context;
        }

        @Override
        public boolean hasNext() {
            return this.i.hasNext();
        }

        public Object last() {
            return this.last;
        }

        @Override
        public String next() throws SAXException, JAXBException {
            this.last = this.i.next();
            String id = this.context.grammar.getBeanInfo(this.last, true).getId(this.last, this.context);
            if (id == null) {
                this.context.errorMissingId(this.last);
            }
            return id;
        }
    }

    private static final class IDREFS<BeanT, PropT>
    extends Lister<BeanT, PropT, String, Pack> {
        private final Lister<BeanT, PropT, Object, Object> core;
        private final Class itemType;

        public IDREFS(Lister core, Class itemType) {
            this.core = core;
            this.itemType = itemType;
        }

        @Override
        public ListIterator<String> iterator(PropT prop, XMLSerializer context) {
            ListIterator<Object> i = this.core.iterator(prop, context);
            return new IDREFSIterator(i, context);
        }

        @Override
        public Pack startPacking(BeanT bean, Accessor<BeanT, PropT> acc) {
            return new Pack(bean, acc);
        }

        @Override
        public void addToPack(Pack pack, String item) {
            pack.add(item);
        }

        @Override
        public void endPacking(Pack pack, BeanT bean, Accessor<BeanT, PropT> acc) {
        }

        @Override
        public void reset(BeanT bean, Accessor<BeanT, PropT> acc) throws AccessorException {
            this.core.reset(bean, acc);
        }

        private class Pack
        implements Patcher {
            private final BeanT bean;
            private final List<String> idrefs = new ArrayList<String>();
            private final UnmarshallingContext context;
            private final Accessor<BeanT, PropT> acc;
            private final LocatorEx location;

            public Pack(BeanT bean, Accessor<BeanT, PropT> acc) {
                this.bean = bean;
                this.acc = acc;
                this.context = UnmarshallingContext.getInstance();
                this.location = new LocatorEx.Snapshot(this.context.getLocator());
                this.context.addPatcher(this);
            }

            public void add(String item) {
                this.idrefs.add(item);
            }

            @Override
            public void run() throws SAXException {
                try {
                    Object pack = IDREFS.this.core.startPacking(this.bean, this.acc);
                    for (String id : this.idrefs) {
                        Object t;
                        Callable callable = this.context.getObjectFromId(id, IDREFS.this.itemType);
                        try {
                            t = callable != null ? (Object)callable.call() : null;
                        }
                        catch (SAXException e) {
                            throw e;
                        }
                        catch (Exception e) {
                            throw new SAXException2(e);
                        }
                        if (t == null) {
                            this.context.errorUnresolvedIDREF(this.bean, id, this.location);
                            continue;
                        }
                        TODO.prototype();
                        IDREFS.this.core.addToPack(pack, t);
                    }
                    IDREFS.this.core.endPacking(pack, this.bean, this.acc);
                }
                catch (AccessorException e) {
                    this.context.handleError(e);
                }
            }
        }
    }

    public static final class CollectionLister<BeanT, T extends Collection>
    extends Lister<BeanT, T, Object, T> {
        private final Class<? extends T> implClass;

        public CollectionLister(Class<? extends T> implClass) {
            this.implClass = implClass;
        }

        @Override
        public ListIterator iterator(T collection, XMLSerializer context) {
            final Iterator itr = collection.iterator();
            return new ListIterator(){

                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                public Object next() {
                    return itr.next();
                }
            };
        }

        @Override
        public T startPacking(BeanT bean, Accessor<BeanT, T> acc) throws AccessorException {
            Collection collection = (Collection)acc.get(bean);
            if (collection == null) {
                collection = (Collection)ClassFactory.create(this.implClass);
                if (!acc.isAdapted()) {
                    acc.set(bean, collection);
                }
            }
            collection.clear();
            return (T)collection;
        }

        @Override
        public void addToPack(T collection, Object o) {
            collection.add((Object)o);
        }

        @Override
        public void endPacking(T collection, BeanT bean, Accessor<BeanT, T> acc) throws AccessorException {
            block3: {
                try {
                    if (acc.isAdapted()) {
                        acc.set(bean, collection);
                    }
                }
                catch (AccessorException ae) {
                    if (!acc.isAdapted()) break block3;
                    throw ae;
                }
            }
        }

        @Override
        public void reset(BeanT bean, Accessor<BeanT, T> acc) throws AccessorException {
            Collection collection = (Collection)acc.get(bean);
            if (collection == null) {
                return;
            }
            collection.clear();
        }
    }

    public static final class Pack<ItemT>
    extends ArrayList<ItemT> {
        private final Class<ItemT> itemType;

        public Pack(Class<ItemT> itemType) {
            this.itemType = itemType;
        }

        public ItemT[] build() {
            return super.toArray((Object[])Array.newInstance(this.itemType, this.size()));
        }
    }

    private static final class ArrayLister<BeanT, ItemT>
    extends Lister<BeanT, ItemT[], ItemT, Pack<ItemT>> {
        private final Class<ItemT> itemType;

        public ArrayLister(Class<ItemT> itemType) {
            this.itemType = itemType;
        }

        @Override
        public ListIterator<ItemT> iterator(final ItemT[] objects, XMLSerializer context) {
            return new ListIterator<ItemT>(){
                int idx = 0;

                @Override
                public boolean hasNext() {
                    return this.idx < objects.length;
                }

                @Override
                public ItemT next() {
                    return objects[this.idx++];
                }
            };
        }

        @Override
        public Pack startPacking(BeanT current, Accessor<BeanT, ItemT[]> acc) {
            return new Pack<ItemT>(this.itemType);
        }

        @Override
        public void addToPack(Pack<ItemT> objects, ItemT o) {
            objects.add(o);
        }

        @Override
        public void endPacking(Pack<ItemT> pack, BeanT bean, Accessor<BeanT, ItemT[]> acc) throws AccessorException {
            acc.set(bean, pack.build());
        }

        @Override
        public void reset(BeanT o, Accessor<BeanT, ItemT[]> acc) throws AccessorException {
            acc.set(o, (Object[])Array.newInstance(this.itemType, 0));
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Coordinator;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.xml.sax.SAXException;

final class AdaptedLister<BeanT, PropT, InMemItemT, OnWireItemT, PackT>
extends Lister<BeanT, PropT, OnWireItemT, PackT> {
    private final Lister<BeanT, PropT, InMemItemT, PackT> core;
    private final Class<? extends XmlAdapter<OnWireItemT, InMemItemT>> adapter;

    AdaptedLister(Lister<BeanT, PropT, InMemItemT, PackT> core, Class<? extends XmlAdapter<OnWireItemT, InMemItemT>> adapter) {
        this.core = core;
        this.adapter = adapter;
    }

    private XmlAdapter<OnWireItemT, InMemItemT> getAdapter() {
        return Coordinator._getInstance().getAdapter(this.adapter);
    }

    @Override
    public ListIterator<OnWireItemT> iterator(PropT prop, XMLSerializer context) {
        return new ListIteratorImpl(this.core.iterator(prop, context), context);
    }

    @Override
    public PackT startPacking(BeanT bean, Accessor<BeanT, PropT> accessor) throws AccessorException {
        return this.core.startPacking(bean, accessor);
    }

    @Override
    public void addToPack(PackT pack, OnWireItemT item) throws AccessorException {
        InMemItemT r;
        try {
            r = this.getAdapter().unmarshal(item);
        }
        catch (Exception e) {
            throw new AccessorException(e);
        }
        this.core.addToPack(pack, r);
    }

    @Override
    public void endPacking(PackT pack, BeanT bean, Accessor<BeanT, PropT> accessor) throws AccessorException {
        this.core.endPacking(pack, bean, accessor);
    }

    @Override
    public void reset(BeanT bean, Accessor<BeanT, PropT> accessor) throws AccessorException {
        this.core.reset(bean, accessor);
    }

    private final class ListIteratorImpl
    implements ListIterator<OnWireItemT> {
        private final ListIterator<InMemItemT> core;
        private final XMLSerializer serializer;

        public ListIteratorImpl(ListIterator<InMemItemT> core, XMLSerializer serializer) {
            this.core = core;
            this.serializer = serializer;
        }

        @Override
        public boolean hasNext() {
            return this.core.hasNext();
        }

        @Override
        public OnWireItemT next() throws SAXException, JAXBException {
            Object next = this.core.next();
            try {
                return AdaptedLister.this.getAdapter().marshal(next);
            }
            catch (Exception e) {
                this.serializer.reportError(null, e);
                return null;
            }
        }
    }
}


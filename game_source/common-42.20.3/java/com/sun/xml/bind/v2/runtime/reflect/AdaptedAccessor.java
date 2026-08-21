/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.runtime.Coordinator;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import javax.xml.bind.annotation.adapters.XmlAdapter;

final class AdaptedAccessor<BeanT, InMemValueT, OnWireValueT>
extends Accessor<BeanT, OnWireValueT> {
    private final Accessor<BeanT, InMemValueT> core;
    private final Class<? extends XmlAdapter<OnWireValueT, InMemValueT>> adapter;
    private XmlAdapter<OnWireValueT, InMemValueT> staticAdapter;

    AdaptedAccessor(Class<OnWireValueT> targetType, Accessor<BeanT, InMemValueT> extThis, Class<? extends XmlAdapter<OnWireValueT, InMemValueT>> adapter) {
        super(targetType);
        this.core = extThis;
        this.adapter = adapter;
    }

    @Override
    public boolean isAdapted() {
        return true;
    }

    @Override
    public OnWireValueT get(BeanT bean) throws AccessorException {
        InMemValueT v = this.core.get(bean);
        XmlAdapter<OnWireValueT, InMemValueT> a = this.getAdapter();
        try {
            return a.marshal(v);
        }
        catch (Exception e) {
            throw new AccessorException(e);
        }
    }

    @Override
    public void set(BeanT bean, OnWireValueT o) throws AccessorException {
        XmlAdapter<OnWireValueT, InMemValueT> a = this.getAdapter();
        try {
            this.core.set(bean, o == null ? null : (Object)a.unmarshal(o));
        }
        catch (Exception e) {
            throw new AccessorException(e);
        }
    }

    @Override
    public Object getUnadapted(BeanT bean) throws AccessorException {
        return this.core.getUnadapted(bean);
    }

    @Override
    public void setUnadapted(BeanT bean, Object value) throws AccessorException {
        this.core.setUnadapted(bean, value);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private XmlAdapter<OnWireValueT, InMemValueT> getAdapter() {
        Coordinator coordinator = Coordinator._getInstance();
        if (coordinator != null) {
            return coordinator.getAdapter(this.adapter);
        }
        AdaptedAccessor adaptedAccessor = this;
        synchronized (adaptedAccessor) {
            if (this.staticAdapter == null) {
                this.staticAdapter = ClassFactory.create(this.adapter);
            }
        }
        return this.staticAdapter;
    }
}


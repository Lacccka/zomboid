/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.property.PropertyImpl;
import com.sun.xml.bind.v2.runtime.property.Utils;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.Lister;

abstract class ArrayProperty<BeanT, ListT, ItemT>
extends PropertyImpl<BeanT> {
    protected final Accessor<BeanT, ListT> acc;
    protected final Lister<BeanT, ListT, ItemT, Object> lister;

    protected ArrayProperty(JAXBContextImpl context, RuntimePropertyInfo prop) {
        super(context, prop);
        assert (prop.isCollection());
        this.lister = Lister.create(Utils.REFLECTION_NAVIGATOR.erasure(prop.getRawType()), prop.id(), prop.getAdapter());
        assert (this.lister != null);
        this.acc = prop.getAccessor().optimize(context);
        assert (this.acc != null);
    }

    @Override
    public void reset(BeanT o) throws AccessorException {
        this.lister.reset(o, this.acc);
    }

    @Override
    public final String getIdValue(BeanT bean) {
        return null;
    }
}


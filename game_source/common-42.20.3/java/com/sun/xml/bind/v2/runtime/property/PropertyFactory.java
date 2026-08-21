/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.property.ArrayElementLeafProperty;
import com.sun.xml.bind.v2.runtime.property.ArrayElementNodeProperty;
import com.sun.xml.bind.v2.runtime.property.ArrayReferenceNodeProperty;
import com.sun.xml.bind.v2.runtime.property.AttributeProperty;
import com.sun.xml.bind.v2.runtime.property.ListElementProperty;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.property.SingleElementLeafProperty;
import com.sun.xml.bind.v2.runtime.property.SingleElementNodeProperty;
import com.sun.xml.bind.v2.runtime.property.SingleMapNodeProperty;
import com.sun.xml.bind.v2.runtime.property.SingleReferenceNodeProperty;
import com.sun.xml.bind.v2.runtime.property.ValueProperty;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public abstract class PropertyFactory {
    private static final Constructor<? extends Property>[] propImpls;

    private PropertyFactory() {
    }

    public static Property create(JAXBContextImpl grammar, RuntimePropertyInfo info) {
        PropertyKind kind = info.kind();
        switch (kind) {
            case ATTRIBUTE: {
                return new AttributeProperty(grammar, (RuntimeAttributePropertyInfo)info);
            }
            case VALUE: {
                return new ValueProperty(grammar, (RuntimeValuePropertyInfo)info);
            }
            case ELEMENT: {
                if (!((RuntimeElementPropertyInfo)info).isValueList()) break;
                return new ListElementProperty(grammar, (RuntimeElementPropertyInfo)info);
            }
            case REFERENCE: 
            case MAP: {
                break;
            }
            default: {
                assert (false);
                break;
            }
        }
        boolean isCollection = info.isCollection();
        boolean isLeaf = PropertyFactory.isLeaf(info);
        Constructor<? extends Property> c = propImpls[(isLeaf ? 0 : 6) + (isCollection ? 3 : 0) + kind.propertyIndex];
        try {
            return c.newInstance(grammar, info);
        }
        catch (InstantiationException e) {
            throw new InstantiationError(e.getMessage());
        }
        catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof Error) {
                throw (Error)t;
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            }
            throw new AssertionError((Object)t);
        }
    }

    static boolean isLeaf(RuntimePropertyInfo info) {
        Collection<? extends RuntimeTypeInfo> types = info.ref();
        if (types.size() != 1) {
            return false;
        }
        RuntimeTypeInfo rti = types.iterator().next();
        if (!(rti instanceof RuntimeNonElement)) {
            return false;
        }
        if (info.id() == ID.IDREF) {
            return true;
        }
        if (((RuntimeNonElement)rti).getTransducer() == null) {
            return false;
        }
        return info.getIndividualType().equals(rti.getType());
    }

    static {
        Class[] implClasses = new Class[]{SingleElementLeafProperty.class, null, null, ArrayElementLeafProperty.class, null, null, SingleElementNodeProperty.class, SingleReferenceNodeProperty.class, SingleMapNodeProperty.class, ArrayElementNodeProperty.class, ArrayReferenceNodeProperty.class, null};
        propImpls = new Constructor[implClasses.length];
        for (int i = 0; i < propImpls.length; ++i) {
            if (implClasses[i] == null) continue;
            PropertyFactory.propImpls[i] = implClasses[i].getConstructors()[0];
        }
    }
}


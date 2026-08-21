/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.bind.Util;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.LifecycleMethods;
import com.sun.xml.bind.v2.runtime.Messages;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public abstract class JaxBeanInfo<BeanT> {
    protected boolean isNilIncluded = false;
    protected short flag;
    private static final short FLAG_IS_ELEMENT = 1;
    private static final short FLAG_IS_IMMUTABLE = 2;
    private static final short FLAG_HAS_ELEMENT_ONLY_CONTENTMODEL = 4;
    private static final short FLAG_HAS_BEFORE_UNMARSHAL_METHOD = 8;
    private static final short FLAG_HAS_AFTER_UNMARSHAL_METHOD = 16;
    private static final short FLAG_HAS_BEFORE_MARSHAL_METHOD = 32;
    private static final short FLAG_HAS_AFTER_MARSHAL_METHOD = 64;
    private static final short FLAG_HAS_LIFECYCLE_EVENTS = 128;
    private LifecycleMethods lcm = null;
    public final Class<BeanT> jaxbType;
    private final Object typeName;
    private static final Class[] unmarshalEventParams = new Class[]{Unmarshaller.class, Object.class};
    private static Class[] marshalEventParams = new Class[]{Marshaller.class};
    private static final Logger logger = Util.getClassLogger();

    protected JaxBeanInfo(JAXBContextImpl grammar, RuntimeTypeInfo rti, Class<BeanT> jaxbType, QName[] typeNames, boolean isElement, boolean isImmutable, boolean hasLifecycleEvents) {
        this(grammar, rti, jaxbType, (Object)typeNames, isElement, isImmutable, hasLifecycleEvents);
    }

    protected JaxBeanInfo(JAXBContextImpl grammar, RuntimeTypeInfo rti, Class<BeanT> jaxbType, QName typeName, boolean isElement, boolean isImmutable, boolean hasLifecycleEvents) {
        this(grammar, rti, jaxbType, (Object)typeName, isElement, isImmutable, hasLifecycleEvents);
    }

    protected JaxBeanInfo(JAXBContextImpl grammar, RuntimeTypeInfo rti, Class<BeanT> jaxbType, boolean isElement, boolean isImmutable, boolean hasLifecycleEvents) {
        this(grammar, rti, jaxbType, (Object)null, isElement, isImmutable, hasLifecycleEvents);
    }

    private JaxBeanInfo(JAXBContextImpl grammar, RuntimeTypeInfo rti, Class<BeanT> jaxbType, Object typeName, boolean isElement, boolean isImmutable, boolean hasLifecycleEvents) {
        grammar.beanInfos.put(rti, this);
        this.jaxbType = jaxbType;
        this.typeName = typeName;
        this.flag = (short)((isElement ? 1 : 0) | (isImmutable ? 2 : 0) | (hasLifecycleEvents ? 128 : 0));
    }

    public final boolean hasBeforeUnmarshalMethod() {
        return (this.flag & 8) != 0;
    }

    public final boolean hasAfterUnmarshalMethod() {
        return (this.flag & 0x10) != 0;
    }

    public final boolean hasBeforeMarshalMethod() {
        return (this.flag & 0x20) != 0;
    }

    public final boolean hasAfterMarshalMethod() {
        return (this.flag & 0x40) != 0;
    }

    public final boolean isElement() {
        return (this.flag & 1) != 0;
    }

    public final boolean isImmutable() {
        return (this.flag & 2) != 0;
    }

    public final boolean hasElementOnlyContentModel() {
        return (this.flag & 4) != 0;
    }

    protected final void hasElementOnlyContentModel(boolean value) {
        this.flag = value ? (short)(this.flag | 4) : (short)(this.flag & 0xFFFFFFFB);
    }

    public boolean isNilIncluded() {
        return this.isNilIncluded;
    }

    public boolean lookForLifecycleMethods() {
        return (this.flag & 0x80) != 0;
    }

    public abstract String getElementNamespaceURI(BeanT var1);

    public abstract String getElementLocalName(BeanT var1);

    public Collection<QName> getTypeNames() {
        if (this.typeName == null) {
            return Collections.emptyList();
        }
        if (this.typeName instanceof QName) {
            return Collections.singletonList((QName)this.typeName);
        }
        return Arrays.asList((QName[])this.typeName);
    }

    public QName getTypeName(@NotNull BeanT instance) {
        if (this.typeName == null) {
            return null;
        }
        if (this.typeName instanceof QName) {
            return (QName)this.typeName;
        }
        return ((QName[])this.typeName)[0];
    }

    public abstract BeanT createInstance(UnmarshallingContext var1) throws IllegalAccessException, InvocationTargetException, InstantiationException, SAXException;

    public abstract boolean reset(BeanT var1, UnmarshallingContext var2) throws SAXException;

    public abstract String getId(BeanT var1, XMLSerializer var2) throws SAXException;

    public abstract void serializeBody(BeanT var1, XMLSerializer var2) throws SAXException, IOException, XMLStreamException;

    public abstract void serializeAttributes(BeanT var1, XMLSerializer var2) throws SAXException, IOException, XMLStreamException;

    public abstract void serializeRoot(BeanT var1, XMLSerializer var2) throws SAXException, IOException, XMLStreamException;

    public abstract void serializeURIs(BeanT var1, XMLSerializer var2) throws SAXException;

    public abstract Loader getLoader(JAXBContextImpl var1, boolean var2);

    public abstract Transducer<BeanT> getTransducer();

    protected void link(JAXBContextImpl grammar) {
    }

    public void wrapUp() {
    }

    private Method[] getDeclaredMethods(final Class<BeanT> c) {
        return AccessController.doPrivileged(new PrivilegedAction<Method[]>(){

            @Override
            public Method[] run() {
                return c.getDeclaredMethods();
            }
        });
    }

    protected final void setLifecycleFlags() {
        try {
            Class<BeanT> jt = this.jaxbType;
            if (this.lcm == null) {
                this.lcm = new LifecycleMethods();
            }
            while (jt != null) {
                for (Method m : this.getDeclaredMethods(jt)) {
                    String name = m.getName();
                    if (this.lcm.beforeUnmarshal == null && name.equals("beforeUnmarshal") && this.match(m, unmarshalEventParams)) {
                        this.cacheLifecycleMethod(m, (short)8);
                    }
                    if (this.lcm.afterUnmarshal == null && name.equals("afterUnmarshal") && this.match(m, unmarshalEventParams)) {
                        this.cacheLifecycleMethod(m, (short)16);
                    }
                    if (this.lcm.beforeMarshal == null && name.equals("beforeMarshal") && this.match(m, marshalEventParams)) {
                        this.cacheLifecycleMethod(m, (short)32);
                    }
                    if (this.lcm.afterMarshal != null || !name.equals("afterMarshal") || !this.match(m, marshalEventParams)) continue;
                    this.cacheLifecycleMethod(m, (short)64);
                }
                jt = jt.getSuperclass();
            }
        }
        catch (SecurityException e) {
            logger.log(Level.WARNING, Messages.UNABLE_TO_DISCOVER_EVENTHANDLER.format(this.jaxbType.getName(), e), e);
        }
    }

    private boolean match(Method m, Class[] params) {
        return Arrays.equals(m.getParameterTypes(), params);
    }

    private void cacheLifecycleMethod(Method m, short lifecycleFlag) {
        if (this.lcm == null) {
            this.lcm = new LifecycleMethods();
        }
        m.setAccessible(true);
        this.flag = (short)(this.flag | lifecycleFlag);
        switch (lifecycleFlag) {
            case 8: {
                this.lcm.beforeUnmarshal = m;
                break;
            }
            case 16: {
                this.lcm.afterUnmarshal = m;
                break;
            }
            case 32: {
                this.lcm.beforeMarshal = m;
                break;
            }
            case 64: {
                this.lcm.afterMarshal = m;
            }
        }
    }

    public final LifecycleMethods getLifecycleMethods() {
        return this.lcm;
    }

    public final void invokeBeforeUnmarshalMethod(UnmarshallerImpl unm, Object child, Object parent) throws SAXException {
        Method m = this.getLifecycleMethods().beforeUnmarshal;
        this.invokeUnmarshallCallback(m, child, unm, parent);
    }

    public final void invokeAfterUnmarshalMethod(UnmarshallerImpl unm, Object child, Object parent) throws SAXException {
        Method m = this.getLifecycleMethods().afterUnmarshal;
        this.invokeUnmarshallCallback(m, child, unm, parent);
    }

    private void invokeUnmarshallCallback(Method m, Object child, UnmarshallerImpl unm, Object parent) throws SAXException {
        try {
            m.invoke(child, unm, parent);
        }
        catch (IllegalAccessException e) {
            UnmarshallingContext.getInstance().handleError(e, false);
        }
        catch (InvocationTargetException e) {
            UnmarshallingContext.getInstance().handleError(e, false);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.Utils;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.property.PropertyFactory;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Discarder;
import com.sun.xml.bind.v2.runtime.unmarshaller.Intercepter;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.util.QNameMap;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class ElementBeanInfoImpl
extends JaxBeanInfo<JAXBElement> {
    private Loader loader;
    private final Property property;
    private final QName tagName;
    public final Class expectedType;
    private final Class scope;
    private final Constructor<? extends JAXBElement> constructor;

    ElementBeanInfoImpl(JAXBContextImpl grammar, RuntimeElementInfo rei) {
        super(grammar, rei, rei.getType(), true, false, true);
        this.property = PropertyFactory.create(grammar, rei.getProperty());
        this.tagName = rei.getElementName();
        this.expectedType = (Class)Utils.REFLECTION_NAVIGATOR.erasure((Type)rei.getContentInMemoryType());
        this.scope = rei.getScope() == null ? JAXBElement.GlobalScope.class : (Class)rei.getScope().getClazz();
        Class<? extends JAXBElement> type = Utils.REFLECTION_NAVIGATOR.erasure(rei.getType());
        if (type == JAXBElement.class) {
            this.constructor = null;
        } else {
            try {
                this.constructor = type.getConstructor(this.expectedType);
            }
            catch (NoSuchMethodException e) {
                NoSuchMethodError x = new NoSuchMethodError("Failed to find the constructor for " + type + " with " + this.expectedType);
                x.initCause(e);
                throw x;
            }
        }
    }

    protected ElementBeanInfoImpl(final JAXBContextImpl grammar) {
        super(grammar, null, JAXBElement.class, true, false, true);
        this.tagName = null;
        this.expectedType = null;
        this.scope = null;
        this.constructor = null;
        this.property = new Property<JAXBElement>(){

            @Override
            public void reset(JAXBElement o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void serializeBody(JAXBElement e, XMLSerializer target, Object outerPeer) throws SAXException, IOException, XMLStreamException {
                QName n;
                ElementBeanInfoImpl bi;
                Class scope = e.getScope();
                if (e.isGlobalScope()) {
                    scope = null;
                }
                if ((bi = grammar.getElement(scope, n = e.getName())) == null) {
                    JaxBeanInfo tbi;
                    try {
                        tbi = grammar.getBeanInfo(e.getDeclaredType(), true);
                    }
                    catch (JAXBException x) {
                        target.reportError(null, x);
                        return;
                    }
                    Object value = e.getValue();
                    target.startElement(n.getNamespaceURI(), n.getLocalPart(), n.getPrefix(), null);
                    if (value == null) {
                        target.writeXsiNilTrue();
                    } else {
                        target.childAsXsiType(value, "value", tbi, false);
                    }
                    target.endElement();
                } else {
                    try {
                        bi.property.serializeBody(e, target, e);
                    }
                    catch (AccessorException x) {
                        target.reportError(null, x);
                    }
                }
            }

            @Override
            public void serializeURIs(JAXBElement o, XMLSerializer target) {
            }

            @Override
            public boolean hasSerializeURIAction() {
                return false;
            }

            @Override
            public String getIdValue(JAXBElement o) {
                return null;
            }

            @Override
            public PropertyKind getKind() {
                return PropertyKind.ELEMENT;
            }

            @Override
            public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers) {
            }

            @Override
            public Accessor getElementPropertyAccessor(String nsUri, String localName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void wrapUp() {
            }

            @Override
            public RuntimePropertyInfo getInfo() {
                return ElementBeanInfoImpl.this.property.getInfo();
            }

            @Override
            public boolean isHiddenByOverride() {
                return false;
            }

            @Override
            public void setHiddenByOverride(boolean hidden) {
                throw new UnsupportedOperationException("Not supported on jaxbelements.");
            }

            @Override
            public String getFieldName() {
                return null;
            }
        };
    }

    @Override
    public String getElementNamespaceURI(JAXBElement e) {
        return e.getName().getNamespaceURI();
    }

    @Override
    public String getElementLocalName(JAXBElement e) {
        return e.getName().getLocalPart();
    }

    @Override
    public Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable) {
        if (this.loader == null) {
            UnmarshallerChain c = new UnmarshallerChain(context);
            QNameMap<ChildLoader> result = new QNameMap<ChildLoader>();
            this.property.buildChildElementUnmarshallers(c, result);
            this.loader = result.size() == 1 ? new IntercepterLoader(result.getOne().getValue().loader) : Discarder.INSTANCE;
        }
        return this.loader;
    }

    @Override
    public final JAXBElement createInstance(UnmarshallingContext context) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return this.createInstanceFromValue(null);
    }

    public final JAXBElement createInstanceFromValue(Object o) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (this.constructor == null) {
            return new JAXBElement<Object>(this.tagName, this.expectedType, this.scope, o);
        }
        return this.constructor.newInstance(o);
    }

    @Override
    public boolean reset(JAXBElement e, UnmarshallingContext context) {
        e.setValue(null);
        return true;
    }

    @Override
    public String getId(JAXBElement e, XMLSerializer target) {
        Object o = e.getValue();
        if (o instanceof String) {
            return (String)o;
        }
        return null;
    }

    @Override
    public void serializeBody(JAXBElement element, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        try {
            this.property.serializeBody(element, target, null);
        }
        catch (AccessorException x) {
            target.reportError(null, x);
        }
    }

    @Override
    public void serializeRoot(JAXBElement e, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        this.serializeBody(e, target);
    }

    @Override
    public void serializeAttributes(JAXBElement e, XMLSerializer target) {
    }

    @Override
    public void serializeURIs(JAXBElement e, XMLSerializer target) {
    }

    @Override
    public final Transducer<JAXBElement> getTransducer() {
        return null;
    }

    @Override
    public void wrapUp() {
        super.wrapUp();
        this.property.wrapUp();
    }

    @Override
    public void link(JAXBContextImpl grammar) {
        super.link(grammar);
        this.getLoader(grammar, true);
    }

    private final class IntercepterLoader
    extends Loader
    implements Intercepter {
        private final Loader core;

        public IntercepterLoader(Loader core) {
            this.core = core;
        }

        @Override
        public final void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            state.setLoader(this.core);
            state.setIntercepter(this);
            UnmarshallingContext context = state.getContext();
            Object child = context.getOuterPeer();
            if (child != null && ElementBeanInfoImpl.this.jaxbType != child.getClass()) {
                child = null;
            }
            if (child != null) {
                ElementBeanInfoImpl.this.reset((JAXBElement)child, context);
            }
            if (child == null) {
                child = context.createInstance(ElementBeanInfoImpl.this);
            }
            this.fireBeforeUnmarshal(ElementBeanInfoImpl.this, child, state);
            context.recordOuterPeer(child);
            UnmarshallingContext.State p = state.getPrev();
            p.setBackup(p.getTarget());
            p.setTarget(child);
            this.core.startElement(state, ea);
        }

        @Override
        public Object intercept(UnmarshallingContext.State state, Object o) throws SAXException {
            JAXBElement e = (JAXBElement)state.getTarget();
            state.setTarget(state.getBackup());
            state.setBackup(null);
            if (state.isNil()) {
                e.setNil(true);
                state.setNil(false);
            }
            if (o != null) {
                e.setValue(o);
            }
            this.fireAfterUnmarshal(ElementBeanInfoImpl.this, e, state);
            return e;
        }
    }
}


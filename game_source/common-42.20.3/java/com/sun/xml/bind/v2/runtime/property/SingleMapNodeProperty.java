/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeMapPropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.PropertyImpl;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.property.Utils;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.util.QNameMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class SingleMapNodeProperty<BeanT, ValueT extends Map>
extends PropertyImpl<BeanT> {
    private final Accessor<BeanT, ValueT> acc;
    private final Name tagName;
    private final Name entryTag;
    private final Name keyTag;
    private final Name valueTag;
    private final boolean nillable;
    private JaxBeanInfo keyBeanInfo;
    private JaxBeanInfo valueBeanInfo;
    private final Class<? extends ValueT> mapImplClass;
    private static final Class[] knownImplClasses = new Class[]{HashMap.class, TreeMap.class, LinkedHashMap.class};
    private Loader keyLoader;
    private Loader valueLoader;
    private final Loader itemsLoader = new Loader(false){
        private ThreadLocal<Stack<BeanT>> target;
        private ThreadLocal<Stack<ValueT>> map;
        {
            this.target = new ThreadLocal();
            this.map = new ThreadLocal();
        }

        @Override
        public void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            try {
                Object target = state.getPrev().getTarget();
                Map mapValue = (Map)SingleMapNodeProperty.this.acc.get(target);
                if (mapValue == null) {
                    mapValue = (Map)ClassFactory.create(SingleMapNodeProperty.this.mapImplClass);
                } else {
                    mapValue.clear();
                }
                Stack.push(this.target, target);
                Stack.push(this.map, mapValue);
                state.setTarget(mapValue);
            }
            catch (AccessorException e) {
                1.handleGenericException(e, true);
                state.setTarget(new HashMap());
            }
        }

        @Override
        public void leaveElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            super.leaveElement(state, ea);
            try {
                SingleMapNodeProperty.this.acc.set(Stack.pop(this.target), Stack.pop(this.map));
            }
            catch (AccessorException ex) {
                1.handleGenericException(ex, true);
            }
        }

        @Override
        public void childElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            if (ea.matches(SingleMapNodeProperty.this.entryTag)) {
                state.setLoader(SingleMapNodeProperty.this.entryLoader);
            } else {
                super.childElement(state, ea);
            }
        }

        @Override
        public Collection<QName> getExpectedChildElements() {
            return Collections.singleton(SingleMapNodeProperty.this.entryTag.toQName());
        }
    };
    private final Loader entryLoader = new Loader(false){

        @Override
        public void startElement(UnmarshallingContext.State state, TagName ea) {
            state.setTarget(new Object[2]);
        }

        @Override
        public void leaveElement(UnmarshallingContext.State state, TagName ea) {
            Object[] keyValue = (Object[])state.getTarget();
            Map map = (Map)state.getPrev().getTarget();
            map.put(keyValue[0], keyValue[1]);
        }

        @Override
        public void childElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            if (ea.matches(SingleMapNodeProperty.this.keyTag)) {
                state.setLoader(SingleMapNodeProperty.this.keyLoader);
                state.setReceiver(keyReceiver);
                return;
            }
            if (ea.matches(SingleMapNodeProperty.this.valueTag)) {
                state.setLoader(SingleMapNodeProperty.this.valueLoader);
                state.setReceiver(valueReceiver);
                return;
            }
            super.childElement(state, ea);
        }

        @Override
        public Collection<QName> getExpectedChildElements() {
            return Arrays.asList(SingleMapNodeProperty.this.keyTag.toQName(), SingleMapNodeProperty.this.valueTag.toQName());
        }
    };
    private static final Receiver keyReceiver = new ReceiverImpl(0);
    private static final Receiver valueReceiver = new ReceiverImpl(1);

    public SingleMapNodeProperty(JAXBContextImpl context, RuntimeMapPropertyInfo prop) {
        super(context, prop);
        this.acc = prop.getAccessor().optimize(context);
        this.tagName = context.nameBuilder.createElementName(prop.getXmlName());
        this.entryTag = context.nameBuilder.createElementName("", "entry");
        this.keyTag = context.nameBuilder.createElementName("", "key");
        this.valueTag = context.nameBuilder.createElementName("", "value");
        this.nillable = prop.isCollectionNillable();
        this.keyBeanInfo = context.getOrCreate(prop.getKeyType());
        this.valueBeanInfo = context.getOrCreate(prop.getValueType());
        Class sig = (Class)Utils.REFLECTION_NAVIGATOR.erasure(prop.getRawType());
        this.mapImplClass = ClassFactory.inferImplClass(sig, knownImplClasses);
    }

    @Override
    public void reset(BeanT bean) throws AccessorException {
        this.acc.set(bean, null);
    }

    @Override
    public String getIdValue(BeanT bean) {
        return null;
    }

    @Override
    public PropertyKind getKind() {
        return PropertyKind.MAP;
    }

    @Override
    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers) {
        this.keyLoader = this.keyBeanInfo.getLoader(chain.context, true);
        this.valueLoader = this.valueBeanInfo.getLoader(chain.context, true);
        handlers.put(this.tagName, new ChildLoader(this.itemsLoader, null));
    }

    @Override
    public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        Map v = (Map)this.acc.get(o);
        if (v != null) {
            this.bareStartTag(w, this.tagName, v);
            for (Map.Entry e : v.entrySet()) {
                Object value;
                this.bareStartTag(w, this.entryTag, null);
                Object key = e.getKey();
                if (key != null) {
                    w.startElement(this.keyTag, key);
                    w.childAsXsiType(key, this.fieldName, this.keyBeanInfo, false);
                    w.endElement();
                }
                if ((value = e.getValue()) != null) {
                    w.startElement(this.valueTag, value);
                    w.childAsXsiType(value, this.fieldName, this.valueBeanInfo, false);
                    w.endElement();
                }
                w.endElement();
            }
            w.endElement();
        } else if (this.nillable) {
            w.startElement(this.tagName, null);
            w.writeXsiNilTrue();
            w.endElement();
        }
    }

    private void bareStartTag(XMLSerializer w, Name tagName, Object peer) throws IOException, XMLStreamException, SAXException {
        w.startElement(tagName, peer);
        w.endNamespaceDecls(peer);
        w.endAttributes();
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if (this.tagName.equals(nsUri, localName)) {
            return this.acc;
        }
        return null;
    }

    private static final class Stack<T> {
        private Stack<T> parent;
        private T value;

        private Stack(Stack<T> parent, T value) {
            this.parent = parent;
            this.value = value;
        }

        private Stack(T value) {
            this.value = value;
        }

        private static <T> void push(ThreadLocal<Stack<T>> holder, T value) {
            Stack<T> parent = holder.get();
            if (parent == null) {
                holder.set(new Stack<T>(value));
            } else {
                holder.set(new Stack<T>(parent, value));
            }
        }

        private static <T> T pop(ThreadLocal<Stack<T>> holder) {
            Stack<T> current = holder.get();
            if (current.parent == null) {
                holder.remove();
            } else {
                holder.set(current.parent);
            }
            return current.value;
        }
    }

    private static final class ReceiverImpl
    implements Receiver {
        private final int index;

        public ReceiverImpl(int index) {
            this.index = index;
        }

        @Override
        public void receive(UnmarshallingContext.State state, Object o) {
            ((Object[])state.getTarget())[this.index] = o;
        }
    }
}


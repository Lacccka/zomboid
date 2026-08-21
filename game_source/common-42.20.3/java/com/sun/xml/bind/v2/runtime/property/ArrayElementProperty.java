/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.RuntimeUtil;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.ArrayERProperty;
import com.sun.xml.bind.v2.runtime.property.PropertyFactory;
import com.sun.xml.bind.v2.runtime.property.TagAndType;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;
import com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.DefaultValueLoaderDecorator;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TextLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader;
import com.sun.xml.bind.v2.util.QNameMap;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

abstract class ArrayElementProperty<BeanT, ListT, ItemT>
extends ArrayERProperty<BeanT, ListT, ItemT> {
    private final Map<Class, TagAndType> typeMap = new HashMap<Class, TagAndType>();
    private Map<TypeRef<Type, Class>, JaxBeanInfo> refs = new HashMap<TypeRef<Type, Class>, JaxBeanInfo>();
    protected RuntimeElementPropertyInfo prop;
    private final Name nillableTagName;

    protected ArrayElementProperty(JAXBContextImpl grammar, RuntimeElementPropertyInfo prop) {
        super(grammar, prop, prop.getXmlName(), prop.isCollectionNillable());
        this.prop = prop;
        List<? extends RuntimeTypeRef> types = prop.getTypes();
        Name n = null;
        for (RuntimeTypeRef runtimeTypeRef : types) {
            Class type = (Class)runtimeTypeRef.getTarget().getType();
            if (type.isPrimitive()) {
                type = RuntimeUtil.primitiveToBox.get(type);
            }
            JaxBeanInfo beanInfo = grammar.getOrCreate(runtimeTypeRef.getTarget());
            TagAndType tt = new TagAndType(grammar.nameBuilder.createElementName(runtimeTypeRef.getTagName()), beanInfo);
            this.typeMap.put(type, tt);
            this.refs.put(runtimeTypeRef, beanInfo);
            if (!runtimeTypeRef.isNillable() || n != null) continue;
            n = tt.tagName;
        }
        this.nillableTagName = n;
    }

    @Override
    public void wrapUp() {
        super.wrapUp();
        this.refs = null;
        this.prop = null;
    }

    @Override
    protected void serializeListBody(BeanT beanT, XMLSerializer w, ListT list) throws IOException, XMLStreamException, SAXException, AccessorException {
        ListIterator itr = this.lister.iterator(list, w);
        boolean isIdref = itr instanceof Lister.IDREFSIterator;
        while (itr.hasNext()) {
            try {
                Object item = itr.next();
                if (item != null) {
                    Class<?> itemType = item.getClass();
                    if (isIdref) {
                        itemType = ((Lister.IDREFSIterator)itr).last().getClass();
                    }
                    TagAndType tt = this.typeMap.get(itemType);
                    while (tt == null && itemType != null) {
                        itemType = itemType.getSuperclass();
                        tt = this.typeMap.get(itemType);
                    }
                    if (tt == null) {
                        w.startElement(this.typeMap.values().iterator().next().tagName, null);
                        w.childAsXsiType(item, this.fieldName, w.grammar.getBeanInfo(Object.class), false);
                    } else {
                        w.startElement(tt.tagName, null);
                        this.serializeItem(tt.beanInfo, item, w);
                    }
                    w.endElement();
                    continue;
                }
                if (this.nillableTagName == null) continue;
                w.startElement(this.nillableTagName, null);
                w.writeXsiNilTrue();
                w.endElement();
            }
            catch (JAXBException e) {
                w.reportError(this.fieldName, e);
            }
        }
    }

    protected abstract void serializeItem(JaxBeanInfo var1, ItemT var2, XMLSerializer var3) throws SAXException, AccessorException, IOException, XMLStreamException;

    @Override
    public void createBodyUnmarshaller(UnmarshallerChain chain, QNameMap<ChildLoader> loaders) {
        int offset = chain.allocateOffset();
        ArrayERProperty.ReceiverImpl recv = new ArrayERProperty.ReceiverImpl(offset);
        for (RuntimeTypeRef runtimeTypeRef : this.prop.getTypes()) {
            Name tagName = chain.context.nameBuilder.createElementName(runtimeTypeRef.getTagName());
            Loader item = this.createItemUnmarshaller(chain, runtimeTypeRef);
            if (runtimeTypeRef.isNillable() || chain.context.allNillable) {
                item = new XsiNilLoader.Array(item);
            }
            if (runtimeTypeRef.getDefaultValue() != null) {
                item = new DefaultValueLoaderDecorator(item, runtimeTypeRef.getDefaultValue());
            }
            loaders.put(tagName, new ChildLoader(item, recv));
        }
    }

    @Override
    public final PropertyKind getKind() {
        return PropertyKind.ELEMENT;
    }

    private Loader createItemUnmarshaller(UnmarshallerChain chain, RuntimeTypeRef typeRef) {
        if (PropertyFactory.isLeaf(typeRef.getSource())) {
            Transducer xducer = typeRef.getTransducer();
            return new TextLoader(xducer);
        }
        return this.refs.get(typeRef).getLoader(chain.context, true);
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if (this.wrapperTagName != null) {
            if (this.wrapperTagName.equals(nsUri, localName)) {
                return this.acc;
            }
        } else {
            for (TagAndType tt : this.typeMap.values()) {
                if (!tt.tagName.equals(nsUri, localName)) continue;
                return new NullSafeAccessor(this.acc, this.lister);
            }
        }
        return null;
    }
}


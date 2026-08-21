/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.Util;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.AttributeAccessor;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Messages;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.Utils;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.AttributeProperty;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.property.PropertyFactory;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.StructureLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiTypeLoader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

public final class ClassBeanInfoImpl<BeanT>
extends JaxBeanInfo<BeanT>
implements AttributeAccessor<BeanT> {
    public final Property<BeanT>[] properties;
    private Property<? super BeanT> idProperty;
    private Loader loader;
    private Loader loaderWithTypeSubst;
    private RuntimeClassInfo ci;
    private final Accessor<? super BeanT, Map<QName, String>> inheritedAttWildcard;
    private final Transducer<BeanT> xducer;
    public final ClassBeanInfoImpl<? super BeanT> superClazz;
    private final Accessor<? super BeanT, Locator> xmlLocatorField;
    private final Name tagName;
    private boolean retainPropertyInfo = false;
    private AttributeProperty<BeanT>[] attributeProperties;
    private Property<BeanT>[] uriProperties;
    private final Method factoryMethod;
    private static final AttributeProperty[] EMPTY_PROPERTIES = new AttributeProperty[0];
    private static final Logger logger = Util.getClassLogger();

    ClassBeanInfoImpl(JAXBContextImpl owner, RuntimeClassInfo ci) {
        super(owner, (RuntimeTypeInfo)ci, (Class)ci.getClazz(), ci.getTypeName(), ci.isElement(), false, true);
        int classMod;
        this.ci = ci;
        this.inheritedAttWildcard = ci.getAttributeWildcard();
        this.xducer = ci.getTransducer();
        this.factoryMethod = ci.getFactoryMethod();
        this.retainPropertyInfo = owner.retainPropertyInfo;
        if (!(this.factoryMethod == null || Modifier.isPublic(classMod = this.factoryMethod.getDeclaringClass().getModifiers()) && Modifier.isPublic(this.factoryMethod.getModifiers()))) {
            try {
                this.factoryMethod.setAccessible(true);
            }
            catch (SecurityException e) {
                logger.log(Level.FINE, "Unable to make the method of " + this.factoryMethod + " accessible", e);
                throw e;
            }
        }
        this.superClazz = ci.getBaseClass() == null ? null : owner.getOrCreate(ci.getBaseClass());
        this.xmlLocatorField = this.superClazz != null && this.superClazz.xmlLocatorField != null ? this.superClazz.xmlLocatorField : ci.getLocatorField();
        List<? extends RuntimePropertyInfo> ps = ci.getProperties();
        this.properties = new Property[ps.size()];
        int idx = 0;
        boolean elementOnly = true;
        for (RuntimePropertyInfo runtimePropertyInfo : ps) {
            Property p = PropertyFactory.create(owner, runtimePropertyInfo);
            if (runtimePropertyInfo.id() == ID.ID) {
                this.idProperty = p;
            }
            this.properties[idx++] = p;
            elementOnly &= runtimePropertyInfo.elementOnlyContent();
            this.checkOverrideProperties(p);
        }
        this.hasElementOnlyContentModel(elementOnly);
        this.tagName = ci.isElement() ? owner.nameBuilder.createElementName(ci.getElementName()) : null;
        this.setLifecycleFlags();
    }

    private void checkOverrideProperties(Property p) {
        Property<BeanT>[] props;
        ClassBeanInfoImpl<? super BeanT> bi = this;
        while ((bi = bi.superClazz) != null && (props = bi.properties) != null) {
            for (Property<BeanT> superProperty : props) {
                String spName;
                if (superProperty == null || (spName = superProperty.getFieldName()) == null || !spName.equals(p.getFieldName())) continue;
                superProperty.setHiddenByOverride(true);
            }
        }
    }

    @Override
    protected void link(JAXBContextImpl grammar) {
        if (this.uriProperties != null) {
            return;
        }
        super.link(grammar);
        if (this.superClazz != null) {
            this.superClazz.link(grammar);
        }
        this.getLoader(grammar, true);
        if (this.superClazz != null) {
            if (this.idProperty == null) {
                this.idProperty = this.superClazz.idProperty;
            }
            if (!this.superClazz.hasElementOnlyContentModel()) {
                this.hasElementOnlyContentModel(false);
            }
        }
        FinalArrayList<AttributeProperty> attProps = new FinalArrayList<AttributeProperty>();
        FinalArrayList<Property> uriProps = new FinalArrayList<Property>();
        ClassBeanInfoImpl<BeanT> bi = this;
        while (bi != null) {
            for (int i = 0; i < bi.properties.length; ++i) {
                Property<BeanT> p = bi.properties[i];
                if (p instanceof AttributeProperty) {
                    attProps.add((AttributeProperty)p);
                }
                if (!p.hasSerializeURIAction()) continue;
                uriProps.add(p);
            }
            bi = bi.superClazz;
        }
        if (grammar.c14nSupport) {
            Collections.sort(attProps);
        }
        this.attributeProperties = attProps.isEmpty() ? EMPTY_PROPERTIES : attProps.toArray(new AttributeProperty[attProps.size()]);
        this.uriProperties = uriProps.isEmpty() ? EMPTY_PROPERTIES : uriProps.toArray(new Property[uriProps.size()]);
    }

    @Override
    public void wrapUp() {
        for (Property<BeanT> p : this.properties) {
            p.wrapUp();
        }
        this.ci = null;
        super.wrapUp();
    }

    @Override
    public String getElementNamespaceURI(BeanT bean) {
        return this.tagName.nsUri;
    }

    @Override
    public String getElementLocalName(BeanT bean) {
        return this.tagName.localName;
    }

    @Override
    public BeanT createInstance(UnmarshallingContext context) throws IllegalAccessException, InvocationTargetException, InstantiationException, SAXException {
        Object bean = null;
        if (this.factoryMethod == null) {
            bean = ClassFactory.create0(this.jaxbType);
        } else {
            Object o = ClassFactory.create(this.factoryMethod);
            if (this.jaxbType.isInstance(o)) {
                bean = o;
            } else {
                throw new InstantiationException("The factory method didn't return a correct object");
            }
        }
        if (this.xmlLocatorField != null) {
            try {
                this.xmlLocatorField.set(bean, new LocatorImpl(context.getLocator()));
            }
            catch (AccessorException e) {
                context.handleError(e);
            }
        }
        return (BeanT)bean;
    }

    @Override
    public boolean reset(BeanT bean, UnmarshallingContext context) throws SAXException {
        try {
            if (this.superClazz != null) {
                this.superClazz.reset(bean, context);
            }
            for (Property<BeanT> p : this.properties) {
                p.reset(bean);
            }
            return true;
        }
        catch (AccessorException e) {
            context.handleError(e);
            return false;
        }
    }

    @Override
    public String getId(BeanT bean, XMLSerializer target) throws SAXException {
        if (this.idProperty != null) {
            try {
                return this.idProperty.getIdValue(bean);
            }
            catch (AccessorException e) {
                target.reportError(null, e);
            }
        }
        return null;
    }

    @Override
    public void serializeRoot(BeanT bean, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        if (this.tagName == null) {
            Class<?> beanClass = bean.getClass();
            String message = beanClass.isAnnotationPresent(XmlRootElement.class) ? Messages.UNABLE_TO_MARSHAL_UNBOUND_CLASS.format(beanClass.getName()) : Messages.UNABLE_TO_MARSHAL_NON_ELEMENT.format(beanClass.getName());
            target.reportError(new ValidationEventImpl(1, message, null, null));
        } else {
            target.startElement(this.tagName, bean);
            target.childAsSoleContent(bean, null);
            target.endElement();
            if (this.retainPropertyInfo) {
                target.currentProperty.remove();
            }
        }
    }

    @Override
    public void serializeBody(BeanT bean, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        if (this.superClazz != null) {
            this.superClazz.serializeBody(bean, target);
        }
        try {
            for (Property<BeanT> p : this.properties) {
                Class<?> beanClass;
                boolean isThereAnOverridingProperty;
                if (this.retainPropertyInfo) {
                    target.currentProperty.set(p);
                }
                if (!(isThereAnOverridingProperty = p.isHiddenByOverride()) || bean.getClass().equals(this.jaxbType)) {
                    p.serializeBody(bean, target, null);
                    continue;
                }
                if (!isThereAnOverridingProperty || Utils.REFLECTION_NAVIGATOR.getDeclaredField(beanClass = bean.getClass(), p.getFieldName()) != null) continue;
                p.serializeBody(bean, target, null);
            }
        }
        catch (AccessorException e) {
            target.reportError(null, e);
        }
    }

    @Override
    public void serializeAttributes(BeanT bean, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        for (AttributeProperty<BeanT> p : this.attributeProperties) {
            try {
                if (this.retainPropertyInfo) {
                    Property parentProperty = target.getCurrentProperty();
                    target.currentProperty.set(p);
                    p.serializeAttributes(bean, target);
                    target.currentProperty.set(parentProperty);
                } else {
                    p.serializeAttributes(bean, target);
                }
                if (!p.attName.equals("http://www.w3.org/2001/XMLSchema-instance", "nil")) continue;
                this.isNilIncluded = true;
            }
            catch (AccessorException e) {
                target.reportError(null, e);
            }
        }
        try {
            if (this.inheritedAttWildcard != null) {
                Map<QName, String> map = this.inheritedAttWildcard.get(bean);
                target.attWildcardAsAttributes(map, null);
            }
        }
        catch (AccessorException e) {
            target.reportError(null, e);
        }
    }

    @Override
    public void serializeURIs(BeanT bean, XMLSerializer target) throws SAXException {
        try {
            if (this.retainPropertyInfo) {
                Property<BeanT>[] parentProperty = target.getCurrentProperty();
                for (Property<BeanT> p : this.uriProperties) {
                    target.currentProperty.set(p);
                    p.serializeURIs(bean, target);
                }
                target.currentProperty.set((Property)parentProperty);
            } else {
                for (Property<BeanT> p : this.uriProperties) {
                    p.serializeURIs(bean, target);
                }
            }
            if (this.inheritedAttWildcard != null) {
                Map<QName, String> map = this.inheritedAttWildcard.get(bean);
                target.attWildcardAsURIs(map, null);
            }
        }
        catch (AccessorException e) {
            target.reportError(null, e);
        }
    }

    @Override
    public Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable) {
        if (this.loader == null) {
            StructureLoader sl = new StructureLoader(this);
            this.loader = sl;
            this.loaderWithTypeSubst = this.ci.hasSubClasses() ? new XsiTypeLoader(this) : this.loader;
            sl.init(context, this, this.ci.getAttributeWildcard());
        }
        if (typeSubstitutionCapable) {
            return this.loaderWithTypeSubst;
        }
        return this.loader;
    }

    @Override
    public Transducer<BeanT> getTransducer() {
        return this.xducer;
    }
}


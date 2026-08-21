/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.istack.Nullable;
import com.sun.xml.bind.Util;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.reflect.AdaptedAccessor;
import com.sun.xml.bind.v2.runtime.reflect.Messages;
import com.sun.xml.bind.v2.runtime.reflect.Utils;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.awt.Image;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import org.xml.sax.SAXException;

public abstract class Accessor<BeanT, ValueT>
implements Receiver {
    public final Class<ValueT> valueType;
    private static List<Class> nonAbstractableClasses = Arrays.asList(Object.class, Calendar.class, Duration.class, XMLGregorianCalendar.class, Image.class, DataHandler.class, Source.class, Date.class, File.class, URI.class, URL.class, Class.class, String.class, Source.class);
    private static boolean accessWarned = false;
    private static final Accessor ERROR = new Accessor<Object, Object>(Object.class){

        @Override
        public Object get(Object o) {
            return null;
        }

        @Override
        public void set(Object o, Object o1) {
        }
    };
    public static final Accessor<JAXBElement, Object> JAXB_ELEMENT_VALUE = new Accessor<JAXBElement, Object>(Object.class){

        @Override
        public Object get(JAXBElement jaxbElement) {
            return jaxbElement.getValue();
        }

        @Override
        public void set(JAXBElement jaxbElement, Object o) {
            jaxbElement.setValue(o);
        }
    };
    private static final Map<Class, Object> uninitializedValues = new HashMap<Class, Object>();

    public Class<ValueT> getValueType() {
        return this.valueType;
    }

    protected Accessor(Class<ValueT> valueType) {
        this.valueType = valueType;
    }

    public Accessor<BeanT, ValueT> optimize(@Nullable JAXBContextImpl context) {
        return this;
    }

    public abstract ValueT get(BeanT var1) throws AccessorException;

    public abstract void set(BeanT var1, ValueT var2) throws AccessorException;

    public Object getUnadapted(BeanT bean) throws AccessorException {
        return this.get(bean);
    }

    public boolean isAdapted() {
        return false;
    }

    public void setUnadapted(BeanT bean, Object value) throws AccessorException {
        this.set(bean, value);
    }

    @Override
    public void receive(UnmarshallingContext.State state, Object o) throws SAXException {
        try {
            this.set(state.getTarget(), o);
        }
        catch (AccessorException e) {
            Loader.handleGenericException(e, true);
        }
        catch (IllegalAccessError iae) {
            Loader.handleGenericError(iae);
        }
    }

    public boolean isValueTypeAbstractable() {
        return !nonAbstractableClasses.contains(this.getValueType());
    }

    public boolean isAbstractable(Class clazz) {
        return !nonAbstractableClasses.contains(clazz);
    }

    public final <T> Accessor<BeanT, T> adapt(Class<T> targetType, Class<? extends XmlAdapter<T, ValueT>> adapter) {
        return new AdaptedAccessor(targetType, this, adapter);
    }

    public final <T> Accessor<BeanT, T> adapt(Adapter<Type, Class> adapter) {
        return new AdaptedAccessor((Class)Utils.REFLECTION_NAVIGATOR.erasure((Type)adapter.defaultType), this, (Class)adapter.adapterType);
    }

    public static <A, B> Accessor<A, B> getErrorInstance() {
        return ERROR;
    }

    static {
        uninitializedValues.put(Byte.TYPE, (byte)0);
        uninitializedValues.put(Boolean.TYPE, false);
        uninitializedValues.put(Character.TYPE, Character.valueOf('\u0000'));
        uninitializedValues.put(Float.TYPE, Float.valueOf(0.0f));
        uninitializedValues.put(Double.TYPE, 0.0);
        uninitializedValues.put(Integer.TYPE, 0);
        uninitializedValues.put(Long.TYPE, 0L);
        uninitializedValues.put(Short.TYPE, (short)0);
    }

    public static class SetterOnlyReflection<BeanT, ValueT>
    extends GetterSetterReflection<BeanT, ValueT> {
        public SetterOnlyReflection(Method setter) {
            super(null, setter);
        }

        @Override
        public ValueT get(BeanT bean) throws AccessorException {
            throw new AccessorException(Messages.NO_GETTER.format(this.setter.toString()));
        }
    }

    public static class GetterOnlyReflection<BeanT, ValueT>
    extends GetterSetterReflection<BeanT, ValueT> {
        public GetterOnlyReflection(Method getter) {
            super(getter, null);
        }

        @Override
        public void set(BeanT bean, ValueT value) throws AccessorException {
            throw new AccessorException(Messages.NO_SETTER.format(this.getter.toString()));
        }
    }

    public static class GetterSetterReflection<BeanT, ValueT>
    extends Accessor<BeanT, ValueT> {
        public final Method getter;
        public final Method setter;
        private static final Logger logger = Util.getClassLogger();

        public GetterSetterReflection(Method getter, Method setter) {
            super(getter != null ? getter.getReturnType() : setter.getParameterTypes()[0]);
            this.getter = getter;
            this.setter = setter;
            if (getter != null) {
                this.makeAccessible(getter);
            }
            if (setter != null) {
                this.makeAccessible(setter);
            }
        }

        private void makeAccessible(Method m) {
            if (!Modifier.isPublic(m.getModifiers()) || !Modifier.isPublic(m.getDeclaringClass().getModifiers())) {
                try {
                    m.setAccessible(true);
                }
                catch (SecurityException e) {
                    if (!accessWarned) {
                        logger.log(Level.WARNING, Messages.UNABLE_TO_ACCESS_NON_PUBLIC_FIELD.format(m.getDeclaringClass().getName(), m.getName()), e);
                    }
                    accessWarned = true;
                }
            }
        }

        @Override
        public ValueT get(BeanT bean) throws AccessorException {
            try {
                return (ValueT)this.getter.invoke(bean, new Object[0]);
            }
            catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
            catch (InvocationTargetException e) {
                throw this.handleInvocationTargetException(e);
            }
        }

        @Override
        public void set(BeanT bean, ValueT value) throws AccessorException {
            try {
                if (value == null) {
                    value = uninitializedValues.get(this.valueType);
                }
                this.setter.invoke(bean, value);
            }
            catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
            catch (InvocationTargetException e) {
                throw this.handleInvocationTargetException(e);
            }
        }

        private AccessorException handleInvocationTargetException(InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            }
            if (t instanceof Error) {
                throw (Error)t;
            }
            return new AccessorException(t);
        }

        @Override
        public Accessor<BeanT, ValueT> optimize(JAXBContextImpl context) {
            return this;
        }
    }

    public static final class ReadOnlyFieldReflection<BeanT, ValueT>
    extends FieldReflection<BeanT, ValueT> {
        public ReadOnlyFieldReflection(Field f, boolean supressAccessorWarnings) {
            super(f, supressAccessorWarnings);
        }

        public ReadOnlyFieldReflection(Field f) {
            super(f);
        }

        @Override
        public void set(BeanT bean, ValueT value) {
        }

        @Override
        public Accessor<BeanT, ValueT> optimize(JAXBContextImpl context) {
            return this;
        }
    }

    public static class FieldReflection<BeanT, ValueT>
    extends Accessor<BeanT, ValueT> {
        public final Field f;
        private static final Logger logger = Util.getClassLogger();

        public FieldReflection(Field f) {
            this(f, false);
        }

        public FieldReflection(Field f, boolean supressAccessorWarnings) {
            super(f.getType());
            this.f = f;
            int mod = f.getModifiers();
            if (!Modifier.isPublic(mod) || Modifier.isFinal(mod) || !Modifier.isPublic(f.getDeclaringClass().getModifiers())) {
                try {
                    f.setAccessible(true);
                }
                catch (SecurityException e) {
                    if (!accessWarned && !supressAccessorWarnings) {
                        logger.log(Level.WARNING, Messages.UNABLE_TO_ACCESS_NON_PUBLIC_FIELD.format(f.getDeclaringClass().getName(), f.getName()), e);
                    }
                    accessWarned = true;
                }
            }
        }

        @Override
        public ValueT get(BeanT bean) {
            try {
                return (ValueT)this.f.get(bean);
            }
            catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }

        @Override
        public void set(BeanT bean, ValueT value) {
            try {
                if (value == null) {
                    value = uninitializedValues.get(this.valueType);
                }
                this.f.set(bean, value);
            }
            catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }

        @Override
        public Accessor<BeanT, ValueT> optimize(JAXBContextImpl context) {
            return this;
        }
    }
}


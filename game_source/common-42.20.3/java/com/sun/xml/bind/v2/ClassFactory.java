/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2;

import com.sun.xml.bind.Util;
import com.sun.xml.bind.v2.Messages;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ClassFactory {
    private static final Class[] emptyClass = new Class[0];
    private static final Object[] emptyObject = new Object[0];
    private static final Logger logger = Util.getClassLogger();
    private static final ThreadLocal<Map<Class, WeakReference<Constructor>>> tls = new ThreadLocal<Map<Class, WeakReference<Constructor>>>(){

        @Override
        public Map<Class, WeakReference<Constructor>> initialValue() {
            return new WeakHashMap<Class, WeakReference<Constructor>>();
        }
    };

    public static void cleanCache() {
        if (tls != null) {
            try {
                tls.remove();
            }
            catch (Exception e) {
                logger.log(Level.WARNING, "Unable to clean Thread Local cache of classes used in Unmarshaller: {0}", e.getLocalizedMessage());
            }
        }
    }

    public static <T> T create0(final Class<T> clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<Class, WeakReference<Constructor>> m = tls.get();
        Constructor cons = null;
        WeakReference<Constructor> consRef = m.get(clazz);
        if (consRef != null) {
            cons = (Constructor)consRef.get();
        }
        if (cons == null) {
            cons = System.getSecurityManager() == null ? ClassFactory.tryGetDeclaredConstructor(clazz) : (Constructor)AccessController.doPrivileged(new PrivilegedAction<Constructor<T>>(){

                @Override
                public Constructor<T> run() {
                    return ClassFactory.tryGetDeclaredConstructor(clazz);
                }
            });
            int classMod = clazz.getModifiers();
            if (!Modifier.isPublic(classMod) || !Modifier.isPublic(cons.getModifiers())) {
                try {
                    cons.setAccessible(true);
                }
                catch (SecurityException e) {
                    logger.log(Level.FINE, "Unable to make the constructor of " + clazz + " accessible", e);
                    throw e;
                }
            }
            m.put(clazz, new WeakReference<Constructor>(cons));
        }
        return cons.newInstance(emptyObject);
    }

    private static <T> Constructor<T> tryGetDeclaredConstructor(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor(emptyClass);
        }
        catch (NoSuchMethodException e) {
            logger.log(Level.INFO, "No default constructor found on " + clazz, e);
            NoSuchMethodError exp = clazz.getDeclaringClass() != null && !Modifier.isStatic(clazz.getModifiers()) ? new NoSuchMethodError(Messages.NO_DEFAULT_CONSTRUCTOR_IN_INNER_CLASS.format(clazz.getName())) : new NoSuchMethodError(e.getMessage());
            exp.initCause(e);
            throw exp;
        }
    }

    public static <T> T create(Class<T> clazz) {
        try {
            return ClassFactory.create0(clazz);
        }
        catch (InstantiationException e) {
            logger.log(Level.INFO, "failed to create a new instance of " + clazz, e);
            throw new InstantiationError(e.toString());
        }
        catch (IllegalAccessException e) {
            logger.log(Level.INFO, "failed to create a new instance of " + clazz, e);
            throw new IllegalAccessError(e.toString());
        }
        catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof RuntimeException) {
                throw (RuntimeException)target;
            }
            if (target instanceof Error) {
                throw (Error)target;
            }
            throw new IllegalStateException(target);
        }
    }

    public static Object create(Method method) {
        Throwable errorMsg;
        try {
            return method.invoke(null, emptyObject);
        }
        catch (InvocationTargetException ive) {
            Throwable target = ive.getTargetException();
            if (target instanceof RuntimeException) {
                throw (RuntimeException)target;
            }
            if (target instanceof Error) {
                throw (Error)target;
            }
            throw new IllegalStateException(target);
        }
        catch (IllegalAccessException e) {
            logger.log(Level.INFO, "failed to create a new instance of " + method.getReturnType().getName(), e);
            throw new IllegalAccessError(e.toString());
        }
        catch (IllegalArgumentException iae) {
            logger.log(Level.INFO, "failed to create a new instance of " + method.getReturnType().getName(), iae);
            errorMsg = iae;
        }
        catch (NullPointerException npe) {
            logger.log(Level.INFO, "failed to create a new instance of " + method.getReturnType().getName(), npe);
            errorMsg = npe;
        }
        catch (ExceptionInInitializerError eie) {
            logger.log(Level.INFO, "failed to create a new instance of " + method.getReturnType().getName(), eie);
            errorMsg = eie;
        }
        NoSuchMethodError exp = new NoSuchMethodError(errorMsg.getMessage());
        exp.initCause(errorMsg);
        throw exp;
    }

    public static <T> Class<? extends T> inferImplClass(Class<T> fieldType, Class[] knownImplClasses) {
        if (!fieldType.isInterface()) {
            return fieldType;
        }
        for (Class impl : knownImplClasses) {
            if (!fieldType.isAssignableFrom(impl)) continue;
            return impl.asSubclass(fieldType);
        }
        return null;
    }
}


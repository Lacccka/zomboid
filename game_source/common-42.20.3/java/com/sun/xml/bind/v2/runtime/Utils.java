/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.v2.model.nav.Navigator;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

final class Utils {
    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());
    static final Navigator<Type, Class, Field, Method> REFLECTION_NAVIGATOR;

    private Utils() {
    }

    static {
        try {
            final Class<?> refNav = Class.forName("com.sun.xml.bind.v2.model.nav.ReflectionNavigator");
            Method getInstance = AccessController.doPrivileged(new PrivilegedAction<Method>(){

                @Override
                public Method run() {
                    try {
                        Method getInstance = refNav.getDeclaredMethod("getInstance", new Class[0]);
                        getInstance.setAccessible(true);
                        return getInstance;
                    }
                    catch (NoSuchMethodException e) {
                        throw new IllegalStateException("ReflectionNavigator.getInstance can't be found");
                    }
                }
            });
            REFLECTION_NAVIGATOR = (Navigator)getInstance.invoke(null, new Object[0]);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException("Can't find ReflectionNavigator class");
        }
        catch (InvocationTargetException e) {
            throw new IllegalStateException("ReflectionNavigator.getInstance throws the exception");
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException("ReflectionNavigator.getInstance method is inaccessible");
        }
        catch (SecurityException e) {
            LOGGER.log(Level.FINE, "Unable to access ReflectionNavigator.getInstance", e);
            throw e;
        }
    }
}


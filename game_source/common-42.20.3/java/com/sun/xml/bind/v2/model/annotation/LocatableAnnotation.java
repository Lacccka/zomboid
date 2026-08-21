/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Init;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.Quick;
import com.sun.xml.bind.v2.model.annotation.SecureLoader;
import com.sun.xml.bind.v2.runtime.Location;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class LocatableAnnotation
implements InvocationHandler,
Locatable,
Location {
    private final Annotation core;
    private final Locatable upstream;
    private static final Map<Class, Quick> quicks = new HashMap<Class, Quick>();

    public static <A extends Annotation> A create(A annotation, Locatable parentSourcePos) {
        if (annotation == null) {
            return null;
        }
        Class<? extends Annotation> type = annotation.annotationType();
        if (quicks.containsKey(type)) {
            return (A)quicks.get(type).newInstance(parentSourcePos, annotation);
        }
        ClassLoader cl = SecureLoader.getClassClassLoader(LocatableAnnotation.class);
        try {
            Class<?> loadableT = Class.forName(type.getName(), false, cl);
            if (loadableT != type) {
                return annotation;
            }
            return (A)((Annotation)Proxy.newProxyInstance(cl, new Class[]{type, Locatable.class}, new LocatableAnnotation(annotation, parentSourcePos)));
        }
        catch (ClassNotFoundException e) {
            return annotation;
        }
        catch (IllegalArgumentException e) {
            return annotation;
        }
    }

    LocatableAnnotation(Annotation core, Locatable upstream) {
        this.core = core;
        this.upstream = upstream;
    }

    @Override
    public Locatable getUpstream() {
        return this.upstream;
    }

    @Override
    public Location getLocation() {
        return this;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args2) throws Throwable {
        try {
            if (method.getDeclaringClass() == Locatable.class) {
                return method.invoke((Object)this, args2);
            }
            if (Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException();
            }
            return method.invoke((Object)this.core, args2);
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() != null) {
                throw e.getTargetException();
            }
            throw e;
        }
    }

    @Override
    public String toString() {
        return this.core.toString();
    }

    static {
        for (Quick q : Init.getAll()) {
            quicks.put(q.annotationType(), q);
        }
    }
}


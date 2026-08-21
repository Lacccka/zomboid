/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.nav;

import com.sun.xml.bind.v2.model.nav.GenericArrayTypeImpl;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.nav.ParameterizedTypeImpl;
import com.sun.xml.bind.v2.model.nav.SecureLoader;
import com.sun.xml.bind.v2.model.nav.TypeVisitor;
import com.sun.xml.bind.v2.model.nav.WildcardTypeImpl;
import com.sun.xml.bind.v2.runtime.Location;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;

final class ReflectionNavigator
implements Navigator<Type, Class, Field, Method> {
    private static final ReflectionNavigator INSTANCE = new ReflectionNavigator();
    private static final TypeVisitor<Type, Class> baseClassFinder = new TypeVisitor<Type, Class>(){

        @Override
        public Type onClass(Class c, Class sup) {
            Type r;
            if (sup == c) {
                return sup;
            }
            Type sc = c.getGenericSuperclass();
            if (sc != null && (r = (Type)this.visit(sc, sup)) != null) {
                return r;
            }
            for (Type i : c.getGenericInterfaces()) {
                r = (Type)this.visit(i, sup);
                if (r == null) continue;
                return r;
            }
            return null;
        }

        @Override
        public Type onParameterizdType(ParameterizedType p, Class sup) {
            Class raw = (Class)p.getRawType();
            if (raw == sup) {
                return p;
            }
            Type r = raw.getGenericSuperclass();
            if (r != null) {
                r = (Type)this.visit(this.bind(r, raw, p), sup);
            }
            if (r != null) {
                return r;
            }
            for (Type i : raw.getGenericInterfaces()) {
                r = (Type)this.visit(this.bind(i, raw, p), sup);
                if (r == null) continue;
                return r;
            }
            return null;
        }

        @Override
        public Type onGenericArray(GenericArrayType g, Class sup) {
            return null;
        }

        @Override
        public Type onVariable(TypeVariable v, Class sup) {
            return (Type)this.visit(v.getBounds()[0], sup);
        }

        @Override
        public Type onWildcard(WildcardType w, Class sup) {
            return null;
        }

        private Type bind(Type t, GenericDeclaration decl, ParameterizedType args2) {
            return (Type)binder.visit(t, new BinderArg(decl, args2.getActualTypeArguments()));
        }
    };
    private static final TypeVisitor<Type, BinderArg> binder = new TypeVisitor<Type, BinderArg>(){

        @Override
        public Type onClass(Class c, BinderArg args2) {
            return c;
        }

        @Override
        public Type onParameterizdType(ParameterizedType p, BinderArg args2) {
            Type[] params = p.getActualTypeArguments();
            boolean different = false;
            for (int i = 0; i < params.length; ++i) {
                Type t = params[i];
                params[i] = (Type)this.visit(t, args2);
                different |= t != params[i];
            }
            Type newOwner = p.getOwnerType();
            if (newOwner != null) {
                newOwner = (Type)this.visit(newOwner, args2);
            }
            if (!(different |= p.getOwnerType() != newOwner)) {
                return p;
            }
            return new ParameterizedTypeImpl((Class)p.getRawType(), params, newOwner);
        }

        @Override
        public Type onGenericArray(GenericArrayType g, BinderArg types) {
            Type c = (Type)this.visit(g.getGenericComponentType(), types);
            if (c == g.getGenericComponentType()) {
                return g;
            }
            return new GenericArrayTypeImpl(c);
        }

        @Override
        public Type onVariable(TypeVariable v, BinderArg types) {
            return types.replace(v);
        }

        @Override
        public Type onWildcard(WildcardType w, BinderArg types) {
            Type t;
            int i;
            Type[] lb = w.getLowerBounds();
            Type[] ub = w.getUpperBounds();
            boolean diff = false;
            for (i = 0; i < lb.length; ++i) {
                t = lb[i];
                lb[i] = (Type)this.visit(t, types);
                diff |= t != lb[i];
            }
            for (i = 0; i < ub.length; ++i) {
                t = ub[i];
                ub[i] = (Type)this.visit(t, types);
                diff |= t != ub[i];
            }
            if (!diff) {
                return w;
            }
            return new WildcardTypeImpl(lb, ub);
        }
    };
    private static final TypeVisitor<Class, Void> eraser = new TypeVisitor<Class, Void>(){

        @Override
        public Class onClass(Class c, Void v) {
            return c;
        }

        @Override
        public Class onParameterizdType(ParameterizedType p, Void v) {
            return (Class)this.visit(p.getRawType(), null);
        }

        @Override
        public Class onGenericArray(GenericArrayType g, Void v) {
            return Array.newInstance((Class)this.visit(g.getGenericComponentType(), null), 0).getClass();
        }

        @Override
        public Class onVariable(TypeVariable tv, Void v) {
            return (Class)this.visit(tv.getBounds()[0], null);
        }

        @Override
        public Class onWildcard(WildcardType w, Void v) {
            return (Class)this.visit(w.getUpperBounds()[0], null);
        }
    };

    static ReflectionNavigator getInstance() {
        return INSTANCE;
    }

    private ReflectionNavigator() {
    }

    @Override
    public Class getSuperClass(Class clazz) {
        if (clazz == Object.class) {
            return null;
        }
        Class<Object> sc = clazz.getSuperclass();
        if (sc == null) {
            sc = Object.class;
        }
        return sc;
    }

    @Override
    public Type getBaseClass(Type t, Class sup) {
        return baseClassFinder.visit(t, sup);
    }

    @Override
    public String getClassName(Class clazz) {
        return clazz.getName();
    }

    @Override
    public String getTypeName(Type type) {
        if (type instanceof Class) {
            Class c = (Class)type;
            if (c.isArray()) {
                return this.getTypeName(c.getComponentType()) + "[]";
            }
            return c.getName();
        }
        return type.toString();
    }

    @Override
    public String getClassShortName(Class clazz) {
        return clazz.getSimpleName();
    }

    @Override
    public Collection<? extends Field> getDeclaredFields(final Class clazz) {
        Field[] fields = AccessController.doPrivileged(new PrivilegedAction<Field[]>(){

            @Override
            public Field[] run() {
                return clazz.getDeclaredFields();
            }
        });
        return Arrays.asList(fields);
    }

    @Override
    public Field getDeclaredField(final Class clazz, final String fieldName) {
        return AccessController.doPrivileged(new PrivilegedAction<Field>(){

            @Override
            public Field run() {
                try {
                    return clazz.getDeclaredField(fieldName);
                }
                catch (NoSuchFieldException e) {
                    return null;
                }
            }
        });
    }

    @Override
    public Collection<? extends Method> getDeclaredMethods(final Class clazz) {
        Method[] methods = AccessController.doPrivileged(new PrivilegedAction<Method[]>(){

            @Override
            public Method[] run() {
                return clazz.getDeclaredMethods();
            }
        });
        return Arrays.asList(methods);
    }

    @Override
    public Class getDeclaringClassForField(Field field) {
        return field.getDeclaringClass();
    }

    @Override
    public Class getDeclaringClassForMethod(Method method) {
        return method.getDeclaringClass();
    }

    @Override
    public Type getFieldType(Field field) {
        Class<?> c;
        if (field.getType().isArray() && (c = field.getType().getComponentType()).isPrimitive()) {
            return Array.newInstance(c, 0).getClass();
        }
        return this.fix(field.getGenericType());
    }

    @Override
    public String getFieldName(Field field) {
        return field.getName();
    }

    @Override
    public String getMethodName(Method method) {
        return method.getName();
    }

    @Override
    public Type getReturnType(Method method) {
        return this.fix(method.getGenericReturnType());
    }

    public Type[] getMethodParameters(Method method) {
        return method.getGenericParameterTypes();
    }

    @Override
    public boolean isStaticMethod(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    @Override
    public boolean isFinalMethod(Method method) {
        return Modifier.isFinal(method.getModifiers());
    }

    @Override
    public boolean isSubClassOf(Type sub, Type sup) {
        return this.erasure(sup).isAssignableFrom(this.erasure(sub));
    }

    @Override
    public Class ref(Class c) {
        return c;
    }

    @Override
    public Class use(Class c) {
        return c;
    }

    @Override
    public Class asDecl(Type t) {
        return this.erasure(t);
    }

    @Override
    public Class asDecl(Class c) {
        return c;
    }

    @Override
    public <T> Class<T> erasure(Type t) {
        return eraser.visit(t, null);
    }

    @Override
    public boolean isAbstract(Class clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    @Override
    public boolean isFinal(Class clazz) {
        return Modifier.isFinal(clazz.getModifiers());
    }

    public Type createParameterizedType(Class rawType, Type ... arguments) {
        return new ParameterizedTypeImpl(rawType, arguments, null);
    }

    @Override
    public boolean isArray(Type t) {
        if (t instanceof Class) {
            Class c = (Class)t;
            return c.isArray();
        }
        return t instanceof GenericArrayType;
    }

    @Override
    public boolean isArrayButNotByteArray(Type t) {
        if (t instanceof Class) {
            Class c = (Class)t;
            return c.isArray() && c != byte[].class;
        }
        if (t instanceof GenericArrayType) {
            return (t = ((GenericArrayType)t).getGenericComponentType()) != Byte.TYPE;
        }
        return false;
    }

    @Override
    public Type getComponentType(Type t) {
        if (t instanceof Class) {
            Class c = (Class)t;
            return c.getComponentType();
        }
        if (t instanceof GenericArrayType) {
            return ((GenericArrayType)t).getGenericComponentType();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Type getTypeArgument(Type type, int i) {
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType)type;
            return this.fix(p.getActualTypeArguments()[i]);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean isParameterizedType(Type type) {
        return type instanceof ParameterizedType;
    }

    @Override
    public boolean isPrimitive(Type type) {
        if (type instanceof Class) {
            Class c = (Class)type;
            return c.isPrimitive();
        }
        return false;
    }

    @Override
    public Type getPrimitive(Class primitiveType) {
        assert (primitiveType.isPrimitive());
        return primitiveType;
    }

    @Override
    public Location getClassLocation(final Class clazz) {
        return new Location(){

            @Override
            public String toString() {
                return clazz.getName();
            }
        };
    }

    @Override
    public Location getFieldLocation(final Field field) {
        return new Location(){

            @Override
            public String toString() {
                return field.toString();
            }
        };
    }

    @Override
    public Location getMethodLocation(final Method method) {
        return new Location(){

            @Override
            public String toString() {
                return method.toString();
            }
        };
    }

    @Override
    public boolean hasDefaultConstructor(Class c) {
        try {
            c.getDeclaredConstructor(new Class[0]);
            return true;
        }
        catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public boolean isStaticField(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    @Override
    public boolean isPublicMethod(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }

    @Override
    public boolean isPublicField(Field field) {
        return Modifier.isPublic(field.getModifiers());
    }

    @Override
    public boolean isEnum(Class c) {
        return Enum.class.isAssignableFrom(c);
    }

    public Field[] getEnumConstants(Class clazz) {
        try {
            T[] values2 = clazz.getEnumConstants();
            Field[] fields = new Field[values2.length];
            for (int i = 0; i < values2.length; ++i) {
                fields[i] = clazz.getField(((Enum)values2[i]).name());
            }
            return fields;
        }
        catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    @Override
    public Type getVoidType() {
        return Void.class;
    }

    @Override
    public String getPackageName(Class clazz) {
        String name = clazz.getName();
        int idx = name.lastIndexOf(46);
        if (idx < 0) {
            return "";
        }
        return name.substring(0, idx);
    }

    @Override
    public Class loadObjectFactory(Class referencePoint, String pkg) {
        ClassLoader cl = SecureLoader.getClassClassLoader(referencePoint);
        if (cl == null) {
            cl = SecureLoader.getSystemClassLoader();
        }
        try {
            return cl.loadClass(pkg + ".ObjectFactory");
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public boolean isBridgeMethod(Method method) {
        return method.isBridge();
    }

    @Override
    public boolean isOverriding(Method method, final Class base) {
        final String name = method.getName();
        final Class[] params = method.getParameterTypes();
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                for (Class clazz = base; clazz != null; clazz = clazz.getSuperclass()) {
                    try {
                        Method m = clazz.getDeclaredMethod(name, params);
                        if (m == null) continue;
                        return Boolean.TRUE;
                    }
                    catch (NoSuchMethodException noSuchMethodException) {
                        // empty catch block
                    }
                }
                return Boolean.FALSE;
            }
        });
    }

    @Override
    public boolean isInterface(Class clazz) {
        return clazz.isInterface();
    }

    @Override
    public boolean isTransient(Field f) {
        return Modifier.isTransient(f.getModifiers());
    }

    @Override
    public boolean isInnerClass(Class clazz) {
        return clazz.getEnclosingClass() != null && !Modifier.isStatic(clazz.getModifiers());
    }

    @Override
    public boolean isSameType(Type t1, Type t2) {
        return t1.equals(t2);
    }

    private Type fix(Type t) {
        if (!(t instanceof GenericArrayType)) {
            return t;
        }
        GenericArrayType gat = (GenericArrayType)t;
        if (gat.getGenericComponentType() instanceof Class) {
            Class c = (Class)gat.getGenericComponentType();
            return Array.newInstance(c, 0).getClass();
        }
        return t;
    }

    private static class BinderArg {
        final TypeVariable[] params;
        final Type[] args;

        BinderArg(TypeVariable[] params, Type[] args2) {
            this.params = params;
            this.args = args2;
            assert (params.length == args2.length);
        }

        public BinderArg(GenericDeclaration decl, Type[] args2) {
            this(decl.getTypeParameters(), args2);
        }

        Type replace(TypeVariable v) {
            for (int i = 0; i < this.params.length; ++i) {
                if (!this.params[i].equals(v)) continue;
                return this.args[i];
            }
            return v;
        }
    }
}


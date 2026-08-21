/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.nav;

import com.sun.xml.bind.v2.runtime.Location;
import java.util.Collection;

public interface Navigator<T, C, F, M> {
    public C getSuperClass(C var1);

    public T getBaseClass(T var1, C var2);

    public String getClassName(C var1);

    public String getTypeName(T var1);

    public String getClassShortName(C var1);

    public Collection<? extends F> getDeclaredFields(C var1);

    public F getDeclaredField(C var1, String var2);

    public Collection<? extends M> getDeclaredMethods(C var1);

    public C getDeclaringClassForField(F var1);

    public C getDeclaringClassForMethod(M var1);

    public T getFieldType(F var1);

    public String getFieldName(F var1);

    public String getMethodName(M var1);

    public T getReturnType(M var1);

    public T[] getMethodParameters(M var1);

    public boolean isStaticMethod(M var1);

    public boolean isSubClassOf(T var1, T var2);

    public T ref(Class var1);

    public T use(C var1);

    public C asDecl(T var1);

    public C asDecl(Class var1);

    public boolean isArray(T var1);

    public boolean isArrayButNotByteArray(T var1);

    public T getComponentType(T var1);

    public T getTypeArgument(T var1, int var2);

    public boolean isParameterizedType(T var1);

    public boolean isPrimitive(T var1);

    public T getPrimitive(Class var1);

    public Location getClassLocation(C var1);

    public Location getFieldLocation(F var1);

    public Location getMethodLocation(M var1);

    public boolean hasDefaultConstructor(C var1);

    public boolean isStaticField(F var1);

    public boolean isPublicMethod(M var1);

    public boolean isFinalMethod(M var1);

    public boolean isPublicField(F var1);

    public boolean isEnum(C var1);

    public <P> T erasure(T var1);

    public boolean isAbstract(C var1);

    public boolean isFinal(C var1);

    public F[] getEnumConstants(C var1);

    public T getVoidType();

    public String getPackageName(C var1);

    public C loadObjectFactory(C var1, String var2);

    public boolean isBridgeMethod(M var1);

    public boolean isOverriding(M var1, C var2);

    public boolean isInterface(C var1);

    public boolean isTransient(F var1);

    public boolean isInnerClass(C var1);

    public boolean isSameType(T var1, T var2);
}


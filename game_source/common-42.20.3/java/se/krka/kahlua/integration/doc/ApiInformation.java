/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.doc;

import java.util.List;
import se.krka.kahlua.integration.expose.MethodDebugInformation;

public interface ApiInformation {
    public List<Class<?>> getAllClasses();

    public List<Class<?>> getRootClasses();

    public List<Class<?>> getChildrenForClass(Class<?> var1);

    public List<MethodDebugInformation> getMethodsForClass(Class<?> var1);

    public List<MethodDebugInformation> getFunctionsForClass(Class<?> var1);
}


/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.doc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import se.krka.kahlua.integration.doc.ApiInformation;
import se.krka.kahlua.integration.expose.ClassDebugInformation;
import se.krka.kahlua.integration.expose.MethodDebugInformation;

public class ApiDocumentationExporter
implements ApiInformation {
    private final Map<Class<?>, ClassDebugInformation> classes;
    private final Map<Class<?>, List<Class<?>>> classHierarchy = new HashMap();
    private final List<Class<?>> rootClasses = new ArrayList();
    private final List<Class<?>> allClasses = new ArrayList();
    private final Comparator<Class<?>> classSorter = new Comparator<Class<?>>(this){
        {
            Objects.requireNonNull(this$0);
        }

        @Override
        public int compare(Class<?> arg0, Class<?> arg1) {
            int c = arg0.getSimpleName().compareTo(arg1.getSimpleName());
            if (c != 0) {
                return c;
            }
            return arg0.getName().compareTo(arg1.getName());
        }
    };
    private final Comparator<MethodDebugInformation> methodSorter = new Comparator<MethodDebugInformation>(this){
        {
            Objects.requireNonNull(this$0);
        }

        @Override
        public int compare(MethodDebugInformation arg0, MethodDebugInformation arg1) {
            return arg0.getLuaName().compareTo(arg1.getLuaName());
        }
    };

    public ApiDocumentationExporter(Map<Class<?>, ClassDebugInformation> classes) {
        this.classes = classes;
        this.setupHierarchy();
    }

    public void setupHierarchy() {
        for (Map.Entry<Class<?>, ClassDebugInformation> entry : this.classes.entrySet()) {
            Class<?> clazz = entry.getKey();
            Class<?> zuper = clazz.getSuperclass();
            if (this.classes.get(zuper) != null) {
                List<Class<?>> list = this.classHierarchy.get(zuper);
                if (list == null) {
                    list = new ArrayList();
                    this.classHierarchy.put(zuper, list);
                }
                list.add(clazz);
            } else {
                this.rootClasses.add(clazz);
            }
            this.allClasses.add(clazz);
        }
        Collections.sort(this.allClasses, this.classSorter);
        Collections.sort(this.rootClasses, this.classSorter);
        for (List list : this.classHierarchy.values()) {
            Collections.sort(list, this.classSorter);
        }
    }

    @Override
    public List<Class<?>> getAllClasses() {
        return this.allClasses;
    }

    @Override
    public List<Class<?>> getChildrenForClass(Class<?> clazz) {
        List<Class<?>> list = this.classHierarchy.get(clazz);
        if (list != null) {
            return list;
        }
        return Collections.emptyList();
    }

    @Override
    public List<Class<?>> getRootClasses() {
        return this.rootClasses;
    }

    private List<MethodDebugInformation> getMethods(Class<?> clazz, boolean isMethod) {
        ArrayList<MethodDebugInformation> list = new ArrayList<MethodDebugInformation>();
        ClassDebugInformation information = this.classes.get(clazz);
        for (MethodDebugInformation methodInfo : information.getMethods().values()) {
            if (methodInfo.isMethod() != isMethod) continue;
            list.add(methodInfo);
        }
        Collections.sort(list, this.methodSorter);
        return list;
    }

    @Override
    public List<MethodDebugInformation> getFunctionsForClass(Class<?> clazz) {
        return this.getMethods(clazz, false);
    }

    @Override
    public List<MethodDebugInformation> getMethodsForClass(Class<?> clazz) {
        return this.getMethods(clazz, true);
    }
}


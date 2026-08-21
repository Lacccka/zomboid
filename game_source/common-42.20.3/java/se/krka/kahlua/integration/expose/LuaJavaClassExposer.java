/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose;

import java.awt.Desktop;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.annotations.Desc;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.integration.expose.AnnotationUtil;
import se.krka.kahlua.integration.expose.ClassDebugInformation;
import se.krka.kahlua.integration.expose.LuaJavaInvoker;
import se.krka.kahlua.integration.expose.MethodDebugInformation;
import se.krka.kahlua.integration.expose.MultiLuaJavaInvoker;
import se.krka.kahlua.integration.expose.caller.ConstructorCaller;
import se.krka.kahlua.integration.expose.caller.MethodCaller;
import se.krka.kahlua.integration.processor.ClassParameterInformation;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.Platform;
import zombie.HiddenFromLua;
import zombie.UsedFromLua;
import zombie.core.Core;
import zombie.debug.DebugType;

public class LuaJavaClassExposer {
    private static final Set<String> IGNORED_METHOD_NAMES = Set.of("wait", "notify", "notifyALl");
    private static final Set<Method> IGNORED_METHODS = Arrays.stream(Object.class.getMethods()).filter(m -> IGNORED_METHOD_NAMES.contains(m.getName())).collect(Collectors.toUnmodifiableSet());
    private static final Object DEBUGINFO_KEY = new Object();
    private final KahluaConverterManager manager;
    private final Platform platform;
    private final KahluaTable environment;
    private final KahluaTable classMetatables;
    private final Set<Type> visitedTypes = new HashSet<Type>();
    private final KahluaTable autoExposeBase;
    private final Map<Class<?>, Boolean> shouldExposeCache = new HashMap();
    public final HashMap<String, Class<?>> typeMap = new HashMap();
    private static final Set<Class<?>> DISALLOWED_CLASSES = Set.of(Class.class, ClassLoader.class, Module.class, Runtime.class, Desktop.class);

    public static boolean isDisallowed(Object object) {
        Class<?> c;
        Class<?> clazz;
        if (object == null) {
            return false;
        }
        Class<?> clazz2 = clazz = object instanceof Class ? (c = (Class<?>)object) : object.getClass();
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        if (clazz.isPrimitive()) {
            return false;
        }
        if (clazz.isAnnotationPresent(HiddenFromLua.class) || DISALLOWED_CLASSES.contains(clazz)) {
            return true;
        }
        if (Core.debug && (clazz == Method.class || clazz == Field.class)) {
            return false;
        }
        String packageName = clazz.getPackageName();
        return packageName.equals("java.lang.invoke") || packageName.equals("java.lang.reflect") || ClassLoader.class.isAssignableFrom(clazz);
    }

    public LuaJavaClassExposer(KahluaConverterManager manager, Platform platform, KahluaTable environment) {
        this(manager, platform, environment, null);
    }

    public LuaJavaClassExposer(KahluaConverterManager manager, Platform platform, KahluaTable environment_, KahluaTable autoExposeBase) {
        this.manager = manager;
        this.platform = platform;
        this.environment = environment_;
        this.autoExposeBase = autoExposeBase;
        this.classMetatables = KahluaUtil.getClassMetatables(platform, this.environment);
    }

    public Map<Class<?>, ClassDebugInformation> getClassDebugInformation() {
        HashMap classMap = this.environment.rawget(DEBUGINFO_KEY);
        if (!(classMap instanceof Map)) {
            classMap = new HashMap();
            this.environment.rawset(DEBUGINFO_KEY, classMap);
        }
        return classMap;
    }

    private KahluaTable getMetaTable(Class<?> clazz) {
        return (KahluaTable)this.classMetatables.rawget(clazz);
    }

    private KahluaTable getIndexTable(KahluaTable metaTable) {
        if (metaTable == null) {
            return null;
        }
        Object indexObject = metaTable.rawget("__index");
        if (indexObject == null) {
            return null;
        }
        if (indexObject instanceof KahluaTable) {
            KahluaTable kahluaTable = (KahluaTable)indexObject;
            return kahluaTable;
        }
        return null;
    }

    public void exposeGlobalObjectFunction(KahluaTable environment, Object owner, Method method) {
        this.exposeGlobalObjectFunction(environment, owner, method, method.getName());
    }

    public void exposeGlobalObjectFunction(KahluaTable environment, Object owner, Method method, String methodName) {
        Class<?> clazz = owner.getClass();
        LuaJavaInvoker invoker = this.getMethodInvoker(clazz, method, methodName, owner, false);
        this.addInvoker(environment, methodName, invoker);
    }

    public void exposeGlobalClassFunction(KahluaTable environment, Class<?> clazz, Constructor<?> constructor, String methodName) {
        LuaJavaInvoker invoker = this.getConstructorInvoker(clazz, constructor, methodName);
        this.addInvoker(environment, methodName, invoker);
    }

    private LuaJavaInvoker getMethodInvoker(Class<?> clazz, Method method, String methodName, Object owner, boolean hasSelf) {
        return new LuaJavaInvoker(this, this.manager, clazz, methodName, new MethodCaller(method, owner, hasSelf));
    }

    private LuaJavaInvoker getConstructorInvoker(Class<?> clazz, Constructor<?> constructor, String methodName) {
        return new LuaJavaInvoker(this, this.manager, clazz, methodName, new ConstructorCaller(constructor));
    }

    private LuaJavaInvoker getMethodInvoker(Class<?> clazz, Method method, String methodName) {
        return this.getMethodInvoker(clazz, method, methodName, null, true);
    }

    private LuaJavaInvoker getGlobalInvoker(Class<?> clazz, Method method, String methodName) {
        return this.getMethodInvoker(clazz, method, methodName, null, false);
    }

    public void exposeGlobalClassFunction(KahluaTable environment, Class<?> clazz, Method method, String methodName) {
        this.addInvoker(environment, methodName, this.getGlobalInvoker(clazz, method, methodName));
    }

    public void exposeMethod(Class<?> clazz, Method method, KahluaTable staticBase) {
        this.exposeMethod(clazz, method, method.getName(), staticBase);
    }

    public void exposeMethod(Class<?> clazz, Method method, String methodName, KahluaTable staticBase) {
        if (IGNORED_METHODS.contains(method)) {
            return;
        }
        if (!this.isExposed(clazz)) {
            this.setupMetaTables(clazz, staticBase);
        }
        KahluaTable metaTable = this.getMetaTable(clazz);
        KahluaTable indexTable = this.getIndexTable(metaTable);
        this.addInvoker(indexTable, methodName, this.getMethodInvoker(clazz, method, methodName));
    }

    private void addInvoker(KahluaTable indexTable, String methodName, LuaJavaInvoker invoker) {
        Object current = indexTable.rawget(methodName);
        if (current != null) {
            if (current instanceof LuaJavaInvoker) {
                LuaJavaInvoker luaJavaInvoker = (LuaJavaInvoker)current;
                if (current.equals(invoker)) {
                    return;
                }
                MultiLuaJavaInvoker multiInvoker = new MultiLuaJavaInvoker();
                multiInvoker.addInvoker(luaJavaInvoker);
                multiInvoker.addInvoker(invoker);
                indexTable.rawset(methodName, (Object)multiInvoker);
            } else if (current instanceof MultiLuaJavaInvoker) {
                MultiLuaJavaInvoker multiLuaJavaInvoker = (MultiLuaJavaInvoker)current;
                multiLuaJavaInvoker.addInvoker(invoker);
            }
        } else {
            indexTable.rawset(methodName, (Object)invoker);
        }
    }

    public boolean shouldExpose(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        Boolean bool = this.shouldExposeCache.get(clazz);
        if (bool != null) {
            return bool;
        }
        if (this.autoExposeBase != null) {
            this.exposeLikeJavaRecursively(clazz, this.autoExposeBase);
            return true;
        }
        if (this.isExposed(clazz)) {
            this.shouldExposeCache.put(clazz, Boolean.TRUE);
            return true;
        }
        if (this.shouldExpose(clazz.getSuperclass())) {
            this.shouldExposeCache.put(clazz, Boolean.TRUE);
            return true;
        }
        for (Class<?> sub : clazz.getInterfaces()) {
            if (!this.shouldExpose(sub)) continue;
            this.shouldExposeCache.put(clazz, Boolean.TRUE);
            return true;
        }
        this.shouldExposeCache.put(clazz, Boolean.FALSE);
        return false;
    }

    private void setupMetaTables(Class<?> clazz, KahluaTable staticBase) {
        Class<?> superClazz = clazz.getSuperclass();
        this.exposeLikeJavaRecursively(superClazz, staticBase);
        KahluaTable superMetaTable = this.getMetaTable(superClazz);
        KahluaTable metatable = this.platform.newTable();
        KahluaTable indexTable = this.platform.newTable();
        metatable.rawset("__index", (Object)indexTable);
        if (superMetaTable != null) {
            metatable.rawset("__newindex", superMetaTable.rawget("__newindex"));
        }
        indexTable.setMetatable(superMetaTable);
        this.classMetatables.rawset(clazz, (Object)metatable);
    }

    private void addJavaEquals(KahluaTable metatable) {
        metatable.rawset("__eq", (Object)new JavaFunction(this){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public int call(LuaCallFrame callFrame, int nArguments) {
                boolean equals = callFrame.get(0).equals(callFrame.get(1));
                callFrame.push(equals);
                return 1;
            }
        });
    }

    public void exposeGlobalFunctions(Object object) {
        Class<?> clazz = object.getClass();
        if (LuaJavaClassExposer.isDisallowed(clazz)) {
            return;
        }
        for (Method method : clazz.getMethods()) {
            LuaMethod luaMethod = AnnotationUtil.getAnnotation(method, LuaMethod.class);
            if (luaMethod == null) continue;
            String methodName = luaMethod.name().isEmpty() ? method.getName() : luaMethod.name();
            if (!luaMethod.global()) continue;
            this.exposeGlobalObjectFunction(this.environment, object, method, methodName);
        }
    }

    public void exposeLikeJava(Class<?> clazz) {
        this.exposeLikeJava(clazz, this.autoExposeBase);
    }

    public void exposeLikeJava(Class<?> clazz, KahluaTable staticBase) {
        if (clazz == null || this.isExposed(clazz) || !this.shouldExpose(clazz) || LuaJavaClassExposer.isDisallowed(clazz)) {
            return;
        }
        this.setupMetaTables(clazz, staticBase);
        this.exposeMethods(clazz, staticBase);
        if (!(clazz.isSynthetic() || clazz.isAnonymousClass() || clazz.isPrimitive() || Proxy.isProxyClass(clazz) || clazz.getSimpleName().startsWith("$"))) {
            this.exposeStatics(clazz, staticBase);
        }
    }

    private void exposeStatics(Class<?> clazz, KahluaTable staticBase) {
        String name;
        String[] packageStructure = clazz.getName().replaceAll("\\$", ".").split("\\.");
        KahluaTable container = this.createTableStructure(staticBase, packageStructure);
        container.rawset("class", clazz);
        if (staticBase.rawget(clazz.getSimpleName()) == null) {
            staticBase.rawset(clazz.getSimpleName(), (Object)container);
        }
        for (Method method : clazz.getMethods()) {
            name = method.getName();
            if (method.isAnnotationPresent(HiddenFromLua.class) || !LuaJavaClassExposer.isPublic(method) || !LuaJavaClassExposer.isStatic(method)) continue;
            this.exposeGlobalClassFunction(container, clazz, method, name);
        }
        for (AccessibleObject accessibleObject : clazz.getFields()) {
            name = ((Field)accessibleObject).getName();
            if (accessibleObject.isAnnotationPresent(HiddenFromLua.class) || !LuaJavaClassExposer.isPublic((Member)((Object)accessibleObject)) || !LuaJavaClassExposer.isStatic((Member)((Object)accessibleObject))) continue;
            if (container.rawget(name) == null) {
                try {
                    container.rawset(name, ((Field)accessibleObject).get(clazz));
                }
                catch (IllegalAccessException illegalAccessException) {}
                continue;
            }
            DebugType.Lua.warn("Tried to overwrite a method using field " + name + " in class " + clazz.getSimpleName().replaceAll("\\$", "."));
        }
        for (AccessibleObject accessibleObject : clazz.getConstructors()) {
            if (accessibleObject.isAnnotationPresent(HiddenFromLua.class) || !LuaJavaClassExposer.isPublic((Member)((Object)accessibleObject)) || LuaJavaClassExposer.isInterface((Member)((Object)accessibleObject)) || LuaJavaClassExposer.isAbstract((Member)((Object)accessibleObject))) continue;
            this.addInvoker(container, "new", this.getConstructorInvoker(clazz, (Constructor<?>)accessibleObject, "new"));
        }
    }

    private void exposeMethods(Class<?> clazz, KahluaTable staticBase) {
        for (Method method : clazz.getMethods()) {
            String name = method.getName();
            if (method.isAnnotationPresent(HiddenFromLua.class) || !LuaJavaClassExposer.isPublic(method) || LuaJavaClassExposer.isStatic(method)) continue;
            this.exposeMethod(clazz, method, name, staticBase);
        }
    }

    private KahluaTable createTableStructure(KahluaTable base, String[] structure) {
        for (String s : structure) {
            base = KahluaUtil.getOrCreateTable(this.platform, base, s);
        }
        return base;
    }

    public boolean isExposed(Class<?> clazz) {
        return clazz != null && this.getMetaTable(clazz) != null;
    }

    ClassDebugInformation getDebugdata(Class<?> clazz) {
        this.readDebugDataD(clazz);
        return this.getClassDebugInformation().get(clazz);
    }

    ClassDebugInformation getDebugdataA(Class<?> clazz) {
        return this.getClassDebugInformation().get(clazz);
    }

    private void readDebugDataD(Class<?> clazz) {
        if (this.getDebugdataA(clazz) == null) {
            ClassParameterInformation parameterInfo = null;
            try {
                parameterInfo = ClassParameterInformation.getFromStream(clazz);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (parameterInfo == null) {
                parameterInfo = new ClassParameterInformation(clazz);
            }
            ClassDebugInformation debugInfo = new ClassDebugInformation(clazz, parameterInfo);
            Map<Class<?>, ClassDebugInformation> information = this.getClassDebugInformation();
            information.put(clazz, debugInfo);
        }
    }

    @LuaMethod(global=true, name="definition")
    @Desc(value="returns a string that describes the object")
    public String getDefinition(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof LuaJavaInvoker) {
            LuaJavaInvoker luaJavaInvoker = (LuaJavaInvoker)obj;
            MethodDebugInformation data = luaJavaInvoker.getMethodDebugData();
            return data.toString();
        }
        if (obj instanceof MultiLuaJavaInvoker) {
            MultiLuaJavaInvoker multiLuaJavaInvoker = (MultiLuaJavaInvoker)obj;
            StringBuilder builder = new StringBuilder();
            for (LuaJavaInvoker invoker : multiLuaJavaInvoker.getInvokers()) {
                builder.append(invoker.getMethodDebugData().toString());
            }
            return builder.toString();
        }
        return KahluaUtil.tostring(obj, KahluaUtil.getWorkerThread(this.platform, this.environment));
    }

    public void exposeLikeJavaRecursively(Type type, KahluaTable staticBase) {
        this.exposeLikeJava(staticBase, this.visitedTypes, type);
    }

    private void exposeLikeJava(KahluaTable staticBase, Set<Type> visited, Type type) {
        if (type == null) {
            return;
        }
        if (visited.contains(type)) {
            return;
        }
        visited.add(type);
        if (type instanceof Class) {
            Class clazz = (Class)type;
            if (!this.shouldExpose(clazz)) {
                return;
            }
            this.exposeLikeJavaByClass(staticBase, visited, clazz);
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)type;
            this.exposeList(staticBase, visited, wildcardType.getLowerBounds());
            this.exposeList(staticBase, visited, wildcardType.getUpperBounds());
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            this.exposeLikeJava(staticBase, visited, parameterizedType.getRawType());
            this.exposeLikeJava(staticBase, visited, parameterizedType.getOwnerType());
            this.exposeList(staticBase, visited, parameterizedType.getActualTypeArguments());
        } else if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable)type;
            this.exposeList(staticBase, visited, typeVariable.getBounds());
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType)type;
            this.exposeLikeJava(staticBase, visited, genericArrayType.getGenericComponentType());
        }
    }

    private void exposeList(KahluaTable staticBase, Set<Type> visited, Type[] types) {
        for (Type t : types) {
            this.exposeLikeJava(staticBase, visited, t);
        }
    }

    private void exposeLikeJavaByClass(KahluaTable staticBase, Set<Type> visited, Class<?> clazz) {
        String t = clazz.toString();
        t = t.substring(t.lastIndexOf(".") + 1);
        this.typeMap.put(t, clazz);
        if (Core.IS_DEV && !clazz.isAnnotationPresent(UsedFromLua.class) && !clazz.getName().startsWith("java.")) {
            throw new RuntimeException("Missing @%s annotation in %s".formatted(UsedFromLua.class.getSimpleName(), clazz.getName()));
        }
        this.exposeList(staticBase, visited, clazz.getInterfaces());
        this.exposeLikeJava(staticBase, visited, clazz.getGenericSuperclass());
        if (clazz.isArray()) {
            this.exposeLikeJavaByClass(staticBase, visited, clazz.getComponentType());
        } else {
            this.exposeLikeJava(clazz, staticBase);
        }
        for (Method method : clazz.getDeclaredMethods()) {
            this.exposeList(staticBase, visited, method.getGenericParameterTypes());
            this.exposeList(staticBase, visited, method.getGenericExceptionTypes());
            this.exposeLikeJava(staticBase, visited, method.getGenericReturnType());
        }
        for (AccessibleObject accessibleObject : clazz.getDeclaredFields()) {
            this.exposeLikeJava(staticBase, visited, ((Field)accessibleObject).getGenericType());
        }
        for (AccessibleObject accessibleObject : clazz.getConstructors()) {
            this.exposeList(staticBase, visited, ((Constructor)accessibleObject).getParameterTypes());
            this.exposeList(staticBase, visited, ((Constructor)accessibleObject).getExceptionTypes());
        }
    }

    public void destroy() {
        this.shouldExposeCache.clear();
        this.typeMap.clear();
        this.visitedTypes.clear();
    }

    private static boolean isPublic(Member method) {
        return Modifier.isPublic(method.getModifiers());
    }

    private static boolean isAbstract(Member member) {
        return Modifier.isAbstract(member.getModifiers());
    }

    private static boolean isInterface(Member member) {
        return Modifier.isInterface(member.getModifiers());
    }

    private static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }
}


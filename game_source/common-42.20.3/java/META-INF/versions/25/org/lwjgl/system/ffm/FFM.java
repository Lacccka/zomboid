/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.classfile.ClassFile;
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.ModuleAttribute;
import java.lang.classfile.attribute.ModuleExportInfo;
import java.lang.classfile.attribute.ModuleRequireInfo;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.constant.ModuleDesc;
import java.lang.constant.PackageDesc;
import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.ffm.BCCallDown;
import org.lwjgl.system.ffm.BCCallUp;
import org.lwjgl.system.ffm.BCDescriptors;
import org.lwjgl.system.ffm.BCGroup;
import org.lwjgl.system.ffm.BCUtil;
import org.lwjgl.system.ffm.Binder;
import org.lwjgl.system.ffm.FFMConfig;
import org.lwjgl.system.ffm.GroupBinder;
import org.lwjgl.system.ffm.StructBinder;
import org.lwjgl.system.ffm.TraceConsumer;
import org.lwjgl.system.ffm.UnionBinder;
import org.lwjgl.system.ffm.UpcallBinder;
import org.lwjgl.system.ffm.mapping.DataMapping;
import org.lwjgl.system.ffm.mapping.Mapping;
import org.lwjgl.system.libffi.FFICIF;

public final class FFM {
    static final AddressLayout C_POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Long.MAX_VALUE, ValueLayout.JAVA_BYTE));
    static final Set<String> STANDARD_CHARSETS = Arrays.stream(StandardCharsets.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toUnmodifiableSet());
    static final ConcurrentHashMap<AnnotatedElement, FFMConfig> BINDING_CONFIGS = new ConcurrentHashMap();
    static final ScopedValue<Arena> ARENA = ScopedValue.newInstance();
    public static final Mapping.Opaque opaque = Mapping.createOpaque("void");
    public static final Mapping.Boolean jboolean = Mapping.createBoolean("boolean");
    public static final Mapping.Byte jbyte = Mapping.createByte("byte", true);
    public static final Mapping.Char jchar = Mapping.createChar("char");
    public static final Mapping.Short jshort = Mapping.createShort("short", true);
    public static final Mapping.Int jint = Mapping.createInt("int", true);
    public static final Mapping.Long jlong = Mapping.createLong("long", true);
    public static final Mapping.Float jfloat = Mapping.createFloat("float");
    public static final Mapping.Double jdouble = Mapping.createDouble("double");
    public static final Mapping.Byte int8_t = jbyte.typedef("int8_t");
    public static final Mapping.Short int16_t = jshort.typedef("int16_t");
    public static final Mapping.Int int32_t = jint.typedef("int32_t");
    public static final Mapping.Long int64_t = jlong.typedef("int64_t");
    public static final Mapping.Byte uint8_t = Mapping.createByte("uint8_t", false);
    public static final Mapping.Short uint16_t = Mapping.createShort("uint16_t", false);
    public static final Mapping.Int uint32_t = Mapping.createInt("uint32_t", false);
    public static final Mapping.Long uint64_t = Mapping.createLong("uint64_t", false);
    public static final Mapping.Size size_t = Mapping.createSize("size_t", false);
    public static final Mapping.Size ptrdiff_t = Mapping.createSize("ptrdiff_t", true);
    public static final Mapping.Size intptr_t = Mapping.createSize("intptr_t", true);
    public static final Mapping.Size uintptr_t = Mapping.createSize("uintptr_t", false);
    public static final Mapping.Boolean bool = jboolean.typedef("bool");
    public static final Mapping.Byte cchar = int8_t.typedef("char");
    public static final Mapping.Short cshort = int16_t.typedef("short");
    public static final Mapping.Int cint = int32_t.typedef("int");
    public static final Mapping.CLong clong = Mapping.createCLong("long", true);
    public static final Mapping.Long long_long = jlong.typedef("long long");
    public static final Mapping.Byte unsigned_char = uint8_t.typedef("unsigned char");
    public static final Mapping.Short unsigned_short = uint16_t.typedef("unsigned short");
    public static final Mapping.Int unsigned_int = uint32_t.typedef("unsigned int");
    public static final Mapping.CLong unsigned_long = Mapping.createCLong("unsigned long", false);
    public static final Mapping.Long unsigned_long_long = uint64_t.typedef("unsigned long long");
    public static final Mapping.Float float32 = jfloat.typedef("float");
    public static final Mapping.Double float64 = jdouble.typedef("double");

    private FFM() {
    }

    static void main() {
        Path path = Path.of("bin", "classes", "lwjgl", "core", "META-INF", "versions", "25", "module-info.class");
        ModuleAttribute moduleAttr = ModuleAttribute.of(ModuleDesc.of("org.lwjgl"), mab -> {
            mab.moduleVersion(System.getProperty("module.version")).requires(ModuleRequireInfo.of(ModuleDesc.of("java.base"), AccessFlag.MODULE.mask(), "25")).exports(ModuleExportInfo.of(PackageDesc.of("org.lwjgl"), 0, new ModuleDesc[0])).exports(ModuleExportInfo.of(PackageDesc.of("org.lwjgl.system"), 0, new ModuleDesc[0])).exports(ModuleExportInfo.of(PackageDesc.of("org.lwjgl.system.ffm"), 0, new ModuleDesc[0])).exports(ModuleExportInfo.of(PackageDesc.of("org.lwjgl.system.freebsd"), 0, new ModuleDesc[0])).exports(ModuleExportInfo.of(PackageDesc.of("org.lwjgl.system.jni"), 0, new ModuleDesc[0])).exports(ModuleExportInfo.of(PackageDesc.of("org.lwjgl.system.libc"), 0, new ModuleDesc[0])).exports(ModuleExportInfo.of(PackageDesc.of("org.lwjgl.system.libffi"), 0, new ModuleDesc[0])).exports(ModuleExportInfo.of(PackageDesc.of("org.lwjgl.system.linux"), 0, new ModuleDesc[0])).exports(ModuleExportInfo.of(PackageDesc.of("org.lwjgl.system.macosx"), 0, new ModuleDesc[0])).exports(ModuleExportInfo.of(PackageDesc.of("org.lwjgl.system.windows"), 0, new ModuleDesc[0]));
            if (Boolean.getBoolean("unsafe")) {
                mab.requires(ModuleRequireInfo.of(ModuleDesc.of("jdk.unsupported"), AccessFlag.TRANSITIVE.mask(), "25"));
            }
        });
        try {
            ClassFile.of().buildModuleTo(path, moduleAttr);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static FFMConfig getConfig(Class<?> bindingInterface) {
        for (Class<?> c = bindingInterface; c != null; c = c.getEnclosingClass()) {
            FFMConfig config = BINDING_CONFIGS.get(c);
            if (config == null) continue;
            return config;
        }
        Package p = Objects.requireNonNull(bindingInterface.getPackage());
        FFMConfig config = BINDING_CONFIGS.get(p);
        if (config == null) {
            throw new IllegalStateException("No FFMConfig registered for " + String.valueOf(bindingInterface));
        }
        return config;
    }

    private static <T> T generate(Class<T> bindingInterface, FFMConfig config) throws Exception {
        Method[] methods = bindingInterface.getMethods();
        ClassDesc thisClass = ClassDesc.of(bindingInterface.getPackageName(), bindingInterface.getSimpleName() + "Impl");
        byte[] bytecode = ClassFile.of().build(thisClass, classBuilder -> {
            BCUtil.startHiddenClass(classBuilder).withInterfaceSymbols(bindingInterface.describeConstable().orElseThrow());
            for (int m = 0; m < methods.length; ++m) {
                Method method = methods[m];
                MethodTypeDesc methodTypeDesc = BCUtil.getMethodTypeDesc(method);
                DynamicConstantDesc condy = DynamicConstantDesc.ofNamed(BCDescriptors.DMHD_FFM_bootstrapDowncall, method.getName(), ConstantDescs.CD_MethodHandle, Integer.valueOf(m));
                classBuilder.withMethod(method.getName(), methodTypeDesc, 1, mb -> mb.withCode(cb -> {
                    cb.ldc(condy);
                    for (int p = 0; p < methodTypeDesc.parameterCount(); ++p) {
                        cb.loadLocal(TypeKind.from(methodTypeDesc.parameterType(p)), cb.parameterSlot(p));
                    }
                    cb.invokevirtual(ConstantDescs.CD_MethodHandle, "invokeExact", methodTypeDesc).return_(TypeKind.from(methodTypeDesc.returnType()));
                }));
            }
        });
        return (T)config.lookup.defineHiddenClassWithClassData(bytecode, List.of(config, methods), false, new MethodHandles.Lookup.ClassOption[0]).lookupClass().getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
    }

    public static MethodHandle bootstrapDowncall(MethodHandles.Lookup lookup, String name, Class<?> bootstrapClass, int methodIndex) throws IllegalAccessException {
        FFMConfig config = MethodHandles.classDataAt(lookup, "_", FFMConfig.class, 0);
        Method method = MethodHandles.classDataAt(lookup, "_", Method[].class, 1)[methodIndex];
        if (config.debugGenerator) {
            APIUtil.apiLog("BOOTSTRAPPING DOWNCALL#" + methodIndex + ": " + name);
        }
        return new BCCallDown(config, method).bootstrap();
    }

    static Field findBinderField(Class<?> targetType) {
        Field field = null;
        for (Field targetField : targetType.getDeclaredFields()) {
            Class binderClass;
            ParameterizedType binderTypeGeneric;
            Type[] binderTypeArguments;
            int modifiers = targetField.getModifiers();
            if (!(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers))) {
                throw new IllegalStateException(String.valueOf(targetType) + " is not an interface");
            }
            Type binderType = targetField.getGenericType();
            if (!(binderType instanceof ParameterizedType) || (binderTypeArguments = (binderTypeGeneric = (ParameterizedType)binderType).getActualTypeArguments()).length != 1 || !binderTypeArguments[0].equals(targetType) || !GroupBinder.class.isAssignableFrom(binderClass = (Class)binderTypeGeneric.getRawType()) && !UpcallBinder.class.isAssignableFrom(binderClass)) continue;
            if (field != null) {
                throw new IllegalStateException("Multiple binder fields found for " + String.valueOf(targetType));
            }
            field = targetField;
        }
        if (field == null) {
            throw new IllegalStateException("No binder field found for " + String.valueOf(targetType));
        }
        return field;
    }

    static FFMConfig.BinderField lookupBinder(FFMConfig config, Class<?> targetType) {
        FFMConfig.BinderField binderField = config.binders.get(targetType);
        if (binderField == null) {
            binderField = FFM.lookupBinderCacheMiss(config, targetType);
        }
        return binderField;
    }

    private static FFMConfig.BinderField lookupBinderCacheMiss(FFMConfig config, Class<?> targetType) {
        Binder binder;
        Field field = FFM.findBinderField(targetType);
        try {
            binder = (Binder)field.get(null);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (binder == null) {
            throw new IllegalStateException("Missing binder field value for " + String.valueOf(targetType));
        }
        FFMConfig.BinderField binderField = new FFMConfig.BinderField(field.getName(), binder);
        config.binders.put(targetType, binderField);
        return binderField;
    }

    public static <T> StructBinderBuilder<T> ffmStruct(Class<T> structInterface) {
        return new StructBinderBuilder<T>(structInterface);
    }

    public static <T> UnionBinderBuilder<T> ffmUnion(Class<T> unionInterface) {
        return new UnionBinderBuilder<T>(unionInterface);
    }

    public static <T> UpcallBinder<T> ffmUpcall(Class<T> upcallInterface) {
        return FFM.ffmUpcall(upcallInterface, null);
    }

    public static <T> UpcallBinder<T> ffmUpcall(Class<T> upcallInterface, @Nullable FFICIF cif) {
        FFMConfig config = FFM.getConfig(upcallInterface);
        if (config.debugGenerator) {
            APIUtil.apiLog("BOOTSTRAPPING UPCALL " + String.valueOf(upcallInterface));
        }
        return new BCCallUp(config, upcallInterface, cif).bootstrap();
    }

    public static ScopedValue<Arena> ffmScopedArena() {
        return ARENA;
    }

    public static void ffmScopedRun(Arena arena, Runnable runnable) {
        ScopedValue.where(ARENA, arena).run(runnable);
    }

    public static <R, X extends Throwable> R ffmScopedCall(Arena arena, ScopedValue.CallableOp<? extends R, X> op) throws X {
        return ScopedValue.where(ARENA, arena).call(op);
    }

    public static <T> T ffmGenerate(Class<T> bindingInterface) {
        try {
            return FFM.generate(bindingInterface, FFM.getConfig(bindingInterface));
        }
        catch (Error | RuntimeException e) {
            throw e;
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static <T> T ffmGenerate(Class<T> bindingInterface, FFMConfig config) {
        FFMConfig previous = BINDING_CONFIGS.put(bindingInterface, config);
        try {
            T t = FFM.generate(bindingInterface, config);
            return t;
        }
        catch (Error | RuntimeException e) {
            throw e;
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
        finally {
            FFM.ffmConfig(bindingInterface, previous);
        }
    }

    public static FFMConfigBuilder ffmConfigBuilder(MethodHandles.Lookup lookup) {
        return new FFMConfigBuilder(lookup);
    }

    public static void ffmConfig(Package _package, @Nullable FFMConfig config) {
        if (config == null) {
            BINDING_CONFIGS.remove(_package);
        } else {
            BINDING_CONFIGS.put(_package, config);
        }
    }

    public static void ffmConfig(Class<?> _class, @Nullable FFMConfig config) {
        if (config == null) {
            BINDING_CONFIGS.remove(_class);
        } else {
            BINDING_CONFIGS.put(_class, config);
        }
    }

    public static final class StructBinderBuilder<T>
    extends GroupBinderBuilder<T, StructLayout, StructBinder<T>, StructBinderBuilder<T>> {
        StructBinderBuilder(Class<T> structInterface) {
            super(structInterface);
        }

        @Override
        StructBinderBuilder<T> self() {
            return this;
        }

        @Override
        BCGroup.Kind kind() {
            return BCGroup.Kind.STRUCT;
        }

        @Override
        public StructBinderBuilder<T> m(String name, DataMapping<?> mapping) {
            Object layout = mapping.layout();
            long layoutAlignment = layout.byteAlignment();
            if (this.packAlignment < layoutAlignment) {
                layoutAlignment = this.packAlignment;
                layout = MemoryLayout.sequenceLayout(layout.byteSize(), ValueLayout.JAVA_BYTE).withByteAlignment(this.packAlignment);
            }
            if (this.automaticPadding && this.sizeof % layoutAlignment != 0L) {
                this.padding(StructBinderBuilder.align(this.sizeof, layoutAlignment) - this.sizeof);
            }
            this.alignof = Math.max(this.alignof, layoutAlignment);
            this.sizeof += layout.byteSize();
            return (StructBinderBuilder)this.addMember(name, (MemoryLayout)layout);
        }

        @Override
        public StructBinder<T> build() {
            return (StructBinder)super.build();
        }
    }

    public static final class UnionBinderBuilder<T>
    extends GroupBinderBuilder<T, UnionLayout, UnionBinder<T>, UnionBinderBuilder<T>> {
        UnionBinderBuilder(Class<T> UnionInterface) {
            super(UnionInterface);
        }

        @Override
        UnionBinderBuilder<T> self() {
            return this;
        }

        @Override
        BCGroup.Kind kind() {
            return BCGroup.Kind.UNION;
        }

        @Override
        public UnionBinderBuilder<T> m(String name, DataMapping<?> mapping) {
            Object layout = mapping.layout();
            long layoutAlignment = Math.min(layout.byteAlignment(), this.packAlignment);
            this.alignof = Math.max(this.alignof, layoutAlignment);
            this.sizeof = Math.max(this.sizeof, layout.byteSize());
            return (UnionBinderBuilder)this.addMember(name, (MemoryLayout)layout);
        }

        @Override
        public UnionBinder<T> build() {
            return (UnionBinder)super.build();
        }
    }

    public static final class FFMConfigBuilder {
        private static final @Nullable Class<? extends Annotation> defaultNullableAnnotation;
        private final MethodHandles.Lookup lookup;
        private @Nullable Class<? extends Annotation> nullableAnnotation = defaultNullableAnnotation;
        private @Nullable SymbolLookup symbolLookup;
        private @Nullable TraceConsumer traceConsumer;
        private @Nullable Predicate<Method> tracingFilter;
        private @Nullable Function<Method, Boolean> criticalOverride;
        private boolean checks = Checks.CHECKS;
        private boolean debugGenerator = Configuration.DEBUG_GENERATOR.get(false);

        FFMConfigBuilder(MethodHandles.Lookup lookup) {
            this.lookup = lookup;
        }

        public FFMConfigBuilder withNullableAnnotation(Class<? extends Annotation> annotation) {
            this.nullableAnnotation = annotation;
            return this;
        }

        public FFMConfigBuilder withSymbolLookup(SymbolLookup lookup) {
            this.symbolLookup = lookup;
            return this;
        }

        public FFMConfigBuilder withFunctionProvider(FunctionProvider provider) {
            return this.withSymbolLookup(name -> Optional.of(MemorySegment.ofAddress(provider.getFunctionAddress(name))));
        }

        public FFMConfigBuilder withTracing(TraceConsumer consumer) {
            this.traceConsumer = consumer;
            return this;
        }

        public FFMConfigBuilder withTracing(TraceConsumer consumer, Predicate<Method> filter) {
            this.traceConsumer = consumer;
            this.tracingFilter = filter;
            return this;
        }

        public FFMConfigBuilder withCriticalOverride(Function<Method, @Nullable Boolean> criticalOverride) {
            this.criticalOverride = criticalOverride;
            return this;
        }

        public FFMConfigBuilder withChecks(boolean enabled) {
            this.checks = enabled;
            return this;
        }

        public FFMConfigBuilder withDebugGenerator(boolean enabled) {
            this.debugGenerator = enabled;
            return this;
        }

        public FFMConfig build() {
            return new FFMConfig(this.nullableAnnotation, this.lookup, this.symbolLookup, this.traceConsumer, this.tracingFilter, this.criticalOverride, this.checks, this.debugGenerator);
        }

        static {
            Class<?> nullableAnnotation = null;
            Object config = Configuration.FFM_DEFAULT_NULLABLE_ANNOTATION.get("auto");
            if (!"none".equals(config)) {
                if (config instanceof String) {
                    String annotationClassName = (String)config;
                    boolean custom = !"auto".equals(annotationClassName);
                    List<String> popularNullableAnnotationClasses = custom ? List.of(annotationClassName) : List.of("org.jspecify.annotations.Nullable", "javax.annotation.Nullable", "org.jetbrains.annotations.Nullable", "org.eclipse.jdt.annotation.Nullable", "edu.umd.cs.findbugs.annotations.Nullable", "org.checkerframework.checker.nullness.qual.Nullable");
                    for (String className : popularNullableAnnotationClasses) {
                        try {
                            Class<?> c = Class.forName(className);
                            if (!Annotation.class.isAssignableFrom(c)) continue;
                            Class<?> annotationClass = c;
                            FFMConfig.validateNullableAnnotationClass(annotationClass);
                            nullableAnnotation = annotationClass;
                            APIUtil.apiLog("Default nullable annotation: " + String.valueOf(annotationClass));
                            break;
                        }
                        catch (Exception e) {
                            if (!custom) continue;
                            throw new IllegalStateException(e);
                        }
                    }
                } else if (config instanceof Class) {
                    Class<?> annotationClass;
                    Class<?> nullableAnnotationClass = annotationClass = (Class<?>)config;
                    FFMConfig.validateNullableAnnotationClass((Class<? extends Annotation>)nullableAnnotationClass);
                    nullableAnnotation = nullableAnnotationClass;
                }
            }
            defaultNullableAnnotation = nullableAnnotation;
        }
    }

    public static abstract sealed class GroupBinderBuilder<T, L extends GroupLayout, M extends GroupBinder<L, T>, SELF extends GroupBinderBuilder<T, L, M, SELF>>
    permits StructBinderBuilder, UnionBinderBuilder {
        final Class<T> groupInterface;
        final Field binderField;
        final SequencedMap<String, MemoryLayout> members = new LinkedHashMap<String, MemoryLayout>();
        @Nullable BiPredicate<T, Object> equals;
        @Nullable ToIntFunction<T> hashCode;
        @Nullable Function<T, String> toString;
        protected boolean automaticPadding = true;
        protected boolean checkPadding = true;
        private int paddingIndex;
        protected long sizeof;
        protected long alignof;
        protected long packAlignment = Long.MAX_VALUE;
        private long alignas;

        protected GroupBinderBuilder(Class<T> groupInterface) {
            if (!groupInterface.isInterface()) {
                throw new UnsupportedOperationException("The binder must be parameterized with an interface");
            }
            if (groupInterface.isHidden()) {
                throw new UnsupportedOperationException("The binder must not be parameterized with a hidden interface");
            }
            if (groupInterface.isSealed()) {
                throw new UnsupportedOperationException("The binder must not be parameterized with a sealed interface");
            }
            if (groupInterface.getTypeParameters().length != 0) {
                throw new UnsupportedOperationException("The binder must not be parameterized with a generic interface");
            }
            this.groupInterface = groupInterface;
            this.binderField = FFM.findBinderField(groupInterface);
        }

        abstract SELF self();

        abstract BCGroup.Kind kind();

        protected static long align(long offset, long alignment) {
            return (offset - 1L | alignment - 1L) + 1L;
        }

        public M build() {
            long sizeofAligned;
            long byteAlignment = Math.max(this.alignof, this.alignas);
            if (this.automaticPadding && (sizeofAligned = GroupBinderBuilder.align(this.sizeof, byteAlignment)) != this.sizeof) {
                this.padding(sizeofAligned - this.sizeof);
            }
            Object m = BCGroup.bootstrap(this, byteAlignment);
            if (this.checkPadding && !GroupBinderBuilder.isAligned(m.layout().byteSize(), m.layout().byteAlignment())) {
                throw new IllegalStateException("Group size is not a multiple of its alignment");
            }
            return m;
        }

        private static boolean isAligned(long offset, long alignment) {
            return (offset & alignment - 1L) == 0L;
        }

        public SELF automaticPadding(boolean enabled) {
            this.automaticPadding = enabled;
            return this.self();
        }

        public SELF checkPadding(boolean enabled) {
            this.checkPadding = enabled;
            return this.self();
        }

        public SELF pack(long alignment) {
            this.packAlignment = alignment;
            return this.self();
        }

        public SELF alignas(long alignment) {
            this.alignas = alignment;
            return this.self();
        }

        public abstract SELF m(String var1, DataMapping<?> var2);

        protected SELF addMember(String name, MemoryLayout layout) {
            MemoryLayout previous = this.members.put(name, layout.withName(name));
            if (previous != null) {
                throw new IllegalStateException("struct member '" + name + "' is already defined");
            }
            return this.self();
        }

        public SELF padding(long padding) {
            this.members.put("__padding__" + this.paddingIndex++, MemoryLayout.paddingLayout(padding));
            this.sizeof += padding;
            return this.self();
        }

        public SELF withEquals(BiPredicate<T, Object> equals) {
            this.equals = equals;
            return this.self();
        }

        public SELF withHashCode(ToIntFunction<T> hashCode) {
            this.hashCode = hashCode;
            return this.self();
        }

        public SELF withToString(Function<T, String> toString) {
            this.toString = toString;
            return this.self();
        }
    }
}

